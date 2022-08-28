package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.spock.JForeignExpression;

import org.codehaus.groovy.ast.expr.ListExpression;

import java.util.List;
import java.util.Objects;

public class ListExpressionTranslator implements ExpressionTranslator<ListExpression, JExpression> {

  private final ExPool exPool;

  public ListExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<ListExpression> getTranslatedType() {
    return ListExpression.class;
  }

  @Override
  public JExpression translate(ListExpression node, TContext context) {
    if (node.getExpressions().isEmpty()) {
      return new JForeignExpression(
        new JMethodCallExpression(
          new JVariableExpression("Collections"),
          "emptyList"
        ),
        List.of(new JImport("java.util.Collections", false))
      );
    } else {
      return new JForeignExpression(
        new JMethodCallExpression(
          new JVariableExpression("Arrays"),
          "asList",
          TranslateHelper.translate(node.getExpressions(), exPool, context)
        ),
        List.of(new JImport("java.util.Arrays", false))
      );
    }
  }
}
