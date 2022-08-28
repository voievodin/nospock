package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JEmptyStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.EmptyStatement;

public class EmptyStatementTranslator implements StatementTranslator<EmptyStatement, JEmptyStatement> {
  @Override
  public JEmptyStatement translate(EmptyStatement node, TContext context) {
    return new JEmptyStatement();
  }

  @Override
  public Class<EmptyStatement> getTranslatedType() {
    return EmptyStatement.class;
  }
}
