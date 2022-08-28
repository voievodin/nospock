package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JArrayExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;

import org.codehaus.groovy.ast.expr.ArrayExpression;

import java.util.List;
import java.util.Objects;

public class ArrayExpressionTranslator implements ExpressionTranslator<ArrayExpression, JArrayExpression> {

  private final ExPool exPool;

  public ArrayExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public JArrayExpression translate(ArrayExpression node, TContext context) {
    return new JArrayExpression(
      new JType(node.getType().getComponentType() + "[]"),
      node.getExpressions() == null ? List.of() : TranslateHelper.translate(node.getExpressions(), exPool, context),
      node.getSizeExpression() == null ? List.of() : TranslateHelper.translate(node.getSizeExpression(), exPool, context)
    );
  }

  @Override
  public Class<ArrayExpression> getTranslatedType() {
    return ArrayExpression.class;
  }
}
