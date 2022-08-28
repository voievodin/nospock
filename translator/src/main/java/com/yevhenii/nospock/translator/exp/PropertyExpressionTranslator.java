package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.PropertyExpression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class PropertyExpressionTranslator implements ExpressionTranslator<PropertyExpression, JExpression> {

  private final ExPool exPool;
  private final RuntimeLookup runtimeLookup;

  public PropertyExpressionTranslator(ExPool exPool, RuntimeLookup runtimeLookup) {
    this.exPool = Objects.requireNonNull(exPool);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  @Override
  public JExpression translate(PropertyExpression node, TContext context) {
    final Method getter = runtimeLookup.methods.getterUnchecked(node, context);
    if (getter == null) {
      return new JFieldAccessExpression(
        exPool.translate(node.getObjectExpression(), context),
        exPool.translate(node.getProperty(), context)
      );
    } else {
      return new JMethodCallExpression(
        exPool.translate(node.getObjectExpression(), context),
        getter.getName(),
        List.of()
      );
    }
  }

  @Override
  public Class<PropertyExpression> getTranslatedType() {
    return PropertyExpression.class;
  }
}