package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.Objects;

public class JFieldAccessExpression implements JExpression {

  private final JExpression object;
  private final JExpression property;

  public JFieldAccessExpression(JExpression object, JExpression property) {
    this.object = Objects.requireNonNull(object);
    this.property = Objects.requireNonNull(property);
    // in case of Integer.MAX_VALUE, MAX_VALUE is a string constant expression
    // there is no difference between MAX_VALUE and "MAX_VALUE" from the
    // perspective of a constant expression, though here we know that
    // string literal cannot be used in the field access chain.
    if (property instanceof JConstantExpression) {
      ((JConstantExpression) property).setCannotBeLiteral();
    }
  }

  public JExpression object() {
    return object;
  }

  public JExpression property() {
    return property;
  }

  @Override
  public String asCode(CodeStyle style) {
    return object.asCode(style) + '.' + property.asCode(style);
  }
}
