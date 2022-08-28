package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JArrayExpression implements JExpression {

  private final JType type;
  private final List<JExpression> initExpressions;
  private final List<JExpression> sizeExpressions;

  public JArrayExpression(
    JType type,
    List<JExpression> initExpressions,
    List<JExpression> sizeExpressions
  ) {
    this.type = Objects.requireNonNull(type);
    this.initExpressions = new ArrayList<>(initExpressions);
    this.sizeExpressions = new ArrayList<>(sizeExpressions);
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("new ");

    // new int[2][2]
    if (!sizeExpressions.isEmpty()) {
      sb.append(elementTypeName(type));
      for (JExpression exp : sizeExpressions) {
        sb.append('[');
        if (!(exp instanceof JConstantExpression) || !((JConstantExpression) exp).isNull()) {
          sb.append(exp.asCode(style));
        }
        sb.append(']');
      }
    } else {
      sb.append(type.asCode(style));
    }

    // new int[] {1, 2, 3, 4, 5}
    if (!initExpressions.isEmpty()) {
      sb.append(" {")
        .append(
          initExpressions.stream()
            .map(ex -> ex.asCode(style))
            .collect(Collectors.joining(", "))
        )
        .append('}');
    }

    return sb.toString();
  }

  private static String elementTypeName(JType type) {
    final int idx = type.name().indexOf('[');
    if (idx > 0) {
      return type.name().substring(0, idx);
    } else {
      return type.name();
    }
  }

  @Override
  public JType resolveType() {
    return type;
  }
}
  