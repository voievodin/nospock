package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.Defaults;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JCommentExpressionTest {

  @Test
  void singleLineComment() {
    assertEquals("// single line", new JCommentExpression("single line").asCode(Defaults.CODE_STYLE));
  }

  @Test
  void multiLineComment() {
    assertEquals("// multi\n// line", new JCommentExpression("multi\nline").asCode(Defaults.CODE_STYLE));
  }
}
