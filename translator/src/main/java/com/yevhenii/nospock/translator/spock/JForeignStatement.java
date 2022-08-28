package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.stmt.JStatement;

import java.util.List;

public class JForeignStatement extends JForeignAstNode implements JStatement {

  public JForeignStatement(JStatement node, List<JImport> imports) {
    super(node, imports);
  }

  @Override
  public boolean endsWithSemicolon() {
    return ((JStatement) node).endsWithSemicolon();
  }
}
