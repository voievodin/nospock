package com.yevhenii.nospock.jast;

import java.util.*;
import java.util.stream.Collectors;

public class JType implements JAstNode {

  public static JType object() {
    return OBJECT_TYPE;
  }

  public static JType boxedByte() {
    return BYTE_TYPE;
  }

  public static JType boxedShort() {
    return SHORT_TYPE;
  }

  public static JType boxedInt() {
    return INTEGER_TYPE;
  }

  public static JType boxedLong() {
    return LONG_TYPE;
  }

  public static JType boxedFloat() {
    return FLOAT_TYPE;
  }

  public static JType boxedDouble() {
    return DOUBLE_TYPE;
  }

  public static JType boxedBoolean() {
    return BOOLEAN_TYPE;
  }

  public static JType boxedChar() {
    return CHARACTER_TYPE;
  }

  public static JType boxedVoid() {
    return VOID_VALUE;
  }

  private static final JType OBJECT_TYPE = new JType(Object.class);
  private static final JType BYTE_TYPE = new JType(Byte.class);
  private static final JType SHORT_TYPE = new JType(Short.class);
  private static final JType INTEGER_TYPE = new JType(Integer.class);
  private static final JType LONG_TYPE = new JType(Long.class);
  private static final JType FLOAT_TYPE = new JType(Float.class);
  private static final JType DOUBLE_TYPE = new JType(Double.class);
  private static final JType BOOLEAN_TYPE = new JType(Boolean.class);
  private static final JType CHARACTER_TYPE = new JType(Character.class);
  private static final JType VOID_VALUE = new JType(Void.class);

  private static final Set<JType> PRIMITIVES = Set.of(
    new JType(byte.class),
    new JType(short.class),
    new JType(int.class),
    new JType(long.class),
    new JType(float.class),
    new JType(double.class),
    new JType(boolean.class),
    new JType(char.class),
    new JType(void.class)
  );

  private final Fqn fqn;
  private final List<JType> genericTypes;
  // for inner class name is the name including outer class, like Flow.Subscriber
  // or what's given if that's a raw name (e.g. set via string).
  private final String name;

  private boolean useGenerics;

  public JType(Class<?> c) {
    // java.lang.String; java.util.concurrent.Flow$Subscriber
    final var tmpFqn = new Fqn(c.getName());
    this.name = tmpFqn.last().asString().replaceAll("\\$", ".");
    this.fqn = tmpFqn.withoutLastOrEmpty().add(new Fqn(name));
    this.genericTypes = Collections.emptyList(); // todo
  }

  public JType(String name) {
    this.name = name;
    this.fqn = new Fqn(name);
    this.genericTypes = new ArrayList<>();
  }

  public JType(Fqn fqn, String name, List<JType> genericTypes, boolean useGenerics) {
    this.fqn = fqn;
    this.name = name;
    this.genericTypes = new ArrayList<>(genericTypes);
    this.useGenerics = useGenerics;
  }

  public JType(Fqn fqn) {
    this.fqn = fqn;
    this.name = fqn.asString();
    this.genericTypes = new ArrayList<>();
  }

  public JType(String type, List<String> generics) {
    this.fqn = new Fqn(type);
    this.name = type;
    this.genericTypes = generics.stream().map(JType::new).collect(Collectors.toList());
    this.useGenerics = true;
  }

  public Fqn fqn() {
    return fqn;
  }

  public void addGeneric(JType generic) {
    this.genericTypes.add(generic);
  }

  public boolean isPrimitive() {
    return PRIMITIVES.contains(this);
  }

  public String name() {
    return name;
  }

  public List<JType> generics() {
    return genericTypes;
  }

  public JType box() {
    switch (fqn.asString()) {
      case "byte":
        return JType.boxedByte();
      case "short":
        return JType.boxedShort();
      case "int":
        return JType.boxedInt();
      case "long":
        return JType.boxedLong();
      case "float":
        return JType.boxedFloat();
      case "double":
        return JType.boxedDouble();
      case "boolean":
        return JType.boxedBoolean();
      case "char":
        return JType.boxedChar();
      case "void":
        return JType.boxedVoid();
      default:
        throw new IllegalStateException("Cannot box type " + fqn);
    }
  }

  public boolean isInt() {
    return boxIfPrimitive().equals(INTEGER_TYPE);
  }

  public boolean isLong() {
    return boxIfPrimitive().equals(LONG_TYPE);
  }

  public boolean isShort() {
    return boxIfPrimitive().equals(SHORT_TYPE);
  }

  public boolean isByte() {
    return boxIfPrimitive().equals(BYTE_TYPE);
  }

  public boolean isBoolean() {
    return boxIfPrimitive().equals(BOOLEAN_TYPE);
  }

  public boolean isChar() {
    return boxIfPrimitive().equals(CHARACTER_TYPE);
  }

  public boolean isFloat() {
    return boxIfPrimitive().equals(FLOAT_TYPE);
  }

  public boolean isDouble() {
    return boxIfPrimitive().equals(DOUBLE_TYPE);
  }

  public boolean isVoid() {
    return boxIfPrimitive().equals(VOID_VALUE);
  }

  public JType boxIfPrimitive() {
    if (isPrimitive()) {
      return box();
    } else {
      return this;
    }
  }

  /**
   * Ensures generics are used in case of the diamond operator.
   */
  public void useGenerics(boolean useGenerics) {
    this.useGenerics = useGenerics;
  }

  public boolean useGenerics() {
    return useGenerics;
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder(name);
    if (useGenerics || !genericTypes.isEmpty()) {
      sb.append('<');
      Iterator<JType> iterator = genericTypes.iterator();
      while (iterator.hasNext()) {
        sb.append(iterator.next().asCode(style));
        if (iterator.hasNext()) {
          sb.append(", ");
        }
      }
      sb.append('>');
    }
    return sb.toString();
  }

  public boolean isJavaLangObject() {
    return "java.lang.Object".equals(fqn.asString());
  }

  public boolean isJavaLang() {
    return fqn.asString().startsWith("java.lang.");
  }

  public boolean isInPackage(JPackage package0) {
    if (package0 == null) {
      return fqn.asString().equals(name);
    }
    return namespace().asString().equals(package0.fqn());
  }

  public Fqn namespace() {
    final String fqnStr = fqn.asString();
    if (fqnStr.length() == name.length()) {
      return Fqn.EMTPTY;
    } else {
      return new Fqn(fqnStr.substring(0, fqnStr.length() - name.length() - 1));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JType jType = (JType) o;
    return Objects.equals(fqn, jType.fqn) && Objects.equals(genericTypes, jType.genericTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fqn, genericTypes);
  }

  public JType withoutGenerics() {
    return new JType(fqn);
  }

  @Override
  public String toString() {
    return fqn.toString();
  }
}
