package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Objects;

public class ResolvesTypeToNull<T extends Expression> implements ResolvesType<T> {

  private final Class<T> expressionClass;

  public ResolvesTypeToNull(Class<T> expressionClass) {
    this.expressionClass = Objects.requireNonNull(expressionClass);
  }

  @Override
  public Class<T> expression() {
    return expressionClass;
  }

  @Override
  public Class<?> tryResolve(T expression, TContext context, TypeResolver resolver) {
    return null;
  }
}
