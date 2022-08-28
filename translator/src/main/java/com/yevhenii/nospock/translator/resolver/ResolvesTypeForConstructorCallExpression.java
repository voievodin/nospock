package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.ConstructorCallExpression;

import java.util.Objects;

public class ResolvesTypeForConstructorCallExpression implements ResolvesType<ConstructorCallExpression> {

  private final TypeLoader typeLoader;

  public ResolvesTypeForConstructorCallExpression(TypeLoader typeLoader) {
    this.typeLoader = Objects.requireNonNull(typeLoader);
  }

  @Override
  public Class<ConstructorCallExpression> expression() {
    return ConstructorCallExpression.class;
  }

  @Override
  public Class<?> tryResolve(ConstructorCallExpression expression, TContext context, TypeResolver resolver) {
    return typeLoader.tryLoad(expression.getType().getName(), context);
  }
}
