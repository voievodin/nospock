package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JCastExpression implements JExpression {

  private final JType castType;
  private final JExpression expression;

  public JCastExpression(JType castType, JExpression expression) {
    this.castType = Objects.requireNonNull(castType);
    this.expression = Objects.requireNonNull(expression);
  }

  @Override
  public String asCode(CodeStyle style) {
    // has to be fixed e.g. casting method calls isn't correctly rendered
    return "((" + castType.asCode(style) + ") " + expression.asCode(style) + ")";
  }

  @Override
  public JType resolveType() {
    return castType;
  }
}
