package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.FieldExpression;

public class FieldExpressionTranslator implements ExpressionTranslator<FieldExpression, JFieldAccessExpression> {

  @Override
  public JFieldAccessExpression translate(FieldExpression node, TContext context) {
    return new JFieldAccessExpression(
      new JConstantExpression("this"),
      new JConstantExpression(node.getFieldName())
    );
  }

  @Override
  public Class<FieldExpression> getTranslatedType() {
    return FieldExpression.class;
  }
}
