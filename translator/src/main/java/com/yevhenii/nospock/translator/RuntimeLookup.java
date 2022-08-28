package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.translator.resolver.MethodDeclaringTypeResolver;
import com.yevhenii.nospock.translator.resolver.TypeResolver;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RuntimeLookup {

  private static final Logger LOG = Logger.getLogger(RuntimeLookup.class.getName());

  public final Methods methods;
  public final Classes classes;

  private final TypeResolver typeResolver;
  private final MethodDeclaringTypeResolver methodDeclaringTypeResolver;

  public RuntimeLookup(TypeLoader typeLoader, TypeResolver typeResolver) {
    this.typeResolver = typeResolver;
    this.methodDeclaringTypeResolver = new MethodDeclaringTypeResolver(typeLoader);
    this.methods = new Methods();
    this.classes = new Classes();
  }

  public class Methods {
    
    /**
     * For property 'x' finds -> 'getX()' or 'isX()' or 'getIsX()'
     * where method return type is assignable from the property type.
     */
    public Method getter(PropertyExpression pExp, TContext context) {
      return findMethodForProperty(pExp, context, "get", (field, method) ->
        (field.getModifiers() & Modifier.STATIC) == 0
        && method.getParameterCount() == 0
        && method.getReturnType().isAssignableFrom(field.getType())
      );
    }

    /**
     * Finds getter for given property. Does not check that types can be assigned from one another.
     * E.g. resolves {@code Namespace#isActive} for {@code Namespace.active} property.
     */
    public Method getterUnchecked(PropertyExpression pExp, TContext context) {
      final Class<?> c = classes.resolvedBy(pExp.getObjectExpression(), context);
      if (c == null) {
        return null;
      }
      return getterUncheckedForProperty(c, pExp.getPropertyAsString());
    }

    /**
     * Finds getter for given property. Does not check that types can be assigned from one another.
     * E.g. resolves {@code Namespace#isActive} for {@code Namespace.active} property where given class points to {@code Namespace}.
     */
    public Method getterUncheckedForProperty(Class<?> c, String property) {
      final var propertyFirstLetterCapitalized = TranslateHelper.capitalizeFirst(property);
      final Method getter = findGetter(TranslateHelper.methods(c), propertyFirstLetterCapitalized);
      // since we might not know the expected type we need to ensure that 
      // collision between inner class name and get method name is properly handled
      // e.g. Thread#getState when Thread.State property is used (which is expected to resolve to class).
      if (getter != null) {
        for (Class<?> declaredClass : c.getDeclaredClasses()) {
          final var cName = declaredClass.getSimpleName();
          if (cName.equals(property) || cName.equals(propertyFirstLetterCapitalized)) {
            return null;
          }
        }
      }
      return getter;
    }

    /**
     * For property 'x' finds -> 'setX(X' x)' or 'isX(X' x)' or 'setIsX(X' x)'
     * where property type is assignable from the parameter type.
     */
    public Method setter(PropertyExpression pExp, TContext context) {
      return findMethodForProperty(pExp, context, "set", (field, method) ->
        (field.getModifiers() & Modifier.STATIC) == 0
        && method.getParameterCount() == 1
        && field.getType().isAssignableFrom(method.getParameterTypes()[0])
      );
    }

    /**
     * Finds methods on the same class instance having that same name as the given one.
     */
    public List<Method> homonyms(MethodCallExpression mCall, TContext context) {
      final Class<?> aClass = methodDeclaringTypeResolver.tryResolve(mCall, context, typeResolver);
      if (aClass == null) {
        LOG.finest(String.format("Couldn't locate class on which method '%s' is called", mCall.getMethodAsString()));
        return List.of();
      }
      return TranslateHelper.methods(aClass)
        .stream()
        .filter(m -> m.getName().equals(mCall.getMethodAsString()))
        .collect(Collectors.toList());
    }
  }


  public class Classes {

    /**
     * If result of expression can be assigned to a variable then this
     * method will attempt resolving type of that variable, e.g.
     * the constructor call resolves the type of the object being created,
     * the variable named 'x' that resolve to the type declaring that variable.
     */
    public Class<?> resolvedBy(Expression exp, TContext context) {
      return typeResolver.tryResolve(exp, context);
    }
  }

  private Method findMethodForProperty(
    PropertyExpression pExp,
    TContext context,
    String prefix,
    BiFunction<Field, Method, Boolean> predicate
  ) {
    final Class<?> c = classes.resolvedBy(pExp.getObjectExpression(), context);
    if (c == null) {
      return null;
    }

    final String property = pExp.getPropertyAsString();
    Field f;
    try {
      f = c.getDeclaredField(property);
    } catch (NoSuchFieldException x) {
      try {
        f = c.getField(property);
      } catch (NoSuchFieldException e) {
        LOG.finest(String.format("Couldn't get field '%s' for class '%s'", property, c));
        return null;
      }
    }

    final var nameCandidates = new ArrayList<String>();
    nameCandidates.add(prefix + TranslateHelper.capitalizeFirst(property));
    if (f.getType() == boolean.class) {
      nameCandidates.add("is" + TranslateHelper.capitalizeFirst(property));
    } else if (f.getType() == Boolean.class) {
      nameCandidates.add(prefix + "Is" + TranslateHelper.capitalizeFirst(property));
    }

    final Map<String, List<Method>> methodsMap = TranslateHelper.methods(c).stream().collect(
      Collectors.groupingBy(Method::getName, Collectors.toList())
    );
    for (String nameCandidate : nameCandidates) {
      for (Method methodCandidate : Objects.requireNonNullElse(methodsMap.get(nameCandidate), List.<Method>of())) {
        if (predicate.apply(f, methodCandidate)) {
          return methodCandidate;
        }
      }
    }

    LOG.finest(
      String.format(
        "Couldn't find method prefix '%s' for property '%s' in class '%s'",
        prefix,
        property,
        c
      )
    );
    return null;
  }
  
  private static Method findGetter(Collection<Method> methods, String propertyFirstCharCapital) {
    final var getName = "get" + propertyFirstCharCapital;
    final var isName = "is" + propertyFirstCharCapital;
    for (Method method : methods) {
      if (isGetGetter(method, getName) || isIsGetter(method, isName)) {
        return method;
      }
    }
    return null;
  }

  private static boolean isGetGetter(Method method, String name) {
    return method.getParameterCount() == 0
           && method.getName().equals(name)
           && void.class != method.getReturnType();
  }

  private static boolean isIsGetter(Method method, String name) {
    return method.getParameterCount() == 0
           && method.getName().equals(name)
           && (boolean.class == method.getReturnType() || Boolean.class == method.getReturnType());
  }
}
