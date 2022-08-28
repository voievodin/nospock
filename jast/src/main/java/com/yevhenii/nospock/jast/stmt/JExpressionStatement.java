package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JCommentExpression;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.Objects;

public class JExpressionStatement implements JStatement {

  private final JExpression expression;

  public JExpressionStatement(JExpression expression) {
    this.expression = Objects.requireNonNull(expression);
  }

  @Override
  public String asCode(CodeStyle style) {
    return expression.asCode(style);
  }

  @Override
  public boolean endsWithSemicolon() {
    return !(expression instanceof JCommentExpression);
  }
}
