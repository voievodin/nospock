package com.yevhenii.nospock.translator.spock.mock;

import com.yevhenii.nospock.translator.TranslateHelper;

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MockDetector {

  private static final Map<String, MockType> MOCK_METHODS = Map.of(
    "Mock", MockType.MOCK,
    "Spy", MockType.SPY
  );

  public DetectedMockInitialization detectInitialization(MethodCallExpression expression) {
    final MockType mockType = MOCK_METHODS.get(expression.getMethodAsString());
    if (mockType == null) {
      return null;
    }
    final var arguments = (ArgumentListExpression) expression.getArguments();
    // Type x = Mock();
    if (arguments.getExpressions() == null || arguments.getExpressions().isEmpty()) {
      return new DetectedMockInitialization(mockType, "java.lang.Object");
    }
    final String className = TranslateHelper.classNameFromClassLiteralExpression(arguments.getExpression(0));
    if (arguments.getExpressions().size() == 1 || !(arguments.getExpression(1) instanceof ClosureExpression)) {
      // Mock(X)
      return new DetectedMockInitialization(mockType, className);
    } else {
      // Mock(X) { ... }
      return new DetectedMockInitialization(
        mockType,
        className,
        ((ClosureExpression) arguments.getExpression(1))
      );
    }
  }

  public List<DetectedMockInteraction> detectInteractions(Statement statement) {
    if (!(statement instanceof ExpressionStatement)) {
      return Collections.emptyList();
    }
    final var expressionStatement = ((ExpressionStatement) statement);
    if (!(expressionStatement.getExpression() instanceof BinaryExpression)) {
      return Collections.emptyList();
    }
    final var binary = ((BinaryExpression) expressionStatement.getExpression());

    // In case we only verify times called and don't mock result
    if (isVerificationBinary(binary)) {
      return List.of(
        new DetectedMockInteraction(
          DetectedMockInteraction.Operation.VERIFY_METHOD_CALLED,
          binary.getLeftExpression(),
          binary.getRightExpression()
        )
      );
    }
    
    if (!binary.getOperation().getText().equals(">>")) {
      return List.of();
    }

    // mock.call(_) >> 123
    if (!isVerificationBinary(binary.getLeftExpression())) {
      return List.of(
        new DetectedMockInteraction(
          DetectedMockInteraction.Operation.MOCK_RETURN_VALUE,
          binary.getLeftExpression(),
          binary.getRightExpression()
        )
      );
    }
    
    final var leftAsBinary = ((BinaryExpression) binary.getLeftExpression());

    // when we're certain that no mocking is needed we can have verification only
    // e.g. when parameters are not matched against real values and we have
    // explicit type declaration of closure arguments, while closure contains assertions only.
    //
    // 1 * mock.call(_) >> { Person x ->
    //   assert x.name == "Name"
    //   assert x.age == 21
    // }
    if (closureThatOnlyAssertsExplicitlyTypedParameter(binary.getRightExpression())
        && methodCallThatHasOnlyPlaceholderParameters(leftAsBinary.getRightExpression())) {
      return List.of(
        new DetectedMockInteraction(
          DetectedMockInteraction.Operation.VERIFY_METHOD_CALLED,
          leftAsBinary.getLeftExpression(),
          leftAsBinary.getRightExpression(),
          ((ClosureExpression) binary.getRightExpression())
        )
      );
    }

    // For complex structure like the one below
    // 
    // 1 * mock.call(xx) >> 123
    // 
    // we have 2 interactions
    // 
    // a) 1 * mock.call(xx) - verify that method is called
    // b) mock.call(xx) >> 123 - mock return value
    return List.of(
      new DetectedMockInteraction(
        DetectedMockInteraction.Operation.VERIFY_METHOD_CALLED,
        leftAsBinary.getLeftExpression(),
        leftAsBinary.getRightExpression()
      ),
      new DetectedMockInteraction(
        DetectedMockInteraction.Operation.MOCK_RETURN_VALUE,
        leftAsBinary.getRightExpression(),
        binary.getRightExpression()
      )
    );
  }

  private static boolean closureThatOnlyAssertsExplicitlyTypedParameter(Expression expression) {
    if (!(expression instanceof ClosureExpression)) {
      return false;
    }
    final var closure = ((ClosureExpression) expression);
    if (closure.getParameters() == null || closure.getParameters().length != 1) {
      return false;
    }
    final Parameter parameter = closure.getParameters()[0];
    if (parameter.getType().getUnresolvedName().equals("java.lang.Object")) {
      return false;
    }
    if (!(closure.getCode() instanceof BlockStatement)) {
      return false;
    }
    for (Statement statement : ((BlockStatement) closure.getCode()).getStatements()) {
      if (!(statement instanceof AssertStatement) && !(statement instanceof EmptyStatement)) {
        return false;
      }
    }
    return true;
  }

  private static boolean methodCallThatHasOnlyPlaceholderParameters(Expression expression) {
    if (!(expression instanceof MethodCallExpression)) {
      return false;
    }
    if (!(((MethodCallExpression) expression).getArguments() instanceof ArgumentListExpression)) {
      return false;
    }
    for (Expression argExp : ((ArgumentListExpression) ((MethodCallExpression) expression).getArguments())) {
      if (!isAnyArgument(argExp)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isAnyArgument(Expression expression) {
    if (expression instanceof SpreadExpression) {
      return true;
    }
    if (expression instanceof CastExpression) {
      return isAnyArgument(((CastExpression) expression).getExpression());
    }
    if (!(expression instanceof VariableExpression)) {
      return false;
    }
    return ((VariableExpression) expression).getName().equals("_");
  }

  private static boolean isVerificationBinary(Expression expression) {
    if (!(expression instanceof BinaryExpression)) {
      return false;
    } else {
      return ((BinaryExpression) expression).getOperation().getText().equals("*");
    }
  }
}
