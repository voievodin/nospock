package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JForStatement implements JStatement {

  private final JStatement block;
  private final List<JExpression> vars = new ArrayList<>();
  private final List<JExpression> actions = new ArrayList<>();

  private JExpression condition;

  public JForStatement(JStatement body) {
    this.block = Objects.requireNonNull(body);
  }

  public List<JExpression> vars() {
    return vars;
  }

  public void condition(JExpression condition) {
    this.condition = condition;
  }

  public List<JExpression> actions() {
    return actions;
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("for (")
      .append(
        vars.stream()
          .map(exp -> exp.asCode(style))
          .collect(Collectors.joining(", "))
      )
      .append("; ").append(condition.asCode(style)).append("; ")
      .append(
        actions.stream()
          .map(exp -> exp.asCode(style))
          .collect(Collectors.joining(", "))
      )
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
