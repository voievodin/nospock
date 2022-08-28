package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JPrefixExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.PrefixExpression;

import java.util.Objects;

public class PrefixExpressionTranslator implements ExpressionTranslator<PrefixExpression, JPrefixExpression> {

  private final ExPool exPool;

  public PrefixExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<PrefixExpression> getTranslatedType() {
    return PrefixExpression.class;
  }

  @Override
  public JPrefixExpression translate(PrefixExpression node, TContext context) {
    return new JPrefixExpression(
      exPool.translate(node.getExpression(), context),
      node.getOperation().getText()
    );
  }
}
