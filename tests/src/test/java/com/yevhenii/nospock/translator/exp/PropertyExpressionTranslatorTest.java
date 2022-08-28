package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyExpressionTranslatorTest {

  Translator<PropertyExpression, JExpression> translator = Defaults.translators().translator(TKey.EX_PROPERTY);

  @Test
  public void constantField() {
    PropertyExpression propertyExp = (PropertyExpression) Ast.locators(
        """
          class X {
            public int f1 = Integer.MAX_VALUE;
          }
          """
      )
      .class0loc()
      .field0()
      .getInitialValueExpression();

    JFieldAccessExpression fExp = (JFieldAccessExpression) translator.translate(propertyExp, Defaults.context());

    assertEquals("Integer.MAX_VALUE", fExp.asCode(new CodeStyle()));
  }

  @Test
  public void chainOfFields() {
    PropertyExpression propertyExp = (PropertyExpression) Ast.locators(
        """
          class X {
            public int f1 = MyClass.s1.f1.f2.f3;
          }
          """
      )
      .class0loc()
      .field0()
      .getInitialValueExpression();

    JFieldAccessExpression fExp = (JFieldAccessExpression) translator.translate(propertyExp, Defaults.context());

    assertEquals("MyClass.s1.f1.f2.f3", fExp.asCode(new CodeStyle()));
  }

  @Test
  public void chainOfFieldsWithoutClass() {
    PropertyExpression propertyExp = (PropertyExpression) Ast.locators(
        """
          class X {
            public int f1 = f1.f2.f3;
          }
          """
      )
      .class0loc()
      .field0()
      .getInitialValueExpression();

    JFieldAccessExpression fExp = (JFieldAccessExpression) translator.translate(propertyExp, Defaults.context());

    assertEquals("f1.f2.f3", fExp.asCode(new CodeStyle()));
  }

  @Test
  public void fieldStartingFromThis() {
    PropertyExpression propertyExp = (PropertyExpression) Ast.locators(
        """
          class X {
            public int f1 = this.x;
          }
          """
      )
      .class0loc()
      .field0()
      .getInitialValueExpression();

    JFieldAccessExpression fExp = (JFieldAccessExpression) translator.translate(propertyExp, Defaults.context());

    assertEquals("this.x", fExp.asCode(new CodeStyle()));
  }
}
