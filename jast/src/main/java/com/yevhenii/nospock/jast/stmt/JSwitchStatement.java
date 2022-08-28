package com.yevhenii.nospock.jast.stmt;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.ArrayList;
import java.util.List;

public class JSwitchStatement implements JStatement {

  private final JExpression expression;
  private final List<JCaseStatement> cases = new ArrayList<>();

  private JStatement defaultStatement;

  public JSwitchStatement(JExpression expression) {
    this.expression = expression;
  }

  public void addCaseStatement(JCaseStatement caseStatement) {
    cases.add(caseStatement);
  }

  public void setDefaultStatement(JStatement defaultStatement) {
    this.defaultStatement = defaultStatement;
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("switch (")
      .append(expression.asCode(style))
      .append(") {")
      .append(style.nlSequence());
    for (JCaseStatement casesStatement : cases) {
      sb.append(
        CodeHelper.indent(
          casesStatement.asCode(style),
          style.nlSequence(),
          style.indent()
        )
      ).append(style.nlSequence());
    }
    if (defaultStatement != null && !(defaultStatement instanceof JEmptyStatement)) {
      sb.append(
        CodeHelper.indent(
          "default: " + defaultStatement.asCode(style),
          style.nlSequence(),
          style.indent()
        )
      ).append(style.nlSequence());
    }
    return sb.append("}").append(style.nlSequence()).toString();
  }

  @Override
  public boolean endsWithSemicolon() {
    return false;
  }

  @Override
  public List<? extends JStatement> getNestedStatements() {
    return cases;
  }
}
