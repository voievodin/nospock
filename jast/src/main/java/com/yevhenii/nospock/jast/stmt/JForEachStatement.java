package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.List;
import java.util.Objects;

public class JForEachStatement implements JStatement {

  private final JParameter parameter;
  private final JExpression iterable;
  private final JStatement block;

  public JForEachStatement(JParameter parameter, JExpression iterable, JStatement block) {
    this.parameter = Objects.requireNonNull(parameter);
    this.iterable = Objects.requireNonNull(iterable);
    this.block = Objects.requireNonNull(block);
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder("for (")
      .append(parameter.asCode(style))
      .append(" : ")
      .append(iterable.asCode(style))
      .append(")");
    CodeHelper.appendStatementsBlock(sb, block, style);
    return sb.append(style.nlSequence()).toString();
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
