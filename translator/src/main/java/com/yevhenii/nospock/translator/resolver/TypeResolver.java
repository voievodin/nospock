package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.Expression;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class TypeResolver {

  private static final Logger LOG = Logger.getLogger(TypeResolver.class.getName());

  private final List<ResolvesType<?>> resolvers = new CopyOnWriteArrayList<>();

  public TypeResolver() {}

  public void register(ResolvesType<?> typeResolver) {
    resolvers.add(typeResolver);
  }

  @SuppressWarnings("all")
  public Class<?> tryResolve(Expression expression, TContext context) {
    Class<?> resolved = null;
    for (ResolvesType resolver : resolvers) {
      // might worth using is assignable from, for now this is ok
      if (resolver.expression().equals(expression.getClass())) {
        resolved = resolver.tryResolve(expression, context, this);
        break;
      }
    }

    if (resolved == null) {
      LOG.finest(
        String.format(
          "Failed to load class for expression '%s', text='%s'",
          expression.getClass().getSimpleName(),
          expression.getText()
        )
      );
    } else {
      LOG.finest(
        String.format(
          "Resolved '%s' for expression '%s'",
          resolved.getSimpleName(),
          expression.getClass().getSimpleName()
        )
      );
    }

    return resolved;
  }
}
