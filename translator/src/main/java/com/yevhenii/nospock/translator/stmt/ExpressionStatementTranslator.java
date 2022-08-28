package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.ExpressionStatement;

public class ExpressionStatementTranslator implements StatementTranslator<ExpressionStatement, JExpressionStatement> {

  private final ExPool exPool;

  public ExpressionStatementTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public JExpressionStatement translate(ExpressionStatement node, TContext context) {
    return new JExpressionStatement(exPool.translate(node.getExpression(), context));
  }

  @Override
  public Class<ExpressionStatement> getTranslatedType() {
    return ExpressionStatement.class;
  }
}
