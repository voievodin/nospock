package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JWhileStatement;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WhileStatementTranslatorTest {

  private final Translator<WhileStatement, JWhileStatement> translator = Defaults.translators().translator(TKey.ST_WHILE);

  @Test
  public void simpleWhileStatement() {
    String source =
      """
        class X {
          void x() {
            while (true) {
            }
          }
        }
        """;

    JWhileStatement jWhileStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(WhileStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        while (true) {
        }
        """,
      jWhileStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void whileStatementNestedStatement() {
    String source =
      """
        class X {
          void x() {
            while (x > 2)
              if (x % 2 == 1) {
                x++;
              }
          }
        }
        """;

    JWhileStatement jWhileStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(WhileStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        while (x > 2)
          if ((x % 2) == 1) {
            x++;
          }
        """,
      jWhileStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void whileStatementWithoutBody() {
    String source =
      """
        class X {
          void x() {
            while (true);
          }
        }
        """;

    JWhileStatement jWhileStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(WhileStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        while (true);
        """,
      jWhileStatement.asCode(Defaults.CODE_STYLE)
    );
  }
}
