package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.exp.JCastExpression;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CastExpressionTranslatorTest {

  private Translator<CastExpression, JCastExpression> translator = Defaults.translators().translator(TKey.EX_CAST);

  @Test
  public void simpleCastOfNumber() {
    final var statement = Ast.locators(
        """
          class X {
            void x() {
              def v = (long) 123;
            }
          }
          """
      )
      .class0loc()
      .method0loc()
      .statement0(ExpressionStatement.class);

    final var translated = translator.translate(
      (CastExpression) ((DeclarationExpression) statement.getExpression()).getRightExpression(),
      Defaults.context()
    );

    assertEquals("((long) 123)", translated.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void castOfMethodCallResult() {
    final var statement = Ast.locators(
        """
          class X {
            void x() {
              def v = (long) X.num();
            }
            
            public static int num() {
              return 2;
            }
          }
          """
      )
      .class0loc()
      .method0loc()
      .statement0(ExpressionStatement.class);


    final var translated = translator.translate(
      (CastExpression) ((DeclarationExpression) statement.getExpression()).getRightExpression(),
      Defaults.context()
    );

    assertEquals("((long) X.num())", translated.asCode(Defaults.CODE_STYLE));
  }
}
