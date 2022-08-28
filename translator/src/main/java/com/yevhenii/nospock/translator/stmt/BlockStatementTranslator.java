package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.translator.BlockTranslation;
import com.yevhenii.nospock.translator.CPath;
import com.yevhenii.nospock.translator.IdSequence;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Objects;

public class BlockStatementTranslator implements StatementTranslator<BlockStatement, JBlockStatement> {

  private final StPool stPool;

  public BlockStatementTranslator(StPool stPool) {
    this.stPool = Objects.requireNonNull(stPool);
  }

  @Override
  public JBlockStatement translate(BlockStatement node, TContext context) {
    context = context.deepen(CPath.Seg.forBlock(Integer.toString(IdSequence.next())));
    final var block = new BlockTranslation();
    if (node != null) {
      for (Statement statement : node.getStatements()) {
        block.add(stPool.translate(statement, context), context);
      }
    }
    return block.end(context);
  }

  @Override
  public Class<BlockStatement> getTranslatedType() {
    return BlockStatement.class;
  }
}
