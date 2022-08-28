package com.yevhenii.nospock.translator.spock.mock;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;

public class DetectedMockInteraction {

  public enum Operation {
    MOCK_RETURN_VALUE,
    VERIFY_METHOD_CALLED
  }

  private final Operation operation;
  private final Expression left;
  private final Expression right;
  private final ClosureExpression verificationClosure;

  public DetectedMockInteraction(Operation operation, Expression left, Expression right) {
    this(operation, left, right, null);
  }

  public DetectedMockInteraction(
    Operation operation,
    Expression left,
    Expression right,
    ClosureExpression verificationClosure
  ) {
    this.operation = operation;
    this.left = left;
    this.right = right;
    this.verificationClosure = verificationClosure;
  }

  public Operation operation() {
    return operation;
  }

  public Expression left() {
    return left;
  }

  public Expression right() {
    return right;
  }

  public ClosureExpression verificationClosure() {
    return verificationClosure;
  }

  @Override
  public String toString() {
    return "MockInteraction{" +
           "operation=" + operation +
           ", left=" + left +
           ", right=" + right +
           '}';
  }
}
