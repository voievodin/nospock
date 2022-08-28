package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.Objects;

public class JTernaryExpression implements JExpression {

  private final JExpression condition;
  private final JExpression ifTrue;
  private final JExpression ifFalse;

  public JTernaryExpression(JExpression condition, JExpression ifTrue, JExpression ifFalse) {
    this.condition = Objects.requireNonNull(condition);
    this.ifTrue = Objects.requireNonNull(ifTrue);
    this.ifFalse = Objects.requireNonNull(ifFalse);
  }

  @Override
  public String asCode(CodeStyle style) {
    return condition.asCode(style) + " ? " + ifTrue.asCode(style) + " : " + ifFalse.asCode(style);
  }
}
