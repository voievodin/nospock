package com.yevhenii.nospock.translator.spock.assertion;

import org.codehaus.groovy.ast.expr.Expression;

public class Assertion {

  public enum Operation {
    EQUALS,
    THROWS,
    IS_TRUE,
    IS_NOT_NULL,
    NOT_EQUALS,
    IS_NULL,
    IS_FALSE
  }

  public Assertion(Operation operation, Expression left, Expression right) {
    this.operation = operation;
    this.left = left;
    this.right = right;
  }

  private final Operation operation;
  private final Expression left;
  private final Expression right;

  public Operation getOperation() {
    return operation;
  }

  public Expression getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }
}
