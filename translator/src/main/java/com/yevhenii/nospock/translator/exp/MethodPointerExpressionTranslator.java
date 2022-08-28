package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JMethodReferenceExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.MethodPointerExpression;

import java.util.Objects;

public class MethodPointerExpressionTranslator implements ExpressionTranslator<MethodPointerExpression, JMethodReferenceExpression> {

  private final ExPool exPool;

  public MethodPointerExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<MethodPointerExpression> getTranslatedType() {
    return MethodPointerExpression.class;
  }

  @Override
  public JMethodReferenceExpression translate(MethodPointerExpression node, TContext context) {
    return new JMethodReferenceExpression(
      exPool.translate(node.getExpression(), context),
      node.getMethodName().getText()
    );
  }
}
