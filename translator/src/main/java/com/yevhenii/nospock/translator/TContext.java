package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JField;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JPackage;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.translator.spi.TranslationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides additional insights into translation flow
 * for translators to make smarter decisions.
 */
public class TContext {

  /**
   * Represents variable declaration under the scope specified by cpath.
   */
  public static class VariableDeclaration {
    public final JExpression left;
    public final JExpression right;
    public final CPath path;

    VariableDeclaration(JExpression left, JExpression right, CPath path) {
      this.left = Objects.requireNonNull(left);
      this.right = right;
      this.path = Objects.requireNonNull(path);
    }
  }

  /**
   * Represents field declaration under the cpath scope.
   */
  public static class FieldDeclaration {
    public final JField field;
    public final CPath path;

    FieldDeclaration(JField field, CPath path) {
      this.field = field;
      this.path = path;
    }
  }

  /**
   * Represents parameter declaration under the cpath scope.
   */
  public static class ParametersDeclaration {
    public final List<JParameter> parameters;
    public final CPath path;

    public ParametersDeclaration(List<JParameter> parameters, CPath path) {
      this.parameters = Objects.requireNonNull(parameters);
      this.path = Objects.requireNonNull(path);
    }
  }

  private final JPackage package0;
  private final List<JImport> imports;
  private final List<VariableDeclaration> variableDeclarations;
  private final List<FieldDeclaration> fieldDeclarations;
  private final List<ParametersDeclaration> methodParametersDeclarations;
  private final List<ParametersDeclaration> lambdaParametersDeclarations;
  private final CPath path;

  public TContext(
    JPackage package0,
    List<JImport> imports,
    List<VariableDeclaration> variableDeclarations,
    List<FieldDeclaration> fieldDeclarations,
    List<ParametersDeclaration> methodParametersDeclarations,
    List<ParametersDeclaration> lambdaParametersDeclarations,
    CPath path
  ) {
    this.package0 = package0;
    this.imports = new ArrayList<>(imports);
    this.fieldDeclarations = new ArrayList<>(fieldDeclarations);
    this.variableDeclarations = new ArrayList<>(variableDeclarations);
    this.methodParametersDeclarations = new ArrayList<>(methodParametersDeclarations);
    this.lambdaParametersDeclarations = new ArrayList<>(lambdaParametersDeclarations);
    this.path = path;
  }

  public List<JImport> imports() {
    return imports;
  }

  public JPackage package0() {
    return package0;
  }

  public void declareVariable(JExpression left, JExpression right) {
    variableDeclarations.add(new VariableDeclaration(left, right, path));
  }

  public void declareField(JField field) {
    fieldDeclarations.add(new FieldDeclaration(field, path));
  }

  public void declareMethodParameters(List<JParameter> parameters, CPath path) {
    if (!path.last().isMethod()) {
      throw new TranslationException("Cannot declare parameters not being in method path " + path);
    }
    methodParametersDeclarations.add(new ParametersDeclaration(parameters, path));
  }

  public void declareLambdaParameters(List<JParameter> parameters, CPath path) {
    if (!path.last().isLambda()) {
      throw new TranslationException("Cannot declare parameters for non lambda top level segment " + path);
    }
    lambdaParametersDeclarations.add(new ParametersDeclaration(parameters, path));
  }

  public JField field(String variable, CPath path) {
    for (FieldDeclaration declaration : fieldDeclarations) {
      if (declaration.path.equals(path) && variable.equals(declaration.field.name())) {
        return declaration.field;
      }
    }
    return null;
  }

  public JField accessibleField(String variable) {
    final var queriedPaths = new HashSet<>(path.everyClassInPath());
    return fieldDeclarations
      .stream()
      .filter(fd -> queriedPaths.contains(fd.path) && fd.field.name().equals(variable))
      .findAny()
      .map(fd -> fd.field)
      .orElse(null);
  }

  public List<VariableDeclaration> accessibleDeclarations() {
    final var queriedPaths = new HashSet<>(path.everyBlockLeadingToThisOne());
    if (path.isWithinMethod()) {
      queriedPaths.add(path.containingMethod());
    }
    return variableDeclarations
      .stream()
      .filter(d -> queriedPaths.contains(d.path))
      .collect(Collectors.toList());
  }

  public ParametersDeclaration methodParametersDeclaration(CPath path) {
    if (!path.last().isMethod()) {
      throw new TranslationException("Cannot lookup parameters declaration on non method path " + this.path);
    }
    return methodParametersDeclarations
      .stream()
      .filter(dp -> dp.path.equals(path))
      .findAny()
      .orElse(null);
  }

  public ParametersDeclaration lambdaParametersDeclaration(CPath path) {
    if (!path.last().isLambda()) {
      throw new TranslationException("Cannot lookup parameters declaration on non lambda path " + this.path);
    }
    return lambdaParametersDeclarations
      .stream()
      .filter(dp -> dp.path.equals(path))
      .findAny()
      .orElse(null);
  }

  public TContext deepen(CPath.Seg segment) {
    return new TContext(
      package0,
      imports,
      variableDeclarations,
      fieldDeclarations,
      methodParametersDeclarations,
      lambdaParametersDeclarations,
      path.add(segment)
    );
  }

  public CPath path() {
    return path;
  }
}
