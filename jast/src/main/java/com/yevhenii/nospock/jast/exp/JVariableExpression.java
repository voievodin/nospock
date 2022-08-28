package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JType;

import java.util.Objects;

public class JVariableExpression implements JExpression {

  /**
   * Creates an expression that can produces code {@code var name},
   * while contextually saves type within the expression.
   */
  public static JVariableExpression var(JType type, String name) {
    final var varExp = new JVariableExpression(type, name);
    varExp.isDeclaration(true);
    varExp.useVar(true);
    return varExp;
  }
  
  public static JVariableExpression this0() {
    return new JVariableExpression("this");
  }

  private JType type;
  private final String name;

  private boolean isDeclaration;
  private boolean useVar;

  public JVariableExpression(JType declaringType, String variable) {
    this(declaringType, variable, false);
  }

  public JVariableExpression(JType declaringType, String variable, boolean isDeclaration) {
    this.type = Objects.requireNonNull(declaringType);
    this.name = Objects.requireNonNull(variable);
    this.isDeclaration = isDeclaration;
  }

  public JVariableExpression(String variable) {
    this(JType.object(), variable);
  }

  public boolean isDeclaration() {
    return isDeclaration;
  }

  public void isDeclaration(boolean isDeclaration) {
    this.isDeclaration = isDeclaration;
  }

  public void useVar(boolean useVar) {
    this.useVar = useVar;
  }

  public String name() {
    return name;
  }

  public JType type() {
    return type;
  }

  public void type(JType type) {
    this.type = Objects.requireNonNull(type);
  }

  public boolean isThis() {
    return name.equals("this");
  }

  @Override
  public String asCode(CodeStyle style) {
    if (!isDeclaration) {
      return name;
    }
    if (useVar) {
      return "var " + name;
    } else {
      return type.asCode(style) + ' ' + name;
    }
  }

  @Override
  public JType resolveType() {
    return type;
  }
}
