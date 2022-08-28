package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.stmt.ForStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForStatementTranslatorTest {

  private final Translator<ForStatement, JStatement> translator = Defaults.translators().translator(TKey.ST_FOR);

  @Test
  public void forEachStatement() {
    String source =
      """
        class X {
          void x() {
            for (int x : collection) {
            }
          }
        }
        """;

    JStatement jForStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(ForStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        for (int x : collection) {
        }
        """,
      jForStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void forEachStatementEmptyBody() {

    String source =
      """
        class X {
          void x() {
            for (int x : collection);
          }
        }
        """;

    JStatement jForStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(ForStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        for (int x : collection);
        """,
      jForStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void forEachNonBlockNestedStatement() {
    String source =
      """
        class X {
          void x() {
            for (int x : collection)
              if (x % 2 == 0) {
                call(x);
              }
          }
        }
        """;

    JStatement jForStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(ForStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        for (int x : collection)
          if ((x % 2) == 0) {
            call(x);
          }
        """,
      jForStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void forStatement() {
    String source =
      """
        class X {
          void x() {
            for (int i = 0; i < 10; i++) {
            }
          }
        }
        """;

    JStatement jForStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(ForStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        for (int i = 0; i < 10; i++) {
        }
        """,
      jForStatement.asCode(Defaults.CODE_STYLE)
    );
  }
}
