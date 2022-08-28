package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.TransformationsQueue;

import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListExpressionTranslatorTest {

  @AfterEach
  public void cleanup() {
    TransformationsQueue.instance().reset();
  }

  @Test
  public void translatesListExpression() {
    final var statement0 = Ast.locators(
        """
          class X {
            void x() {
              [1, 2, 3]
            }
          }
          """
      )
      .class0loc()
      .method0loc()
      .statement0(ExpressionStatement.class);

    assertEquals(
      "Arrays.asList(1, 2, 3)",
      Defaults.translators().translator(TKey.EX_LIST).translate(
        (ListExpression) statement0.getExpression(),
        Defaults.context()
      ).asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void translatesEmptyListToCollectionsEmptyList() {
    final var statement0 = Ast.locators(
        """
          class X {
            void x() {
              []
            }
          }
          """
      )
      .class0loc()
      .method0loc()
      .statement0(ExpressionStatement.class);

    assertEquals(
      "Collections.emptyList()",
      Defaults.translators().translator(TKey.EX_LIST).translate(
        (ListExpression) statement0.getExpression(),
        Defaults.context()
      ).asCode(Defaults.CODE_STYLE)
    );
  }
}
