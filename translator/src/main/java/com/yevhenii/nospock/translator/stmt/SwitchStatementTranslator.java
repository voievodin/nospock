package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.stmt.JCaseStatement;
import com.yevhenii.nospock.jast.stmt.JSwitchStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;

public class SwitchStatementTranslator implements StatementTranslator<SwitchStatement, JSwitchStatement> {

  private final ExPool exPool;
  private final StPool stPool;

  public SwitchStatementTranslator(ExPool exPool, StPool stPool) {
    this.exPool = exPool;
    this.stPool = stPool;
  }

  @Override
  public JSwitchStatement translate(SwitchStatement node, TContext context) {
    final var jSwitch = new JSwitchStatement(exPool.translate(node.getExpression(), context));
    for (CaseStatement caseStatement : node.getCaseStatements()) {
      jSwitch.addCaseStatement(
        new JCaseStatement(
          exPool.translate(caseStatement.getExpression(), context),
          stPool.translate(caseStatement.getCode(), context)
        )
      );
    }
    if (node.getDefaultStatement() != null) {
      jSwitch.setDefaultStatement(stPool.translate(node.getDefaultStatement(), context));
    }
    return jSwitch;
  }

  @Override
  public Class<SwitchStatement> getTranslatedType() {
    return SwitchStatement.class;
  }
}
