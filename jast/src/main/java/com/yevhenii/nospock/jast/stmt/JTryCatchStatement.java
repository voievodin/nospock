package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JTryCatchStatement implements JStatement {

  private final JStatement tryBlock;
  private final List<JCatchStatement> catches = new ArrayList<>();

  private JStatement finallyBlock;

  public JTryCatchStatement(JStatement tryBlock) {
    this.tryBlock = Objects.requireNonNull(tryBlock);
  }

  public void setFinally(JStatement finallyBlock) {
    this.finallyBlock = finallyBlock;
  }

  public void addCatch(JCatchStatement catch0) {
    this.catches.add(catch0);
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("try ").append(tryBlock.asCode(style));
    for (JCatchStatement catchStatement : catches) {
      sb.append(" ").append(catchStatement.asCode(style));
    }
    if (finallyBlock != null && !(finallyBlock instanceof JEmptyStatement)) {
      sb.append(" finally ").append(finallyBlock.asCode(style));
    }
    return sb.append(style.nlSequence()).toString();
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<? extends JStatement> getNestedStatements() {
    final List<JStatement> statements = new ArrayList<>(catches);
    statements.add(tryBlock);
    if (finallyBlock != null) {
      statements.add(finallyBlock);
    }
    return statements;
  }
}
