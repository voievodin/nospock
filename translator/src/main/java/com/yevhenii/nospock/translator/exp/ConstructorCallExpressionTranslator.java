package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.translator.*;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConstructorCallExpressionTranslator implements ExpressionTranslator<ConstructorCallExpression, JConstructorCallExpression> {

  private final Translator<ClassNode, JType> typeTranslator;
  private final Translator<ClassNode, JClass> classTranslator;
  private final ExPool exPool;

  public ConstructorCallExpressionTranslator(
    Translator<ClassNode, JType> typeTranslator,
    Translator<ClassNode, JClass> classTranslator,
    ExPool exPool
  ) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.classTranslator = Objects.requireNonNull(classTranslator);
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<ConstructorCallExpression> getTranslatedType() {
    return ConstructorCallExpression.class;
  }

  @Override
  public JConstructorCallExpression translate(ConstructorCallExpression exp, TContext context) {
    final JConstructorCallExpression result;
    if (isRegularConstructor(exp)) {
      result = regularConstructorCall(exp, context);
    } else {
      // this is not very nice as it will end up creating new classes,
      // however, at this point it's quite simple construct to use
      // if it's a problem in future we can switch to some smarter algo, for example,
      // we can generate a method that creates an instance of the class, but for this
      // to work well we need to analyse types of the parameters/fields etc
      result = anonymousConstructionCallingSetters(exp, context);
    }
    GroovyImplicitImports.explicitlyImport(result.type());
    return result;
  }

  // like new X(1, "hello)
  private JConstructorCallExpression regularConstructorCall(ConstructorCallExpression exp, TContext context) {
    final JConstructorCallExpression result;
    if (exp.getType().getUnresolvedName().contains("$")) {
      result = new JConstructorCallExpression(typeTranslator.translate(exp.getType().getSuperClass(), context));
      JClass anonType = classTranslator.translate(exp.getType(), context);
      anonType.setAnonymous(true);
      result.anonymousType(anonType);
    } else {
      result = new JConstructorCallExpression(typeTranslator.translate(exp.getType(), context));
    }
    for (Expression argExp : (ArgumentListExpression) exp.getArguments()) {
      JExpression argTranslated = exPool.translate(argExp, context);
      if (argTranslated instanceof ConstructorCallInitializedWithSetters) {
        final ConstructorCallInitializedWithSetters constructorWithSetters = (ConstructorCallInitializedWithSetters) argTranslated;
        final ConstructorToVariableExtractor extractor = new ConstructorToVariableExtractor(constructorWithSetters);
        argTranslated = extractor.extractBeforeCurrentStatement(context);
      }
      result.addArgument(argTranslated);
    }
    return result;
  }

  /**
   * Like:
   * <pre>
   * new X() {
   *   {
   *     setP1(1);
   *     setP2(2);
   *   }
   * }
   * </pre>
   */
  private JConstructorCallExpression anonymousConstructionCallingSetters(ConstructorCallExpression exp, TContext context) {
    return new ConstructorCallInitializedWithSetters(
      typeTranslator.translate(exp.getType(), context),
      mapEntryExpressions(exp)
        .stream()
        .map(entry -> asSetterCall(entry, context))
        .collect(Collectors.toList())
    );
  }

  private static boolean isRegularConstructor(ConstructorCallExpression exp) {
    return exp.getArguments() instanceof ArgumentListExpression;
  }

  private List<MapEntryExpression> mapEntryExpressions(ConstructorCallExpression exp) {
    if (!(exp.getArguments() instanceof TupleExpression)) {
      throw new RuntimeException("Expected arguments to be TupleExpression " + exp.getArguments());
    }
    final var tExp = (TupleExpression) exp.getArguments();
    if (tExp.getExpressions().size() != 1) {
      throw new RuntimeException("Expected exactly 1 expression in the tuple " + tExp);
    }
    if (!(tExp.getExpression(0) instanceof NamedArgumentListExpression)) {
      throw new RuntimeException("Expected the expression in the tuple to be NamedArgumentListExpression " + tExp.getExpression(0));
    }
    return ((NamedArgumentListExpression) tExp.getExpression(0)).getMapEntryExpressions();
  }

  private JMethodCallExpression asSetterCall(MapEntryExpression meExp, TContext context) {
    final var call =
      new JMethodCallExpression(JVariableExpression.this0(), TranslateHelper.setterForField(meExp.getKeyExpression().getText()));
    call.useThis(false);
    call.addArgument(exPool.translate(meExp.getValueExpression(), context));
    return call;
  }
}
