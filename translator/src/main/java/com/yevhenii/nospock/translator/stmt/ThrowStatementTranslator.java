package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JThrowStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.ThrowStatement;

public class ThrowStatementTranslator implements StatementTranslator<ThrowStatement, JThrowStatement> {

  private final ExPool exPool;

  public ThrowStatementTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public JThrowStatement translate(ThrowStatement node, TContext context) {
    return new JThrowStatement(exPool.translate(node.getExpression(), context));
  }

  @Override
  public Class<ThrowStatement> getTranslatedType() {
    return ThrowStatement.class;
  }
}
