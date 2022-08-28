package com.yevhenii.nospock.jast;


import com.yevhenii.nospock.jast.exp.JExpression;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JField implements JAstNode {

  private final String name;
  private final List<JAnnotationUsage> annotations = new ArrayList<>();

  private JType type;
  private int modifiers;
  private JExpression initExpression;
  private boolean isEnumValue;

  public JField(JType type, String name) {
    this.type = Objects.requireNonNull(type);
    this.name = Objects.requireNonNull(name);
  }

  public JExpression initExpression() {
    return initExpression;
  }

  public void type(JType type) {
    this.type = Objects.requireNonNull(type);
  }

  public void initExpression(JExpression initExpression) {
    this.initExpression = initExpression;
  }

  public void modifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  public void addAnnotation(JAnnotationUsage annotationUsage) {
    this.annotations.add(annotationUsage);
  }

  public void setIsEnumValue() {
    this.isEnumValue = true;
  }

  public boolean isEnumValue() {
    return isEnumValue;
  }

  public String name() {
    return name;
  }

  public JType type() {
    return type;
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder();

    // annotations
    CodeHelper.appendEachNl(sb, annotations, style);

    // modifiers
    CodeHelper.appendModifiers(sb, modifiers);

    // Field f
    sb.append(type.asCode(style)).append(' ').append(name);

    // Field f = ...;
    if (initExpression != null) {
      sb.append(" = ").append(initExpression.asCode(style));
    }
    return sb.toString();
  }

  public boolean isStatic() {
    return Modifier.isStatic(modifiers);
  }
}
