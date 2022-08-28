package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.lang.reflect.Method;

/**
 * Resolves method return type, including void.
 */
public class ResolvesTypeForMethodCallExpression implements ResolvesType<MethodCallExpression> {

  private final MethodDeclaringTypeResolver methodDeclaringTypeResolver;

  public ResolvesTypeForMethodCallExpression(TypeLoader typeLoader) {
    this.methodDeclaringTypeResolver = new MethodDeclaringTypeResolver(typeLoader);
  }

  @Override
  public Class<MethodCallExpression> expression() {
    return MethodCallExpression.class;
  }

  @Override
  public Class<?> tryResolve(MethodCallExpression expression, TContext context, TypeResolver resolver) {
    final Class<?> c = methodDeclaringTypeResolver.tryResolve(expression, context, resolver);
    if (c != null) {
      for (Method method : TranslateHelper.methods(c)) {
        if (method.getName().equals(expression.getMethodAsString())) {
          return method.getReturnType();
        }
      }
    }
    return null;
  }
}
