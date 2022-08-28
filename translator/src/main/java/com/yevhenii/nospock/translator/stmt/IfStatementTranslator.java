package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JIfStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.IfStatement;

public class IfStatementTranslator implements StatementTranslator<IfStatement, JIfStatement> {

  private final ExPool exPool;
  private final StPool stPool;

  public IfStatementTranslator(ExPool exPool, StPool stPool) {
    this.exPool = exPool;
    this.stPool = stPool;
  }

  @Override
  public JIfStatement translate(IfStatement node, TContext context) {
    return new JIfStatement(
      exPool.translate(node.getBooleanExpression(), context),
      stPool.translate(node.getIfBlock(), context),
      stPool.translate(node.getElseBlock(), context)
    );
  }

  @Override
  public Class<IfStatement> getTranslatedType() {
    return IfStatement.class;
  }
}
