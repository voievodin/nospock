package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JTernaryExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.TernaryExpression;

import java.util.Objects;

public class TernaryExpressionTranslator implements ExpressionTranslator<TernaryExpression, JTernaryExpression> {

  private final ExPool exPool;

  public TernaryExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<TernaryExpression> getTranslatedType() {
    return TernaryExpression.class;
  }

  @Override
  public JTernaryExpression translate(TernaryExpression node, TContext context) {
    return new JTernaryExpression(
      exPool.translate(node.getBooleanExpression(), context),
      exPool.translate(node.getTrueExpression(), context),
      exPool.translate(node.getFalseExpression(), context)
    );
  }
}
