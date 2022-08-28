package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Logger;

public class MethodDeclaringTypeResolver {

  private static final Logger LOG = Logger.getLogger(ResolvesTypeForMethodCallExpression.class.getName());

  private final TypeLoader typeLoader;

  public MethodDeclaringTypeResolver(TypeLoader typeLoader) {
    this.typeLoader = Objects.requireNonNull(typeLoader);
  }

  public Class<?> tryResolve(MethodCallExpression expression, TContext context, TypeResolver resolver) {
    if (isThisVariableExpression(expression.getObjectExpression())) {
      return resolveTypeOfThisForMethodCall(expression.getMethodAsString(), context);
    } else {
      return resolver.tryResolve(expression.getObjectExpression(), context);
    }
  }

  /**
   * 'this' is used in case of static import and in case when class instance actually has the method
   * in order to understand which one is used we first attempt to lookup the method within the class
   * and if such method does not exist we try to find the corresponding static import for it.
   */
  private Class<?> resolveTypeOfThisForMethodCall(String methodName, TContext context) {
    final var thisClass = typeLoader.tryLoad(context.path().containingClass().last().name(), context);
    if (thisClass == null) {
      LOG.fine(
        String.format(
          "Failed to load class '%s' for 'this' variable expression",
          context.path().containingClass().last().name()
        )
      );
      return null;
    }

    // class X { void x() { valueOf(123)}; X valueOf(int x) {} }
    if (hasMethodNamed(thisClass, methodName)) {
      return thisClass;
    }

    // import static java.math.BigDecimal.valueOf
    final var staticImport = findStaticImportForMethod(methodName, context);
    if (staticImport == null) {
      LOG.finest(String.format("Couldn't locate static import for method '%s'", methodName));
      return null;
    }

    // for import static java.math.BigDecimal.valueOf loads java.math.BigDecimal
    final var classOnWhichStaticMethodIsImported = typeLoader.tryLoad(staticImport.fqn().withoutLast(), context);
    if (classOnWhichStaticMethodIsImported == null) {
      LOG.finest(
        String.format(
          "Failed to load class on which static method is imported, import = '%s'",
          staticImport.fqn().withoutLast()
        )
      );
    }

    return classOnWhichStaticMethodIsImported;
  }

  private boolean isThisVariableExpression(Expression exp) {
    return exp instanceof VariableExpression && "this".equals(((VariableExpression) exp).getName());
  }

  private static boolean hasMethodNamed(Class<?> c, String methodName) {
    for (Method method : TranslateHelper.methods(c)) {
      if (method.getName().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  private static JImport findStaticImportForMethod(String methodName, TContext context) {
    for (JImport anImport : context.imports()) {
      if (anImport.isStatic() && anImport.fqn().last().asString().equals(methodName)) {
        return anImport;
      }
    }
    return null;
  }
}
