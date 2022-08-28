package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JBreakStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.BreakStatement;

public class BreakStatementTranslator implements StatementTranslator<BreakStatement, JBreakStatement> {
  @Override
  public JBreakStatement translate(BreakStatement node, TContext context) {
    return new JBreakStatement();
  }

  @Override
  public Class<BreakStatement> getTranslatedType() {
    return BreakStatement.class;
  }
}
