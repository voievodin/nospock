package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JField;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.exp.JVariableExpression;

import java.util.Objects;

public class UnusedVariableName {
  
  public static UnusedVariableName generate(String base, TContext context) {
    UnusedVariableName name = new UnusedVariableName(base.replaceAll("[^A-Za-z0-9]", ""), 1);
    while (isUsed(context, name)) {
      name = name.another();
    }
    return name;
  }
  
  private static boolean isUsed(TContext context, UnusedVariableName name) {
    if (context.path().isWithinClass()) {
      final JField jField = context.accessibleField(name.name);
      if (jField != null) {
        return true;
      }
    }
    if (context.path().isWithinMethod()) {
      final TContext.ParametersDeclaration declaration = context.methodParametersDeclaration(context.path().containingMethod());
      if (declaration != null) {
        for (JParameter parameter : context.methodParametersDeclaration(context.path().containingMethod()).parameters) {
          if (parameter.name().equals(name.name)) {
            return true;
          }
        }
      }
    }
    if (context.path().isWithinLambda()) {
      final TContext.ParametersDeclaration declaration = context.lambdaParametersDeclaration(context.path().containingLambda());
      if (declaration != null) {
        for (JParameter parameter : context.lambdaParametersDeclaration(context.path().containingLambda()).parameters) {
          if (parameter.name().equals(name.name)) {
            return true;
          }
        }
      }
    }
    for (TContext.VariableDeclaration accessibleDeclaration : context.accessibleDeclarations()) {
      if (accessibleDeclaration.left instanceof JVariableExpression) {
        final JVariableExpression v = (JVariableExpression) accessibleDeclaration.left;
        if (v.name().equals(name.name)) {
          return true;
        }
      }
    }
    return false;
  }
  
  private final String base;
  private final int idx;
  private final String name;

  private UnusedVariableName(String base, int idx) {
    this.base = Objects.requireNonNull(base);
    this.idx = idx;
    this.name = base + idx;
  }
  
  public String name() {
    return name;
  }
  
  public UnusedVariableName another() {
    return new UnusedVariableName(base, idx + 1);
  }
}
