package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.JAstNode;

import java.util.Collections;
import java.util.List;

public interface JStatement extends JAstNode {
  boolean endsWithSemicolon();

  /**
   * Some statements, like if or try-catch - have nested statements,
   * this method returns those statements.
   */
  default List<? extends JStatement> getNestedStatements() {
    return Collections.emptyList();
  }
}
