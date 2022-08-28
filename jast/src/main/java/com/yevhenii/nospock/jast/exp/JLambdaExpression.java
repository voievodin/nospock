package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JLambdaExpression implements JExpression {

  private final List<JParameter> parameters;
  private final JBlockStatement block;

  public JLambdaExpression(List<JParameter> parameters, JBlockStatement block) {
    this.parameters = new ArrayList<>(parameters);
    for (JParameter parameter : parameters) {
      parameter.formatting().omitType();
    }
    this.block = Objects.requireNonNull(block);
  }

  public JBlockStatement block() {
    return block;
  }

  public List<JParameter> parameters() {
    return parameters;
  }

  @Override
  public String asCode(CodeStyle style) {
    return "(" +
           parameters.stream().map(p -> p.asCode(style)).collect(Collectors.joining(", ")) +
           ") -> " +
           block.asCode(style);
  }
}
