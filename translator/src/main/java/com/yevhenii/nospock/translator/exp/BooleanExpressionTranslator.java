package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.BooleanExpression;

public class BooleanExpressionTranslator implements ExpressionTranslator<BooleanExpression, JExpression> {

  private final ExPool exPool;

  public BooleanExpressionTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public Class<BooleanExpression> getTranslatedType() {
    return BooleanExpression.class;
  }

  @Override
  public JExpression translate(BooleanExpression node, TContext context) {
    return exPool.translate(node.getExpression(), context);
  }
}
