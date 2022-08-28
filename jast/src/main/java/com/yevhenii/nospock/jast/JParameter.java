package com.yevhenii.nospock.jast;

import java.util.Objects;

public class JParameter implements JAstNode {

  private final JType type;
  private final String name;

  private final Formatting formatting = new Formatting();

  public JParameter(JType type, String name) {
    this.type = Objects.requireNonNull(type);
    this.name = Objects.requireNonNull(name);
  }

  public JType type() {
    return type;
  }

  public String name() {
    return name;
  }

  public Formatting formatting() {
    return formatting;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (formatting.omitType) {
      return name;
    } else {
      return type.asCode(style) + " " + name;
    }
  }

  public static class Formatting {
    private boolean omitType;

    public void omitType() {
      this.omitType = true;
    }
  }
}
