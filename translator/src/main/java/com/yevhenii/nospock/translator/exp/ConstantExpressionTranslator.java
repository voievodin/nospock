package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslationConfig;

import org.codehaus.groovy.ast.expr.ConstantExpression;

import java.util.Objects;

public class ConstantExpressionTranslator implements ExpressionTranslator<ConstantExpression, JConstantExpression> {
  
  private final TranslationConfig translationConfig;

  public ConstantExpressionTranslator(TranslationConfig translationConfig) {
    this.translationConfig = Objects.requireNonNull(translationConfig);
  }

  @Override
  public JConstantExpression translate(ConstantExpression node, TContext context) {
    final JConstantExpression cExp = new JConstantExpression(node.getValue());
    if (!translationConfig.enableTextBlocks()) {
      cExp.cannotBeTextBlock();
    }
    return cExp;
  }

  @Override
  public Class<ConstantExpression> getTranslatedType() {
    return ConstantExpression.class;
  }
}
