package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JBinaryExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;

import java.util.Objects;

public class GStringExpressionTranslator implements ExpressionTranslator<GStringExpression, JBinaryExpression> {

  private final ExPool exPool;
  
  public GStringExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<GStringExpression> getTranslatedType() {
    return GStringExpression.class;
  }

  @Override
  public JBinaryExpression translate(GStringExpression node, TContext context) {
    JBinaryExpression result = null;
    for (int i = 0; i < node.getValues().size(); i++) {
      final var binary = new JBinaryExpression(
        exPool.translate(node.getStrings().get(i), context),
        enforceBracesIfBinary(exPool.translate(node.getValues().get(i), context)),
        "+"
      );
      if (result == null) {
        result = binary;
      } else {
        result = new JBinaryExpression(result, binary, "+");
      }
    }
    final ConstantExpression lastString = node.getStrings().get(node.getStrings().size() - 1);
    if (!lastString.isEmptyStringExpression()) {
      result = new JBinaryExpression(result, exPool.translate(lastString, context), "+");
    }
    return result;
  }
  
  private JExpression enforceBracesIfBinary(JExpression expression) {
    if (expression instanceof JBinaryExpression) {
      final var binary = (JBinaryExpression) expression;
      binary.formatting().alwaysWrapInBraces(true);
    }
    return expression;
  }
}
