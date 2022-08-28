package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.Objects;

public class JThrowStatement implements JStatement {

  private final JExpression expression;

  public JThrowStatement(JExpression expression) {
    this.expression = Objects.requireNonNull(expression);
  }

  public JAstNode expression() {
    return expression;
  }

  @Override
  public String asCode(CodeStyle style) {
    return "throw " + expression.asCode(style);
  }

  @Override
  public boolean endsWithSemicolon() {
    return true;
  }
}
