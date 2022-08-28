package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JBlockStatement implements JStatement {

  private final List<JStatement> statements = new ArrayList<>();
  private final Formatting formatting = new Formatting();

  public JBlockStatement() {}

  public JBlockStatement(JStatement statement0) {
    statements.add(statement0);
  }

  public JBlockStatement(List<JStatement> statements) {
    this.statements.addAll(statements);
  }

  public List<JStatement> statements() {
    return statements;
  }

  public Formatting formatting() {
    return formatting;
  }

  @Override
  public String asCode(CodeStyle style) {
    if (statements.size() == 1 && formatting.noBracesIfSingleStatement && canBeUsedWithoutBraces(statements.get(0))) {
      return statements.get(0).asCode(style);
    }

    StringBuilder sb = new StringBuilder("{").append(style.nlSequence());
    for (JStatement statement : statements) {
      sb.append(CodeHelper.indent(statement.asCode(style), style.nlSequence(), style.indent()));
      if (statement.endsWithSemicolon()) {
        sb.append(';');
      }
      sb.append(style.nlSequence());
    }
    return sb.append("}").toString();
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<JStatement> getNestedStatements() {
    return statements;
  }

  public static class Formatting {
    private boolean noBracesIfSingleStatement;

    public void doNotUseBracesIfSingleStatement() {
      noBracesIfSingleStatement = true;
    }
  }
  
  private boolean canBeUsedWithoutBraces(JStatement statement) {
    return statement instanceof JExpressionStatement; 
  }
}
