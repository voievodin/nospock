package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.TransformationsQueue;

import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstructorCallExpressionTranslatorTest {

  Translator<ConstructorCallExpression, JConstructorCallExpression> translator =
    Defaults.translators().translator(TKey.EX_CONSTRUCTOR_CALL);

  @AfterEach
  public void afterEach() {
    TransformationsQueue.instance().reset();
  }

  @Test
  public void emptyConstructorCall() {
    ConstructorCallExpression exp = (ConstructorCallExpression) Ast.locators(
      """
        class X {
          int v = new X();
        }
        """
    ).class0loc().field0().getInitialExpression();

    JConstructorCallExpression cExp = translator.translate(exp, Defaults.context());

    assertEquals("new X()", cExp.asCode(new CodeStyle()));
  }

  @Test
  public void constructorCallWithArguments() {
    ConstructorCallExpression exp = (ConstructorCallExpression) Ast.locators(
      """
        class X {
          int v = new Y(1, "x", null);
        }
        """
    ).class0loc().field0().getInitialExpression();

    JConstructorCallExpression cExp = translator.translate(exp, Defaults.context());

    assertEquals("new Y(1, \"x\", null)", cExp.asCode(new CodeStyle()));
  }

  @Test
  public void constructorCallWithConstructorCallAsArgument() {
    ConstructorCallExpression exp = (ConstructorCallExpression) Ast.locators(
      """
        class X {
          int v = new Y(1, new Z(1))
        }
        """
    ).class0loc().field0().getInitialExpression();

    JConstructorCallExpression cExp = translator.translate(exp, Defaults.context());

    assertEquals("new Y(1, new Z(1))", cExp.asCode(new CodeStyle()));
  }

  @Test
  public void anonymousClassConstructor() {
    ConstructorCallExpression exp = (ConstructorCallExpression) Ast.locators(
      """
        class X {
          Serializable s = new Serializable() {
          }
        }
        """
    ).class0loc().field0().getInitialExpression();

    JConstructorCallExpression cExp = translator.translate(exp, Defaults.context());

    assertEquals(
      """
        new Serializable() {
        }
        """,
      cExp.asCode(new CodeStyle())
    );
  }

  @Test
  public void anonymousClassConstructorWithArguments() {
    ConstructorCallExpression exp = (ConstructorCallExpression) Ast.locators(
      """
        class X {
          Y s = new Y(1, "x") {
          }
        }
        """
    ).class0loc().field0().getInitialExpression();

    JConstructorCallExpression cExp = translator.translate(exp, Defaults.context());

    assertEquals(
      """
        new Y(1, "x") {
        }
        """,
      cExp.asCode(new CodeStyle())
    );
  }
}
