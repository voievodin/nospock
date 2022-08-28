package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JReturnStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.ReturnStatement;

public class ReturnStatementTranslator implements StatementTranslator<ReturnStatement, JReturnStatement> {

  private final ExPool exPool;

  public ReturnStatementTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public JReturnStatement translate(ReturnStatement node, TContext context) {
    return new JReturnStatement(exPool.translate(node.getExpression(), context));
  }

  @Override
  public Class<ReturnStatement> getTranslatedType() {
    return ReturnStatement.class;
  }
}
