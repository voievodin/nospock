package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.Objects;

public class JMethodReferenceExpression implements JExpression {

  private final JExpression object;
  private final String method;

  public JMethodReferenceExpression(JExpression object, String method) {
    this.object = Objects.requireNonNull(object);
    this.method = Objects.requireNonNull(method);
  }

  @Override
  public String asCode(CodeStyle style) {
    return object.asCode(style) + "::" + method;
  }
}
