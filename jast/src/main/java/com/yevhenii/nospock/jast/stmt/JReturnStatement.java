package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.Objects;

public class JReturnStatement implements JStatement {

  private final JExpression returnExpression;

  private boolean doesNotReturnValue;

  public JReturnStatement(JExpression result) {
    this.returnExpression = Objects.requireNonNull(result);
  }

  public void setDoesNotReturnNull() {
    this.doesNotReturnValue = true;
  }

  public JExpression returnExpression() {
    return returnExpression;
  }

  @Override
  public boolean endsWithSemicolon() {
    return true;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (doesNotReturnValue && returnExpression instanceof JConstantExpression
        && ((JConstantExpression) returnExpression).isNull()) {
      return "return";
    } else {
      return "return " + returnExpression.asCode(style);
    }
  }
}
