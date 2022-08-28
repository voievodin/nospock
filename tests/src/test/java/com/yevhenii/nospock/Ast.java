package com.yevhenii.nospock;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Ast {

  public static Locators locators(String source) {
    return new Locators(
      new AstBuilder().buildFromString(
        CompilePhase.CONVERSION,
        true,
        source
      )
    );
  }

  public static class Locators {
    final List<ASTNode> nodes;

    Locators(List<ASTNode> nodes) {
      this.nodes = nodes;
    }

    public ClassNode class0() {
      return locate(nodes, ClassNode.class, 0);
    }

    public PackageNode package0() {
      return class0().getModule().getPackage();
    }

    public ImportNode import0() {
      final ModuleNode module = class0().getModule();
      return Stream.of(
        module.getImports(),
        module.getStarImports(),
        module.getStaticImports().values(),
        module.getStaticStarImports().values()
      ).flatMap(Collection::stream).findFirst().orElse(null);
    }

    public ClassLocator class0loc() {
      return new ClassLocator(class0());
    }
  }

  public static class ClassLocator {
    private final ClassNode node;

    ClassLocator(ClassNode node) {
      this.node = node;
    }

    public FieldNode field0() {
      return node.getFields().get(0);
    }

    public FieldNode field(int idx) {
      return node.getFields().get(idx);
    }

    public MethodNode method0() {
      return node.getMethods().get(0);
    }

    public MethodNode method(int idx) {
      return node.getMethods().get(idx);
    }

    public MethodLocator method0loc() {
      return new MethodLocator(method0());
    }
  }

  public static class MethodLocator {
    private final MethodNode node;

    MethodLocator(MethodNode node) {
      this.node = node;
    }

    public Statement statement0() {
      if (node.getCode() instanceof BlockStatement) {
        return ((BlockStatement) node.getCode()).getStatements().get(0);
      }
      throw new IllegalStateException("Failed to get first statement");
    }

    public <T extends Statement> T statement0(Class<T> stmtClass) {
      return stmtClass.cast(statement0());
    }

    public List<Expression> statementsAsExpressions() {
      List<Expression> result = new ArrayList<>();
      for (Statement statement : statements()) {
        if (statement instanceof ExpressionStatement) {
          result.add(((ExpressionStatement) statement).getExpression());
        }
      }
      return result;
    }

    public List<Statement> statements() {
      if (node.getCode() instanceof BlockStatement) {
        return ((BlockStatement) node.getCode()).getStatements();
      }
      throw new IllegalStateException("Failed to get statements");
    }
  }

  private static <E extends ASTNode> E locate(List<ASTNode> nodes, Class<E> nodeClass, int index) {
    int i = 0;
    for (ASTNode node : nodes) {
      if (nodeClass == node.getClass()) {
        if (i == index) {
          return nodeClass.cast(node);
        } else {
          i++;
        }
      }
    }
    return null;
  }
}
