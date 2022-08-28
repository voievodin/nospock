package com.yevhenii.nospock.jast.exp;

import com.yevhenii.nospock.jast.CodeHelper;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JMethodCallExpression implements JExpression {

  private JAstNode object;
  private final String name;
  private final List<JAstNode> arguments;

  private boolean useThis;

  public JMethodCallExpression(JAstNode object, String name) {
    this(object, name, List.of());
  }

  public JMethodCallExpression(JAstNode object, String name, List<? extends JAstNode> arguments) {
    this.object = Objects.requireNonNull(object);
    this.name = Objects.requireNonNull(name);
    this.arguments = new ArrayList<>(arguments);
  }

  public void object(JAstNode object) {
    this.object = Objects.requireNonNull(object);
  }
  
  public String name() {
    return name;
  }

  public JAstNode object() {
    return object;
  }

  public void useThis(boolean useThis) {
    this.useThis = useThis;
  }

  public void addArgument(JAstNode node) {
    this.arguments.add(node);
  }

  @Override
  public String asCode(CodeStyle style) {
    final StringBuilder sb = new StringBuilder();
    final String oExpCode = object.asCode(style);
    if ("this".equals(oExpCode)) {
      if (useThis) {
        sb.append("this.");
      }
    } else {
      sb.append(oExpCode).append('.');
    }
    sb.append(name).append('(');
    CodeHelper.appendArguments(sb, arguments, style);
    return sb.append(')').toString();
  }
}
