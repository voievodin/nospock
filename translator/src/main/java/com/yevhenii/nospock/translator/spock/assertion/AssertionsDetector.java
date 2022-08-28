package com.yevhenii.nospock.translator.spock.assertion;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JTernaryExpression;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeCollector;
import com.yevhenii.nospock.translator.TypeDeriver;

import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;
import java.util.Objects;

public class AssertionsDetector {

  public static boolean isThrown(MethodCallExpression expression) {
    return "thrown".equals(expression.getMethodAsString());
  }

  public static boolean isThrown(DeclarationExpression expression) {
    return expression.getRightExpression() instanceof MethodCallExpression
           && isThrown(((MethodCallExpression) expression.getRightExpression()));
  }

  private final List<ExpressionHandler> handlers = List.of(
    new DeclarationExpressionHandler(),
    new BinaryExpressionHandler(),
    new MethodCallExpressionHandler(),
    new PropertyExpressionHandler(),
    new NotExpressionHandler(),
    new ArrayExpressionHandler(),
    new TernaryExpressionHandler(),
    new VariableExpressionHandler()
  );

  private final TypeLoader typeLoader;
  private final RuntimeLookup runtimeLookup;

  public AssertionsDetector(TypeLoader typeLoader, RuntimeLookup runtimeLookup) {
    this.typeLoader = Objects.requireNonNull(typeLoader);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  public Assertion detect(Statement statement, TContext context) {
    final Expression exp;
    if (statement instanceof ExpressionStatement) {
      exp = ((ExpressionStatement) statement).getExpression();
    } else if (statement instanceof AssertStatement) {
      exp = ((AssertStatement) statement).getBooleanExpression().getExpression();
    } else {
      return null;
    }
    return detectForExpression(exp, context);
  }

  private Assertion detectForExpression(Expression exp, TContext context) {
    for (ExpressionHandler handler : handlers) {
      if (handler.handles(exp)) {
        return handler.handle(exp, context);
      }
    }
    return null;
  }

  private interface ExpressionHandler {
    boolean handles(Expression expression);

    Assertion handle(Expression expression, TContext context);
  }

  private static class BinaryExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof BinaryExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      final BinaryExpression binaryExp = (BinaryExpression) expression;
      switch (binaryExp.getOperation().getText()) {
        case "==":
          if (isNullConstant(binaryExp.getLeftExpression())) {
            return new Assertion(
              Assertion.Operation.IS_NULL,
              binaryExp.getRightExpression(),
              null
            );
          } else if (isNullConstant(binaryExp.getRightExpression())) {
            return new Assertion(
              Assertion.Operation.IS_NULL,
              binaryExp.getLeftExpression(),
              null
            );
          } else {
            return new Assertion(
              Assertion.Operation.EQUALS,
              binaryExp.getLeftExpression(),
              binaryExp.getRightExpression()
            );
          }
        case "!=":
          if (isNullConstant(binaryExp.getLeftExpression())) {
            return new Assertion(
              Assertion.Operation.IS_NOT_NULL,
              binaryExp.getRightExpression(),
              null
            );
          } else if (isNullConstant(binaryExp.getRightExpression())) {
            return new Assertion(
              Assertion.Operation.IS_NOT_NULL,
              binaryExp.getLeftExpression(),
              null
            );
          } else {
            return new Assertion(
              Assertion.Operation.NOT_EQUALS,
              binaryExp.getLeftExpression(),
              binaryExp.getRightExpression()
            );
          }
        case "||":
        case "&&":
        case ">":
        case "<":
        case ">=":
        case "<=":
        case "instanceof":
          return new Assertion(
            Assertion.Operation.IS_TRUE,
            binaryExp,
            null
          );
        default:
          return null;
      }
    }
  }

  private class MethodCallExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof MethodCallExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      final var mCall = ((MethodCallExpression) expression);
      if (isThrown(mCall)) {
        final var arg0 = argument0(mCall);
        if (arg0 != null) {
          return new ThrowsAssertion(arg0);
        }
      }

      return assertionForExpressionResolvingType(
        expression,
        new TypeDeriver(typeLoader).derive(
          TypeCollector.collect(
            expression,
            runtimeLookup,
            context
          ),
          context
        )
      );
    }
  }

  private static class DeclarationExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof DeclarationExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      final var dExp = ((DeclarationExpression) expression);
      if (!(dExp.getRightExpression() instanceof MethodCallExpression) || !isThrown(((MethodCallExpression) dExp.getRightExpression()))) {
        return null;
      }
      final var arg0 = argument0(((MethodCallExpression) dExp.getRightExpression()));
      if (arg0 == null) {
        return null;
      }
      return new ThrowsAssertion(dExp.getLeftExpression(), arg0);
    }
  }

  private class PropertyExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof PropertyExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      return assertionForExpressionResolvingType(
        expression,
        new TypeDeriver(typeLoader).derive(
          TypeCollector.collect(
            expression,
            runtimeLookup,
            context
          ),
          context
        )
      );
    }
  }

  private class NotExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof NotExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      final NotExpression notExp = (NotExpression) expression;
      final Assertion detected = detectForExpression(notExp.getExpression(), context);
      if (detected != null) {
        switch (detected.getOperation()) {
          case IS_TRUE:
            return new Assertion(Assertion.Operation.IS_FALSE, detected.getLeft(), detected.getRight());
          case IS_FALSE:
            return new Assertion(Assertion.Operation.IS_TRUE, detected.getLeft(), detected.getRight());
          case IS_NULL:
            return new Assertion(Assertion.Operation.IS_NOT_NULL, detected.getLeft(), detected.getRight());
          case IS_NOT_NULL:
            return new Assertion(Assertion.Operation.IS_NULL, detected.getLeft(), detected.getRight());
        }
      }
      return new Assertion(Assertion.Operation.IS_TRUE, expression, null);
    }
  }

  private static class ArrayExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof ArrayExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      return new Assertion(
        Assertion.Operation.IS_NOT_NULL,
        expression,
        null
      );
    }
  }

  private static class TernaryExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof TernaryExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      return new Assertion(
        Assertion.Operation.IS_TRUE,
        expression,
        null
      );
    }
  }

  private class VariableExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handles(Expression expression) {
      return expression instanceof VariableExpression;
    }

    @Override
    public Assertion handle(Expression expression, TContext context) {
      return assertionForExpressionResolvingType(
        expression,
        new TypeDeriver(typeLoader).derive(
          TypeCollector.collect(
            expression,
            runtimeLookup,
            context
          ),
          context
        )
      );
    }
  }

  private static Assertion assertionForExpressionResolvingType(Expression expression, JType type) {
    if (type == null || type.isVoid()) {
      return null;
    } else if (type.isBoolean()) {
      return new Assertion(
        Assertion.Operation.IS_TRUE,
        expression,
        null
      );
    } else {
      return new Assertion(
        Assertion.Operation.IS_NOT_NULL,
        expression,
        null
      );
    }
  }

  private static Expression argument0(MethodCallExpression mCall) {
    if (!(mCall.getArguments() instanceof ArgumentListExpression)) {
      return null;

    }
    final var argList = ((ArgumentListExpression) mCall.getArguments());
    if (argList.getExpressions() == null || argList.getExpressions().size() != 1) {
      return null;
    }
    return argList.getExpression(0);
  }

  private static boolean isNullConstant(Expression expression) {
    if (!(expression instanceof ConstantExpression)) {
      return false;
    } else {
      return ((ConstantExpression) expression).getValue() == null;
    }
  }
}
