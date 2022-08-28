package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.List;
import java.util.Objects;

public class JCaseStatement implements JStatement {

  private final JExpression expression;
  private final JStatement block;

  public JCaseStatement(JExpression expression, JStatement block) {
    this.expression = Objects.requireNonNull(expression);
    this.block = Objects.requireNonNull(block);
  }

  @Override
  public String asCode(CodeStyle style) {
    return "case " + expression.asCode(style) + ": " + block.asCode(style) + style.nlSequence();
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<JStatement> getNestedStatements() {
    return List.of(block);
  }
}
