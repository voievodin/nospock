package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JThrowStatement;
import com.yevhenii.nospock.translator.TKey;

import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThrowStatementTranslatorTest {

  @Test
  public void throwNewException() {
    String source =
      """
        class X {
          void x() {
            throw new Exception("no!")
          }
        }
        """;

    JThrowStatement jThrowStatement = Defaults.translators().translator(TKey.ST_THROW).translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(ThrowStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        throw new Exception("no!")
        """,
      jThrowStatement.asCode(Defaults.CODE_STYLE) + Defaults.CODE_STYLE.nlSequence()
    );
  }
}
