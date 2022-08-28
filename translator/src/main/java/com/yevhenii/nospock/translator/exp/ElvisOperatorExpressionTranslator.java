package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JTernaryExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;

import java.util.Objects;

public class ElvisOperatorExpressionTranslator implements ExpressionTranslator<ElvisOperatorExpression, JTernaryExpression> {
  
  private final Translator<TernaryExpression, JTernaryExpression> ternaryTranslator;

  public ElvisOperatorExpressionTranslator(Translator<TernaryExpression, JTernaryExpression> ternaryTranslator) {
    this.ternaryTranslator = Objects.requireNonNull(ternaryTranslator);
  }

  @Override
  public Class<ElvisOperatorExpression> getTranslatedType() {
    return ElvisOperatorExpression.class;
  }

  @Override
  public JTernaryExpression translate(ElvisOperatorExpression node, TContext context) {
    return ternaryTranslator.translate(node, context);
  }
}
