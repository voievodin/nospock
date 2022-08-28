package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JClassLiteralExpression implements JExpression {

  private final JType type;

  private final Formatting formatting = new Formatting();

  public JClassLiteralExpression(JType type) {
    this.type = Objects.requireNonNull(type);
  }

  public Formatting formatting() {
    return formatting;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (formatting.useLiteral) {
      return type.asCode(style) + ".class";
    } else {
      return type.asCode(style);
    }
  }

  @Override
  public JType resolveType() {
    return new JType(Class.class);
  }

  public static class Formatting {
    boolean useLiteral = true;

    public void doNotUseLiteral() {
      useLiteral = false;
    }
  }
}
