package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.lang.reflect.Method;
import java.util.Objects;

public class ResolvesTypeForPropertyExpression implements ResolvesType<PropertyExpression> {

  private final TypeLoader typeLoader;
  private final RuntimeLookup runtimeLookup;

  public ResolvesTypeForPropertyExpression(TypeLoader typeLoader, RuntimeLookup runtimeLookup) {
    this.typeLoader = Objects.requireNonNull(typeLoader);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  @Override
  public Class<PropertyExpression> expression() {
    return PropertyExpression.class;
  }

  @Override
  public Class<?> tryResolve(PropertyExpression pExp, TContext context, TypeResolver resolver) {
    if (!(pExp.getProperty() instanceof ConstantExpression)) {
      return null;
    }
    final var property = pExp.getPropertyAsString();

    // BigDecimal.TEN or variable.property
    if (pExp.getObjectExpression() instanceof VariableExpression) {
      final var vExp = ((VariableExpression) pExp.getObjectExpression());
      final Class<?> c = typeLoader.tryLoad(vExp.getText(), context);
      if (c != null) {
        final Class<?> fieldType = TranslateHelper.getFieldType(c, property);
        if (fieldType != null) {
          return fieldType;
        }

        final Method getter = runtimeLookup.methods.getterUncheckedForProperty(c, property);
        if (getter != null) {
          return getter.getReturnType();
        }
      }
    }

    // e.g. for nested enum Outer.Inner.EnumName.VALUE
    // if object expression is type itself we will load it here
    // otherwise if it's method call or something else is in the object expression
    // chain we will be able to resolve it recursively
    if (pExp.getObjectExpression() instanceof PropertyExpression) {
      final var objectPExp = ((PropertyExpression) pExp.getObjectExpression());
      if (isPropertyExpressionThatCanBeInnerClassName(objectPExp)) {
        Class<?> c = TranslateHelper.getFieldType(typeLoader.tryLoad(objectPExp.getText(), context), property);
        if (c != null) {
          return c;
        }
      }
    }

    // in case property expression has other expressions in the chain
    // the type needs to be recursively expanded, e.g. methodcall().TEN
    return TranslateHelper.getFieldType(resolver.tryResolve(pExp.getObjectExpression(), context), property);
  }

  private boolean isPropertyExpressionThatCanBeInnerClassName(PropertyExpression pExp) {
    if (pExp.getObjectExpression() instanceof PropertyExpression) {
      return isPropertyExpressionThatCanBeInnerClassName((PropertyExpression) pExp.getObjectExpression());
    }
    return pExp.getObjectExpression() instanceof VariableExpression;
  }
}
