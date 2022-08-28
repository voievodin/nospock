package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JParameter;

import java.util.List;
import java.util.Objects;

public class JCatchStatement implements JStatement {

  private final JParameter parameter;
  private final JStatement block;

  public JCatchStatement(JParameter parameter, JStatement block) {
    this.parameter = Objects.requireNonNull(parameter);
    this.block = Objects.requireNonNull(block);
  }

  @Override
  public String asCode(CodeStyle style) {
    return "catch (" + parameter.asCode(style) + ") " + block.asCode(style);
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<JStatement> getNestedStatements() {
    return List.of(block);
  }
}
