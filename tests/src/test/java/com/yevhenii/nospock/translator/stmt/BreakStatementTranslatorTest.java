package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JWhileStatement;
import com.yevhenii.nospock.translator.TKey;

import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BreakStatementTranslatorTest {


  @Test
  public void breakStatement() {
    String source =
      """
        class X {
          void x() {
            while (true) {
              break;
            }
          }
        }
        """;

    JWhileStatement jWhileStatement = Defaults.translators().translator(TKey.ST_WHILE).translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(WhileStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        while (true) {
          break;
        }
        """,
      jWhileStatement.asCode(Defaults.CODE_STYLE)
    );
  }
}

