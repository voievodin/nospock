package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JImport;

import java.util.List;
import java.util.Objects;

/**
 * A node that requires a few new imports.
 */
public class JForeignAstNode implements JAstNode {

  protected final JAstNode node;
  protected final List<JImport> imports;

  public JForeignAstNode(JAstNode node, List<JImport> imports) {
    this.node = node;
    this.imports = Objects.requireNonNull(imports);
  }

  public List<JImport> imports() {
    return imports;
  }

  public JAstNode getNode() {
    return node;
  }

  @Override
  public String asCode(CodeStyle style) {
    return node.asCode(style);
  }
}
