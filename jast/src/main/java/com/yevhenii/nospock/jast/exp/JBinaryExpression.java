package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;
import java.util.Set;

public class JBinaryExpression implements JExpression {

  private static final Set<String> BOOLEAN_OP = Set.of(
    "&&",
    "||",
    "==",
    "!=",
    ">",
    "<",
    ">=",
    "<="
  );

  private final Formatting formatting = new Formatting();

  private final JExpression left;
  private final JExpression right;
  private final String operation;

  public JBinaryExpression(JExpression left, JExpression right, String operation) {
    this.left = Objects.requireNonNull(left);
    this.right = Objects.requireNonNull(right);
    this.operation = Objects.requireNonNull(operation);
  }

  public JExpression left() {
    return left;
  }

  public JExpression right() {
    return right;
  }

  public String operation() {
    return operation;
  }

  public Formatting formatting() {
    return formatting;
  }

  @Override
  public String asCode(CodeStyle style) {
    final String code = asCode0(style);
    if (formatting.alwaysWrapInBraces) {
      return '(' + code + ')';
    } else {
      return code;
    }
  }

  private String asCode0(CodeStyle style) {
    // do not explicitly use class literal when using instanceof
    if (right instanceof JClassLiteralExpression) {
      ((JClassLiteralExpression) right).formatting().doNotUseLiteral();
    }

    final var leftCode = left.asCode(style);
    final var rightCode = right.asCode(style);

    if (!BOOLEAN_OP.contains(operation)) {
      return leftCode + ' ' + operation + ' ' + rightCode;
    }

    if (isNonBinary(left) && isNonBinary(right)) {
      return leftCode + ' ' + operation + ' ' + rightCode;
    } else if (isNonBinary(left)) {
      return leftCode + ' ' + operation + " (" + rightCode + ")";
    } else if (isNonBinary(right)) {
      return '(' + leftCode + ") " + operation + ' ' + rightCode;
    } else {
      return '(' + leftCode + ") " + operation + " (" + rightCode + ')';
    }
  }

  private boolean isNonBinary(JExpression expression) {
    return !(expression instanceof JBinaryExpression);
  }

  public static class Formatting {
    private boolean alwaysWrapInBraces;

    public Formatting alwaysWrapInBraces(boolean alwaysWrapInBraces) {
      this.alwaysWrapInBraces = alwaysWrapInBraces;
      return this;
    }
  }
}
