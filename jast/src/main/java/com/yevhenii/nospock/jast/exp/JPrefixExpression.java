package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.Objects;

public class JPrefixExpression implements JExpression {

  private final JExpression expression;
  private final String operation;

  public JPrefixExpression(JExpression expression, String operation) {
    this.expression = Objects.requireNonNull(expression);
    this.operation = Objects.requireNonNull(operation);
  }

  @Override
  public String asCode(CodeStyle style) {
    return operation + expression.asCode(style);
  }
}
