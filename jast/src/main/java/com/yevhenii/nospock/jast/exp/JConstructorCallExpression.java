package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.stmt.JStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JConstructorCallExpression implements JExpression {

  private final JType type;
  private final List<JAstNode> arguments;

  private JClass anonymousType;

  public JConstructorCallExpression(JType type) {
    this(type, List.of());
  }

  public JConstructorCallExpression(JType type, JClass anonymousType) {
    this(type);
    this.anonymousType = anonymousType;
  }
  
  public JConstructorCallExpression(JType type, List<JAstNode> arguments) {
    this.type = Objects.requireNonNull(type);
    this.arguments = new ArrayList<>(arguments);
  }

  public void addArgument(JAstNode argNode) {
    this.arguments.add(argNode);
  }

  public void anonymousType(JClass type) {
    this.anonymousType = type;
  }

  public JType type() {
    return type;
  }

  @Override
  public String asCode(CodeStyle style) {
    final var sb = new StringBuilder("new ");
    sb.append(type.asCode(style));
    sb.append('(');
    CodeHelper.appendArguments(sb, arguments, style);
    sb.append(')');
    if (anonymousType != null) {
      sb.append(' ').append(anonymousType.asCode(style));
    }
    return sb.toString();
  }

  @Override
  public JType resolveType() {
    return type;
  }
}
