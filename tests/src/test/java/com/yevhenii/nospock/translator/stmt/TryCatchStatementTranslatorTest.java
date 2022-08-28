package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JTryCatchStatement;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TryCatchStatementTranslatorTest {

  private final Translator<TryCatchStatement, JTryCatchStatement> translator = Defaults.translators().translator(TKey.ST_TRY_CATCH);

  @Test
  public void simpleTryCatch() {
    String source =
      """
        class X {
          void x() {
            try {
              call();
            } catch (Exception ex) {
              handle(ex);
            }
          }
        }
        """;

    JTryCatchStatement jTryCatch = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(TryCatchStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        try {
          call();
        } catch (Exception ex) {
          handle(ex);
        }
        """,
      jTryCatch.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void tryMultipleCatches() {
    String source =
      """
        class X {
          void x() {
            try {
              call()
            } catch (IOException ioEx) {
              handle(ex)
            } catch (Exception x) {
              handle(x)
            }
          }
        }
        """;

    JTryCatchStatement jTryCatch = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(TryCatchStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        try {
          call();
        } catch (IOException ioEx) {
          handle(ex);
        } catch (Exception x) {
          handle(x);
        }
        """,
      jTryCatch.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void tryFinally() {
    String source =
      """
        class X {
          void x() {
            try {
              call()
            } finally {
              close()
            }
          }
        }
        """;

    JTryCatchStatement jTryCatch = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(TryCatchStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        try {
          call();
        } finally {
          close();
        }
        """,
      jTryCatch.asCode(Defaults.CODE_STYLE)
    );
  }


  @Test
  public void tryCatchFinally() {
    String source =
      """
        class X {
          void x() {
            try {
              call()
            } catch (Exception x) {
              handle(x)
            } finally {
              close()
            }
          }
        }
        """;

    JTryCatchStatement jTryCatch = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(TryCatchStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        try {
          call();
        } catch (Exception x) {
          handle(x);
        } finally {
          close();
        }
        """,
      jTryCatch.asCode(Defaults.CODE_STYLE)
    );
  }
}
