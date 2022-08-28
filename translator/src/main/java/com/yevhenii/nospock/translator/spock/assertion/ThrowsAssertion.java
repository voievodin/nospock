package com.yevhenii.nospock.translator.spock.assertion;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

public class ThrowsAssertion extends Assertion {

  private List<Statement> wraps = new ArrayList<>();

  public ThrowsAssertion(Expression left, Expression right) {
    super(Operation.THROWS, left, right);
  }

  public ThrowsAssertion(Expression right) {
    this(null, right);
  }

  public void wrappedStatements(List<Statement> statements) {
    this.wraps = new ArrayList<>(statements);
  }

  public List<Statement> wrappedStatements() {
    return wraps;
  }
}
