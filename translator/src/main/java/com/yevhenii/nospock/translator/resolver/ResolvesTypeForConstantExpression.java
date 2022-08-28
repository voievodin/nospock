package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.ConstantExpression;

public class ResolvesTypeForConstantExpression implements ResolvesType<ConstantExpression> {

  private final TypeLoader typeLoader;

  public ResolvesTypeForConstantExpression(TypeLoader typeLoader) {
    this.typeLoader = typeLoader;
  }

  @Override
  public Class<ConstantExpression> expression() {
    return ConstantExpression.class;
  }

  @Override
  public Class<?> tryResolve(ConstantExpression expression, TContext context, TypeResolver resolver) {
    if (isNullConstant(expression)) {
      return null;
    } else {
      return typeLoader.tryLoad(expression.getType().getName(), context);
    }
  }

  private static boolean isNullConstant(ConstantExpression cExp) {
    return cExp.getValue() == null && cExp.getType().getName().equals("java.lang.Object");
  }
}
