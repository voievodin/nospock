package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JPostfixExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.PostfixExpression;

public class PostfixExpressionTranslator implements ExpressionTranslator<PostfixExpression, JPostfixExpression> {

  private final ExPool exPool;

  public PostfixExpressionTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public JPostfixExpression translate(PostfixExpression node, TContext context) {
    return new JPostfixExpression(
      exPool.translate(node.getExpression(), context),
      node.getOperation().getText()
    );
  }

  @Override
  public Class<PostfixExpression> getTranslatedType() {
    return PostfixExpression.class;
  }
}
