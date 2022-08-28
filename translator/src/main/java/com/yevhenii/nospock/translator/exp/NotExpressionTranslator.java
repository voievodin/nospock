package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JNotExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.NotExpression;

import java.util.Objects;

public class NotExpressionTranslator implements ExpressionTranslator<NotExpression, JNotExpression> {

  private final ExPool exPool;

  public NotExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<NotExpression> getTranslatedType() {
    return NotExpression.class;
  }

  @Override
  public JNotExpression translate(NotExpression node, TContext context) {
    return new JNotExpression(exPool.translate(node.getExpression(), context));
  }
}
