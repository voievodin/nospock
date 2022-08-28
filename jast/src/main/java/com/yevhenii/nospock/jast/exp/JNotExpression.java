package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JNotExpression implements JExpression {

  private final JExpression expression;

  public JNotExpression(JExpression expression) {
    this.expression = Objects.requireNonNull(expression);
  }

  @Override
  public String asCode(CodeStyle style) {
    if (expression instanceof JBinaryExpression) {
      return "!(" + expression.asCode(style) + ")";
    } else {
      return "!" + expression.asCode(style);
    }
  }

  @Override
  public JType resolveType() {
    return JType.boxedBoolean();
  }
}
