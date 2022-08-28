package com.yevhenii.nospock.jast;

import com.yevhenii.nospock.jast.stmt.JBlockStatement;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class JMethod implements JAstNode {

  private final String name;
  private final JType returnType;
  private final JBlockStatement block;
  private final List<JAnnotationUsage> annotations;
  private final List<JParameter> parameters;
  private final Set<JType> throws0 = new LinkedHashSet<>();

  private int modifiers;
  private boolean noAccessModifier = false;
  private boolean noAbstractModifier = false;

  public JMethod(
    String name,
    JType returnType,
    int modifiers,
    JBlockStatement block,
    List<JAnnotationUsage> annotations,
    List<JParameter> parameters
  ) {
    this.name = Objects.requireNonNull(name);
    this.returnType = Objects.requireNonNull(returnType);
    this.modifiers = modifiers;
    this.block = block;
    this.annotations = new ArrayList<>(annotations);
    this.parameters = new ArrayList<>(parameters);
  }

  public String name() {
    return name;
  }

  public JType returnType() {
    return returnType;
  }

  public int modifiers() {
    return modifiers;
  }

  public void modifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  public JBlockStatement block() {
    return block;
  }

  public List<JAnnotationUsage> annotations() {
    return annotations;
  }

  public List<JParameter> parameters() {
    return parameters;
  }

  public Set<JType> thrown() {
    return throws0;
  }

  public void setNoAccessModifier() {
    this.noAccessModifier = true;
  }

  public void setNoAbstractModifier() {
    this.noAbstractModifier = true;
  }

  public void annotate(JAnnotationUsage annotation) {
    if (!annotations.contains(annotation)) {
      annotations.add(annotation);
    }
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder();
    for (JAnnotationUsage annotation : annotations) {
      sb.append(annotation.asCode(style)).append(style.nlSequence());
    }

    CodeHelper.appendModifiers(sb, modifiersAsCode());

    // 'public Type '
    sb.append(returnType.asCode(style)).append(' ');

    // 'public Type x'
    sb.append(name);

    // 'public Type x(int a, String b)
    sb.append('(');
    sb.append(
      parameters.stream()
        .map(p -> p.asCode(style))
        .collect(Collectors.joining(", "))
    );
    sb.append(")");

    // public Type x(int a, String b) throws IOException
    if (!throws0.isEmpty()) {
      sb.append(" throws ").append(
        throws0.stream()
          .map(t -> t.asCode(style))
          .collect(Collectors.joining(", "))
      );
    }

    // 'public Type x(int a, String b) { ... }
    if (Modifier.isAbstract(modifiers)) {
      sb.append(';');
    } else {
      sb.append(' ').append(block.asCode(style));
    }

    return sb.append(style.nlSequence()).toString();
  }

  private int modifiersAsCode() {
    int tmpMod = modifiers;
    if (noAbstractModifier) {
      tmpMod = tmpMod & ~Modifier.ABSTRACT;
    }
    if (noAccessModifier) {
      tmpMod = tmpMod & ~(Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED);
    }
    return tmpMod;
  }
}
