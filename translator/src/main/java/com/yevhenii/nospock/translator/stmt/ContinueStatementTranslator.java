package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JContinueStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.ContinueStatement;

public class ContinueStatementTranslator implements StatementTranslator<ContinueStatement, JContinueStatement> {
  @Override
  public JContinueStatement translate(ContinueStatement node, TContext context) {
    return new JContinueStatement();
  }

  @Override
  public Class<ContinueStatement> getTranslatedType() {
    return ContinueStatement.class;
  }
}
