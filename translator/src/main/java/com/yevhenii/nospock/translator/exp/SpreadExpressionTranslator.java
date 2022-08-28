package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.SpreadExpression;

// For now supporting it 1:1 so that compilation of generated source fails, though the translation finalizes
public class SpreadExpressionTranslator implements ExpressionTranslator<SpreadExpression, JExpression> {

  @Override
  public Class<SpreadExpression> getTranslatedType() {
    return SpreadExpression.class;
  }

  @Override
  public JExpression translate(SpreadExpression node, TContext context) {
    return new JVariableExpression("*_");
  }
}
