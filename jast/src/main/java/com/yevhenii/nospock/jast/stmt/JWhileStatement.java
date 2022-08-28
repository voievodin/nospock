package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;

import java.util.List;
import java.util.Objects;

public class JWhileStatement implements JStatement {

  private final JAstNode condition;
  private final JStatement block;

  public JWhileStatement(JAstNode condition, JStatement block) {
    this.condition = Objects.requireNonNull(condition);
    this.block = Objects.requireNonNull(block);
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("while (")
      .append(condition.asCode(style))
      .append(")");
    CodeHelper.appendStatementsBlock(sb, block, style);
    return sb.append(style.nlSequence()).toString();
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<? extends JStatement> getNestedStatements() {
    return List.of(block);
  }
}
