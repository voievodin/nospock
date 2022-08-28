package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;

public class JBreakStatement implements JStatement {

  @Override
  public String asCode(CodeStyle style) {
    return "break";
  }

  @Override
  public boolean endsWithSemicolon() {
    return true;
  }
}
