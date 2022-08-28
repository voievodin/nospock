package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.translator.resolver.TypeResolver;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unlike {@link RuntimeLookup} or {@link TypeResolver} operates on {@link JType} instance.
 * Accumulates type by applying different resolution techniques.
 */
public class TypeCollector {

  public static Set<JType> collect(Expression expression, RuntimeLookup lookup, TContext context) {
    return collect(List.of(expression), lookup, context);
  }

  public static Set<JType> collect(Collection<? extends Expression> expressions, RuntimeLookup runtimeLookup, TContext context) {
    final var typeCollector = new TypeCollector(runtimeLookup);
    typeCollector.collectFromExpressionsThatResolveTypes(expressions, context);
    return typeCollector.types();
  }

  private final RuntimeLookup runtimeLookup;
  private final Set<JType> types = new LinkedHashSet<>();

  public TypeCollector(RuntimeLookup runtimeLookup) {
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  public Set<JType> types() {
    return types;
  }

  /**
   * Collects type from expressions if those can possibly resolve type.
   * For example, a constructor call resolves the type of the constructed object.
   */
  public void collectFromExpressionsThatResolveTypes(Collection<? extends Expression> expressions, TContext context) {
    for (Expression expression : expressions) {
      accumulate(runtimeLookup.classes.resolvedBy(expression, context));
    }
  }

  /**
   * Collects types from declarations within the scope.
   * For example, if there is a variable declared of a given type, that type is used.
   * Like for `X x`, the X is resolved.
   */
  public void collectFromContextDeclarations(String variable, TContext context) {
    accumulate(runtimeLookup.classes.resolvedBy(new VariableExpression(variable), context));
  }

  /**
   * Collects types based on how variable is used within the scope and context.
   * For example, if variable named 'x' is passed to method as first parameter,
   * the collector will go and find that method and try to collect type of that parameter.
   */
  public void collectFromInScopeUsage(String variable, List<Statement> scope, TContext context) {
    for (Statement statement : scope) {
      if (!(statement instanceof ExpressionStatement)) {
        continue;
      }
      final var exStatement = (ExpressionStatement) statement;
      if (exStatement.getExpression() instanceof MethodCallExpression) {
        final var mCall = (MethodCallExpression) exStatement.getExpression();
        for (var aClass : typesOfArgumentNamed(variable, mCall, context)) {
          types.add(new JType(aClass).boxIfPrimitive());
        }
      }
    }
  }

  private void accumulate(Class<?> c) {
    if (c != null) {
      types.add(new JType(c).boxIfPrimitive());
    }
  }

  // K.call(x, y) finds -> k.call(String x, int y) resolves -> String
  private Set<Class<?>> typesOfArgumentNamed(String argName, MethodCallExpression mCall, TContext context) {
    if (!(mCall.getArguments() instanceof ArgumentListExpression)) {
      return Set.of();
    }
    final var argList = (ArgumentListExpression) mCall.getArguments();
    for (int i = 0; i < argList.getExpressions().size(); i++) {
      final var argExpression = argList.getExpressions().get(i);
      if (argName.equals(argExpression.getText())) {
        final var fi = i;
        return runtimeLookup.methods.homonyms(mCall, context)
          .stream()
          .filter(m -> m.getParameterCount() > fi)
          .map(m -> m.getParameterTypes()[fi])
          .collect(Collectors.toSet());
      }
    }
    return Set.of();
  }
}
