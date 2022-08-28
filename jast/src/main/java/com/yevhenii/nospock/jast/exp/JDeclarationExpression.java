package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JDeclarationExpression implements JExpression {

  private final JExpression left;
  private final JExpression right;

  public JDeclarationExpression(JExpression left, JExpression right) {
    this.left = Objects.requireNonNull(left);
    this.right = right;
  }

  public JExpression left() {
    return left;
  }

  public JExpression right() {
    return right;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (right == null) {
      return left.asCode(style);
    } else {
      return left.asCode(style) + " = " + right.asCode(style);
    }
  }
}
