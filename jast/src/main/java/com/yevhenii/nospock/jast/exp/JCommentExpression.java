package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class JCommentExpression implements JExpression {

  private final String comment;

  public JCommentExpression(String comment) {
    this.comment = Objects.requireNonNull(comment);
  }

  @Override
  public String asCode(CodeStyle style) {
    return Arrays.stream(comment.split("\n"))
      .map(l -> "// " + l)
      .collect(Collectors.joining(style.nlSequence()));
  }
}
