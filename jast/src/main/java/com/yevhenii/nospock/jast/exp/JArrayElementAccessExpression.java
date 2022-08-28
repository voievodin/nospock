package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JArrayElementAccessExpression implements JExpression {

  private final JExpression object;
  private final JExpression index;

  public JArrayElementAccessExpression(JExpression object, JExpression index) {
    this.object = Objects.requireNonNull(object);
    this.index = Objects.requireNonNull(index);
  }

  @Override
  public String asCode(CodeStyle style) {
    return object.asCode(style) + '[' + index.asCode(style) + ']';
  }
}
