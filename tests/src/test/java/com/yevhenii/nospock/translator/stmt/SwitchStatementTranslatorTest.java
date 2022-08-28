package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.stmt.JSwitchStatement;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwitchStatementTranslatorTest {

  private final Translator<SwitchStatement, JSwitchStatement> translator = Defaults.translators().translator(TKey.ST_SWITCH);

  @Test
  public void switchWithMultipleCaseBlocks() {
    String source =
      """
        class X {
          void x() {
            switch (variable) {
              case "123":
                handle123();
                break;
              case "321":
                handle321();
                break;
              default:
                handleDefault();
                break;
            }
          }
        }
        """;

    JSwitchStatement jSwitch = translator.translate(
      Ast.locators(source)
        .class0loc()
        .method0loc()
        .statement0(SwitchStatement.class),
      Defaults.context()
    );

    assertEquals(
      """
        switch (variable) {
          case "123": {
            handle123();
            break;
          }
          case "321": {
            handle321();
            break;
          }
          default: {
            handleDefault();
            break;
          }
        }
        """,
      jSwitch.asCode(Defaults.CODE_STYLE)
    );
  }
}
