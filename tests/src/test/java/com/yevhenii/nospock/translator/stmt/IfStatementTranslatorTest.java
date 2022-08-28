package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JIfStatement;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.stmt.IfStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IfStatementTranslatorTest {

  private final Translator<IfStatement, JIfStatement> translator = Defaults.translators().translator(TKey.ST_IF);

  @Test
  public void simplestIfStatement() {
    String source =
      """
        class X {
          void x() {
            if (true) {
               
            }
          }
        }
        """;

    JIfStatement jIfStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(IfStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        if (true) {
        }
        """,
      jIfStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void ifElse() {
    String source =
      """
        class X {
          void x() {
            if (true) {
               
            } else {
            
            }
          }
        }
        """;

    JIfStatement jIfStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(IfStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        if (true) {
        } else {
        }
        """,
      jIfStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void ifIfElseElse() {
    String source =
      """
        class X {
          void x() {
            if (true) {
               
            } else if (true) {
            
            } else {
            
            }
          }
        }
        """;

    JIfStatement jIfStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(IfStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        if (true) {
        } else if (true) {
        } else {
        }
        """,
      jIfStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void ifStatementWithMultiComponentExpression() {
    String source =
      """
        class X {
          int x = 22
                
          void x() {
            if (123 > x && isTrue() || x % 2 == 5) {
              x = 33
            }
          }
        }
        """;

    JIfStatement jIfStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(IfStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        if (((123 > x) && isTrue()) || ((x % 2) == 5)) {
          x = 33;
        }
        """,
      jIfStatement.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void ifStatementWithoutBlock() {
    String source =
      """
        class X {
          int x() {
            if (true) return 123
          }
        }
        """;

    JIfStatement jIfStatement = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(IfStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        if (true) return 123;
        """,
      jIfStatement.asCode(Defaults.CODE_STYLE)
    );
  }
}
