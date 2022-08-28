package com.yevhenii.nospock.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Points to the current translation path e.g. [ROOT]Root/[Class]MyClass/[Method]testMethod.
 * Always starts with ROOT.
 */
public class CPath {

  public static CPath ROOT = new CPath(new Seg[] {new Seg(Seg.Type.ROOT, "Root")});

  private final Seg[] path;

  private CPath(CPath cpPath, Seg segment) {
    this.path = new Seg[cpPath.path.length + 1];
    System.arraycopy(cpPath.path, 0, path, 0, cpPath.path.length);
    path[path.length - 1] = segment;
  }

  private CPath(Seg[] path) {
    this.path = path;
  }

  public CPath add(Seg segment) {
    return new CPath(this, segment);
  }

  public Seg last() {
    return path[path.length - 1];
  }

  public CPath withoutLast() {
    final var newPath = new Seg[path.length - 1];
    System.arraycopy(path, 0, newPath, 0, path.length - 1);
    return new CPath(newPath);
  }

  public CPath containingClass() {
    CPath p = this;
    while (!p.last().isClass()) {
      p = p.withoutLast();
    }
    return p;
  }

  public CPath containingMethod() {
    CPath p = this;
    while (!p.last().isMethod()) {
      p = p.withoutLast();
    }
    return p;
  }

  public CPath containingLambda() {
    CPath p = this;
    while (!p.last().isLambda()) {
      p = p.withoutLast();
    }
    return p;
  }

  public boolean isWithinMethod() {
    return isWithin(Seg::isMethod);
  }
  
  public boolean isWithinBlock() {
    return isWithin(Seg::isBlock);
  }

  public boolean isWithinLambda() {
    return isWithin(Seg::isLambda);
  }

  /**
   * For path {@code [ROOT]x/[CLASS]c/[METHOD]m/[BLOCK]1/[BLOCK]2}
   * returns list of path elements containing full path to each block, which is
   * <pre>@{code
   * [
   *   [ROOT]x/[CLASS]c/[METHOD]m/[BLOCK]1/[BLOCK]2,
   *   [ROOT]x/[CLASS]c/[METHOD]m/[BLOCK]1
   * ]
   * }</pre>
   */
  public List<CPath> everyBlockLeadingToThisOne() {
    final var chain = new ArrayList<CPath>();
    var p = this;
    while (p.last().isBlock()) {
      chain.add(p);
      p = p.withoutLast();
    }
    return chain;
  }

  /**
   * For path {@code [ROOT]x/[CLASS]a/[Class]b
   * returns list of path elements containing full path to each block, which is
   * <pre>@{code
   * [
   *   [ROOT]x/[CLASS]a/[CLASS]b
   *   [ROOT]x/[CLASS]a
   * ]
   * }</pre>
   */
  public List<CPath> everyClassInPath() {
    final var result = new ArrayList<CPath>();
    var p = this;
    while (!p.last().isRoot()) {
      if (p.last().isClass()) {
        result.add(p);
      }
      p = p.withoutLast();
    }
    return result;
  }

  public boolean isWithinClass() {
    return isWithin(Seg::isClass);
  }

  private boolean isWithin(Predicate<Seg> predicate) {
    for (Seg seg : path) {
      if (predicate.test(seg)) {
        return true;
      }
    }
    return false;
  }
  
  public static class Seg {

    public static Seg forLambda(String name) {
      return new Seg(Type.LAMBDA, name);
    }

    public static Seg forClass(String name) {
      return new Seg(Type.CLASS, name);
    }

    public static Seg forMethod(String name) {
      return new Seg(Type.METHOD, name);
    }

    public static Seg forBlock(String name) {
      return new Seg(Type.BLOCK, name);
    }

    public boolean isClass() {
      return type == Type.CLASS;
    }

    public boolean isBlock() {
      return type == Type.BLOCK;
    }

    public boolean isMethod() {
      return type == Type.METHOD;
    }

    public boolean isRoot() {
      return type == Type.ROOT;
    }

    public boolean isLambda() {
      return type == Type.LAMBDA;
    }

    public enum Type {
      ROOT, CLASS, METHOD, BLOCK, LAMBDA
    }

    private final Type type;
    private final String name;

    public Seg(Type type, String name) {
      this.type = Objects.requireNonNull(type);
      this.name = Objects.requireNonNull(name);
    }

    public String name() {
      return name;
    }

    public Type type() {
      return type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Seg segment = (Seg) o;
      return type == segment.type && Objects.equals(name, segment.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, name);
    }

    @Override
    public String toString() {
      return "[" + type + "]" + name;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CPath cPath = (CPath) o;
    return Arrays.equals(path, cPath.path);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(path);
  }

  @Override
  public String toString() {
    return Arrays.stream(path).map(Seg::toString).collect(Collectors.joining("/"));
  }
}

