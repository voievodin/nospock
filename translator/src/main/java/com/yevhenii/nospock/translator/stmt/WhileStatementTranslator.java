package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JWhileStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.WhileStatement;

public class WhileStatementTranslator implements StatementTranslator<WhileStatement, JWhileStatement> {

  private final ExPool exPool;
  private final StPool stPool;

  public WhileStatementTranslator(ExPool exPool, StPool stPool) {
    this.exPool = exPool;
    this.stPool = stPool;
  }

  @Override
  public JWhileStatement translate(WhileStatement node, TContext context) {
    return new JWhileStatement(
      exPool.translate(node.getBooleanExpression(), context),
      stPool.translate(node.getLoopBlock(), context)
    );
  }

  @Override
  public Class<WhileStatement> getTranslatedType() {
    return WhileStatement.class;
  }
}
