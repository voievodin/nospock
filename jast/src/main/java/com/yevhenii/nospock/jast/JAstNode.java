package com.yevhenii.nospock.jast;

public interface JAstNode {

  /**
   * Returns code that represents this node assuming that
   * the composition of such representations can form a valid source code.
   */
  String asCode(CodeStyle style);
}
