package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.List;

public class JForeignExpression extends JForeignAstNode implements JExpression {
  public JForeignExpression(JAstNode node, List<JImport> imports) {
    super(node, imports);
  }
}
