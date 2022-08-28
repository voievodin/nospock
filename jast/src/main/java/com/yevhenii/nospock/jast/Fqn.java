package com.yevhenii.nospock.jast;

import java.util.Objects;

public class Fqn implements Comparable<Fqn> {

  public static final Fqn EMTPTY = new Fqn("");

  private final String fqn;

  public Fqn(String fqn) {
    this.fqn = fqn;
  }

  public String asString() {
    return fqn;
  }

  public Fqn last() {
    final var segments = segments();
    return new Fqn(segments[segments.length - 1]);
  }

  public Fqn first() {
    final String[] segments = segments();
    return new Fqn(segments[0]);
  }

  public Fqn withoutLast() {
    final var withoutLast = withoutLastOrEmpty();
    if (withoutLast.isEmpty()) {
      throw new IllegalStateException("No last segment to cut " + fqn);
    } else {
      return withoutLast;
    }
  }

  public boolean isEmpty() {
    return fqn.isEmpty();
  }

  public Fqn withoutLastOrEmpty() {
    final var idx = fqn.lastIndexOf('.');
    if (idx >= 0) {
      return new Fqn(fqn.substring(0, idx));
    } else {
      return Fqn.EMTPTY;
    }
  }

  public Fqn add(Fqn fqn) {
    if (isEmpty()) {
      return fqn;
    } else if (fqn.isEmpty()) {
      return this;
    } else {
      return new Fqn(this.fqn + '.' + fqn.fqn);
    }
  }

  public Fqn add(String fqn) {
    return add(new Fqn(fqn));
  }

  private String[] segments() {
    return fqn.split("\\.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Fqn fqn = (Fqn) o;
    return Objects.equals(this.fqn, fqn.fqn);
  }

  @Override
  public int hashCode() {
    return fqn.hashCode();
  }

  @Override
  public String toString() {
    return fqn;
  }

  @Override
  public int compareTo(Fqn o) {
    return fqn.compareTo(o.fqn);
  }

}
