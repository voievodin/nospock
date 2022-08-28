package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;

// used for things like missing else block
public class JEmptyStatement implements JStatement {
  @Override
  public String asCode(CodeStyle style) {
    return "";
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }
}
