package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.expr.Expression;

/**
 * Resolves type for expression. Can resolve type for expression if
 * result of expression can be assigned to a variable, the type of that variable
 * would be the aim for the resolver to come up with. For example, for
 * constructor call the resolved type would be the type of constructed instance.
 */
public interface ResolvesType<T extends Expression> {

  /**
   * Type of expression that this resolver is capable to resolve.
   */
  Class<T> expression();

  /**
   * Resolves class that expression returns or returns null in case
   * the resolution isn't possible (e.g. don't have necessary information in classpath).
   */
  Class<?> tryResolve(T expression, TContext context, TypeResolver resolver);
}
