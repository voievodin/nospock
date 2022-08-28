package com.yevhenii.nospock.jast;

import java.util.Objects;

public class JPackage implements JAstNode {

  private final String fqn;

  public JPackage(String fqn) {
    this.fqn = Objects.requireNonNull(fqn);
  }

  public String fqn() {
    return fqn;
  }

  public boolean isEmpty() {
    return fqn.isEmpty();
  }

  @Override
  public String asCode(CodeStyle style) {
    if (isEmpty()) {
      return "";
    } else {
      return "package " + fqn + ";";
    }
  }
}
