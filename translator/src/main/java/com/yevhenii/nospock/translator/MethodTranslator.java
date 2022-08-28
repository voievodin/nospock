package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.*;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.*;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;

import java.util.*;

public class MethodTranslator implements Translator<MethodNode, JMethod> {

  private final Translator<ClassNode, JType> typeTranslator;
  private final Translator<BlockStatement, JBlockStatement> blockStatementTranslator;
  private final Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator;
  private final Translator<Parameter, JParameter> parameterTranslator;
  private final ExPool exPool;
  private final TypeLoader typeLoader;

  public MethodTranslator(
    Translator<ClassNode, JType> typeTranslator,
    Translator<BlockStatement, JBlockStatement> blockStatementTranslator,
    Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator,
    Translator<Parameter, JParameter> parameterTranslator,
    ExPool exPool,
    TypeLoader typeLoader
  ) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.blockStatementTranslator = Objects.requireNonNull(blockStatementTranslator);
    this.annotationUsageTranslator = Objects.requireNonNull(annotationUsageTranslator);
    this.parameterTranslator = Objects.requireNonNull(parameterTranslator);
    this.exPool = Objects.requireNonNull(exPool);
    this.typeLoader = Objects.requireNonNull(typeLoader);
  }

  @Override
  public JMethod translate(MethodNode node, TContext context) {
    context = context.deepen(CPath.Seg.forMethod(node.getName()));

    final var translatedParameters = TranslateHelper.translate(Arrays.asList(node.getParameters()), parameterTranslator, context);
    context.declareMethodParameters(translatedParameters, context.path());

    final var block = blockStatementTranslator.translate((BlockStatement) node.getCode(), context);
    var returnType = typeTranslator.translate(node.getReturnType(), context);

    // In case of void return type we need to make sure that
    // none of return statements returns nulls. This is needed as
    // I don't know how to differentiate between 'return null' and 'return'
    // (maybe looking at source indexes will help).
    final List<JReturnStatement> statements = TranslateHelper.findNestedStatementsOfType(block, JReturnStatement.class);
    if (returnType.isVoid()) {
      for (JReturnStatement statement : statements) {
        statement.setDoesNotReturnNull();
      }
    }

    // In case of dynamic return type we also need to walk
    // through the return statements to check return values
    // to make the best assumption about the actual return type.
    if (node.isDynamicReturnType()) {
      if (allReturnsReturnNull(statements)) {
        returnType = new JType("void");
      } else {
        returnType = new JType("Object");
      }
    }

    final var method = new JMethod(
      TranslateHelper.asCamelCaseName(node.getName()),
      returnType,
      node.getModifiers(),
      block,
      TranslateHelper.translate(node.getAnnotations(), annotationUsageTranslator, context),
      translatedParameters
    );

    enqueueTranslationOfDefaultParameterMethods(method, node.getParameters(), context);

    // it's not mandatory to define exceptions in groovy, but maybe
    // someone was kind enough to simplify derivation for us.
    if (node.getExceptions() != null) {
      for (ClassNode exception : node.getExceptions()) {
        method.thrown().add(typeTranslator.translate(exception, context));
      }
    }

    // otherwise look for throws statement within the code and try to
    // derive the exception type from it and if successful add it to the list.
    final var throwStatements = TranslateHelper.findNestedStatementsOfType(block, JThrowStatement.class);
    for (var thr : throwStatements) {
      if (thr.expression() instanceof JConstructorCallExpression) {
        final var type = ((JConstructorCallExpression) thr.expression()).type();
        final Class<?> c = typeLoader.tryLoad(type.fqn().asString(), context);
        if (c != null && !RuntimeException.class.isAssignableFrom(c) && Exception.class.isAssignableFrom(c)) {
          method.thrown().add(type);
        }
      }
    }

    return method;
  }

  /**
   * For each method that has parameter with default value
   * we need to generate a clone that delegates to method with more parameters
   * passing the default value to it, for example:
   * <p>
   * 'void method(int a, int b = 3) { ... }' will be translated to
   * <p>
   * 'void method(int a, int b) { ... }'
   * 'void method(int a) { method(a, 3); }'
   */
  private void enqueueTranslationOfDefaultParameterMethods(JMethod method, Parameter[] parameters, TContext context) {
    for (int i = parameters.length - 1; i > 0; i--) {
      Parameter param = parameters[i];
      if (param.getDefaultValue() == null) {
        break;
      }

      final var callExp = new JMethodCallExpression(
        new JVariableExpression("this"),
        method.name()
      );
      callExp.useThis(false);

      // generating a call that includes all arguments before a given one
      // e.g. for method(int a, int b = 1, int c = 3) and param = 3
      // here we generate call method(a, b)
      for (int j = 0; j < i; j++) {
        callExp.addArgument(new JVariableExpression(parameters[j].getName()));
      }
      // and here we add default value method(a, b, 3)
      callExp.addArgument(exPool.translate(param.getDefaultValue(), context));

      JBlockStatement selfCallBlock;
      if (method.returnType().isVoid()) {
        selfCallBlock = new JBlockStatement(new JExpressionStatement(callExp));
      } else {
        selfCallBlock = new JBlockStatement(new JReturnStatement(callExp));
      }

      final int fi = i;
      TransformationsQueue.instance().enqueue(
        TransformationsQueue.Target.CLASS,
        context.path().containingClass(),
        TransformationsQueue.Priority.IN_THE_END,
        jAstNode -> {
          final JClass containingClass = (JClass) jAstNode;
          containingClass.addMethod(
            new JMethod(
              method.name(),
              method.returnType(),
              method.modifiers(),
              selfCallBlock,
              method.annotations(),
              method.parameters().subList(0, fi)
            )
          );
        }
      );
    }
  }

  private boolean allReturnsReturnNull(List<JReturnStatement> statements) {
    for (JReturnStatement statement : statements) {
      if (!(statement.returnExpression() instanceof JConstantExpression)) {
        return false;
      }
      if (!((JConstantExpression) statement.returnExpression()).isNull()) {
        return false;
      }
    }
    return true;
  }
}
