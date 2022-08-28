package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;

import java.util.List;
import java.util.Objects;

public class JIfStatement implements JStatement {

  private final JAstNode ifExpression;
  private final JStatement ifBlock;
  private final JStatement elseBlock;

  public JIfStatement(JAstNode ifExpression, JStatement ifBlock, JStatement elseBlock) {
    this.ifExpression = Objects.requireNonNull(ifExpression);
    this.ifBlock = Objects.requireNonNull(ifBlock);
    this.elseBlock = elseBlock;
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder("if (")
      .append(ifExpression.asCode(style))
      .append(") ")
      .append(ifBlock.asCode(style));

    // in case of 'if (condition) return 123' or similar
    if (!(ifBlock instanceof JBlockStatement)) {
      sb.append(';');
    }

    if (elseBlock == null || elseBlock instanceof JEmptyStatement) {
      sb.append(style.nlSequence());
    } else if (elseBlock instanceof JBlockStatement) {
      sb.append(" else ").append(elseBlock.asCode(style)).append(style.nlSequence());
    } else if (elseBlock instanceof JIfStatement) {
      sb.append(" else ").append(elseBlock.asCode(style));
    } else {
      throw new IllegalStateException("Not supported else statement in else block " + elseBlock);
    }

    return sb.toString();
  }

  @Override
  public List<JStatement> getNestedStatements() {
    return List.of(ifBlock, elseBlock);
  }
}
