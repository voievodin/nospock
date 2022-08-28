package com.yevhenii.nospock.jast;

import java.util.Objects;

public class JImport implements JAstNode, Comparable<JImport> {

  private Fqn fqn;
  private final boolean isStatic;

  public JImport(String fqn, boolean isStatic) {
    this(new Fqn(fqn), isStatic);
  }

  public JImport(Fqn fqn, boolean isStatic) {
    this.fqn = Objects.requireNonNull(fqn);
    this.isStatic = isStatic;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public Fqn fqn() {
    return fqn;
  }
  
  public void fqn(Fqn fqn) {
    this.fqn = Objects.requireNonNull(fqn);
  }

  @Override
  public String asCode(CodeStyle style) {
    if (isStatic) {
      return "import static " + fqn + ";";
    } else {
      return "import " + fqn + ";";
    }
  }

  @Override
  public int compareTo(JImport o) {
    return fqn.compareTo(o.fqn);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JImport jImport = (JImport) o;
    return isStatic == jImport.isStatic && Objects.equals(fqn, jImport.fqn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fqn, isStatic);
  }

  @Override
  public String toString() {
    return "JImport{" +
           "fqn=" + fqn +
           ", isStatic=" + isStatic +
           '}';
  }
}
