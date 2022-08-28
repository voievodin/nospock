package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.AsCodePostProcessor;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

public class JConstantExpression implements JExpression {

  private final Object value;
  private boolean cannotBeLiteral;
  private boolean cannotBeTextBlock;

  public JConstantExpression(Object value) {
    this.value = value;
  }

  public boolean isNull() {
    return value == null;
  }

  public Object value() {
    return value;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (value instanceof String && !cannotBeLiteral) {
      final String str = (String) this.value;
      if (!cannotBeTextBlock && suitableForMultilineRepresentation(str)) {
        return asTextBlock(str);
      } else {
        return "\"" + escape((String) this.value) + "\"";
      }
    } else if (value != null && value.getClass() == Long.class) {
      return String.valueOf(value) + 'L';
    } else {
      return String.valueOf(value);
    }
  }

  public void setCannotBeLiteral() {
    this.cannotBeLiteral = true;
  }
  
  public void cannotBeTextBlock() {
    this.cannotBeTextBlock = true;
  }

  private static String escape(String source) {
    final var sb = new StringBuilder();
    for (int i = 0; i < source.length(); i++) {
      char c = source.charAt(i);
      if (c == '"' && (i == 0 || source.charAt(i - 1) != '\\')) {
        sb.append('\\').append(c);
      } else if (c == '\n') {
        sb.append("\\").append('n');
      } else if (c == '\t') {
        sb.append("\\").append('t');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  @Override
  public JType resolveType() {
    if (value != null) {
      return new JType(value.getClass());
    }
    return null;
  }

  private static boolean suitableForMultilineRepresentation(String str) {
    final String trimmed = str.trim();
    return trimmed.length() > 50 && trimmed.contains("\n");
  }

  private static String asTextBlock(String str) {
    final var sb = new StringBuilder("\"\"\"").append(AsCodePostProcessor.NL_PLACEHOLDER).append("\n");
    final String[] split = str.split("\n");
    for (int i = 0; i < split.length - 1; i++) {
      sb.append(split[i])
        .append(AsCodePostProcessor.NL_PLACEHOLDER)
        .append("\n");
    }
    return sb.append(split[split.length - 1]).append("\n\"\"\"").toString();
  }
}
