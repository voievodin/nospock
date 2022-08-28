package com.yevhenii.nospock.jast;

import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JConstructor implements JAstNode {

  private final String name;
  private final int modifiers;
  private final JBlockStatement block = new JBlockStatement();
  private final List<JAnnotationUsage> annotations = new ArrayList<>();
  private final List<JParameter> parameters = new ArrayList<>();

  public JConstructor(String name, int modifiers) {
    this.name = Objects.requireNonNull(name);
    this.modifiers = modifiers;
  }

  public void addAnnotation(JAnnotationUsage annotation) {
    this.annotations.add(annotation);
  }

  public void addParameter(JParameter parameter) {
    this.parameters.add(parameter);
  }

  public void addStatement(JStatement statement) {
    this.block.statements().add(statement);
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder();
    for (JAnnotationUsage annotation : annotations) {
      sb.append(annotation.asCode(style)).append(style.nlSequence());
    }

    CodeHelper.appendAccessModifier(sb, modifiers);
    sb.append(' ');

    // 'public Type'
    sb.append(name);

    // 'public Type x(int a, String b)
    sb.append('(');
    sb.append(
      parameters.stream()
        .map(p -> p.asCode(style))
        .collect(Collectors.joining(", "))
    );
    return sb.append(")")
      .append(' ')
      .append(block.asCode(style))
      .append(style.nlSequence()).toString();
  }
}
