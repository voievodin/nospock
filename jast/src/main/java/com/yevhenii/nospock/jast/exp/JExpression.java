package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JType;

/**
 * Marker interface for expression node.
 */
public interface JExpression extends JAstNode {

  /**
   * If result of this expression can be assigned to a variable, this is the type of that variable. Void also counts.
   */
  default JType resolveType() {
    return null;
  }
}
