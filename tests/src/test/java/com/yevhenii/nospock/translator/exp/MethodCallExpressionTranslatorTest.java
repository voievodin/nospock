package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.translator.CPath;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TKey;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodCallExpressionTranslatorTest {

  Translator<MethodCallExpression, JExpression> translator = Defaults.translators().translator(TKey.EX_METHOD_CALL);

  @Test
  public void translatesEmptyMethodCall() {
    MethodCallExpression exp = (MethodCallExpression) Ast.locators(
      """
        class X {
          int v = call();
        }
        """
    ).class0loc().field0().getInitialExpression();

    JMethodCallExpression translated = (JMethodCallExpression) translator.translate(exp, Defaults.context());

    assertEquals("call()", translated.asCode(new CodeStyle()));
  }

  @Test
  public void translatesMethodCallWithArguments() {
    MethodCallExpression exp = (MethodCallExpression) Ast.locators(
      """
        class X {
          int v = call(1, "xxxx", null);
        }
        """
    ).class0loc().field0().getInitialExpression();

    JMethodCallExpression translated = (JMethodCallExpression) translator.translate(
      exp,
      new TContext(
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        CPath.ROOT.add(CPath.Seg.forClass("X"))
      )
    );

    assertEquals("call(1, \"xxxx\", null)", translated.asCode(new CodeStyle()));
  }

  @Test
  public void translatesMethodCallWithNestedMethodCallaAsArgument() {
    MethodCallExpression exp = (MethodCallExpression) Ast.locators(
      """
        class X {
          int v = call(1, call2(2));
        }
        """
    ).class0loc().field0().getInitialExpression();

    JMethodCallExpression translated = (JMethodCallExpression) translator.translate(
      exp,
      new TContext(
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        CPath.ROOT.add(CPath.Seg.forClass("X"))
      )
    );

    assertEquals("call(1, call2(2))", translated.asCode(new CodeStyle()));
  }
}
