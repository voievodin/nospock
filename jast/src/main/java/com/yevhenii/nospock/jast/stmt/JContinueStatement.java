package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;

public class JContinueStatement implements JStatement {

  @Override
  public String asCode(CodeStyle style) {
    return "continue";
  }

  @Override
  public boolean endsWithSemicolon() {
    return true;
  }
}
