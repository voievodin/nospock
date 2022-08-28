package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.Objects;

public class JPostfixExpression implements JExpression {

  private final JExpression expression;
  private final String operation;

  public JPostfixExpression(JExpression expression, String operation) {
    this.expression = Objects.requireNonNull(expression);
    this.operation = Objects.requireNonNull(operation);
  }

  @Override
  public String asCode(CodeStyle style) {
    return expression.asCode(style) + operation;
  }
}
