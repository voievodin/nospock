package com.yevhenii.nospock.translator.resolver;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.TypeLoader;

import org.codehaus.groovy.ast.expr.VariableExpression;

import java.lang.reflect.Field;
import java.util.logging.Logger;

public class ResolvesTypeForVariableExpression implements ResolvesType<VariableExpression> {

  private static final Logger LOG = Logger.getLogger(ResolvesTypeForVariableExpression.class.getName());

  private final TypeLoader typeLoader;

  public ResolvesTypeForVariableExpression(TypeLoader typeLoader) {
    this.typeLoader = typeLoader;
  }

  @Override
  public Class<VariableExpression> expression() {
    return VariableExpression.class;
  }

  @Override
  public Class<?> tryResolve(VariableExpression vExp, TContext context, TypeResolver resolver) {
    // for variable declared within scope of accessible blocks e.g. for 'call' method expression from snippet
    // 'void flow() { X x; x.call(); }'  the result will be Class<X>
    final JType nonObjectDeclaredVariableType = typeOfDeclaredVariable(vExp.getName(), context);
    if (nonObjectDeclaredVariableType != null) {
      final Class<?> staticallyTypedVariableClass = typeLoader.tryLoad(nonObjectDeclaredVariableType.fqn(), context);
      if (staticallyTypedVariableClass == null) {
        LOG.finest(String.format("Failed to load class for JType (based on declared variable '%s')", nonObjectDeclaredVariableType));
      }
      return staticallyTypedVariableClass;
    }

    // for parameter declared in the scope of current method (if that's the context)
    // find out type of such parameter
    if (context.path().isWithinMethod()) {
      final var parametersDeclaration = context.methodParametersDeclaration(context.path().containingMethod());
      if (parametersDeclaration != null) {
        for (JParameter parameter : parametersDeclaration.parameters) {
          if (parameter.name().equals(vExp.getName())) {
            final Class<?> parameterVariableClass = typeLoader.tryLoad(parameter.type().fqn(), context);
            if (parameterVariableClass == null) {
              LOG.finest(String.format("Failed to load class for JType (based on parameter type '%s')", parameter.type()));
            }
            return parameterVariableClass;
          }
        }
      }
    }

    if (context.path().isWithinLambda()) {
      final var parametersDeclaration = context.lambdaParametersDeclaration(context.path().containingLambda());
      if (parametersDeclaration != null) {
        for (JParameter parameter : parametersDeclaration.parameters) {
          if (parameter.name().equals(vExp.getName())) {
            final Class<?> parameterVariableClass = typeLoader.tryLoad(parameter.type().fqn(), context);
            if (parameterVariableClass == null) {
              LOG.finest(String.format("Failed to load class for JType (based on parameter type '%s')", parameter.type()));
            }
            return parameterVariableClass;
          }
        }
      }
    }


    // try to load a type for a field accessible from the current scope and named the same as variable
    final var field = context.accessibleField(vExp.getName());
    if (field != null) {
      final Class<?> classOfField = typeLoader.tryLoad(field.type().fqn(), context);
      if (classOfField == null) {
        LOG.finest(String.format("Failed to load class for JType (based on field type '%s')", field.type()));
      }
      return classOfField;
    }

    // in case variable (enum or constant) is statically imported, e.g. import static java.math.BigDecimal.ONE (var name = ONE)
    JImport staticImport = findStaticImportFor(vExp.getName(), context);
    if (staticImport != null) {
      final Class<?> classDeclaringVariable = typeLoader.tryLoad(staticImport.fqn().withoutLastOrEmpty(), context);
      if (classDeclaringVariable != null) {
        final Field f = TranslateHelper.getField(classDeclaringVariable, vExp.getName());
        if (f != null) {
          return f.getType();
        }
      }
    }

    // e.g. for BigDecimal simply load the type
    return typeLoader.tryLoad(vExp.getName(), context);
  }

  private JType typeOfDeclaredVariable(String variable, TContext context) {
    for (var declaration : context.accessibleDeclarations()) {
      if (declaration.left instanceof JVariableExpression) {
        final var left = (JVariableExpression) declaration.left;
        if (left.name().equals(variable)) {
          if (!left.type().isJavaLangObject()) {
            return left.type();
          } else if (declaration.right != null) {
            return declaration.right.resolveType();
          }
        }
      }
    }
    return null;
  }

  private static JImport findStaticImportFor(String name, TContext context) {
    for (JImport anImport : context.imports()) {
      if (anImport.isStatic() && anImport.fqn().last().asString().equals(name)) {
        return anImport;
      }
    }
    return null;
  }
}
