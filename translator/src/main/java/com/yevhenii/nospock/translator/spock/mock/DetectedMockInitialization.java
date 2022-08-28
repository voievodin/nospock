package com.yevhenii.nospock.translator.spock.mock;

import org.codehaus.groovy.ast.expr.ClosureExpression;

import java.util.Objects;

public class DetectedMockInitialization {

  private final MockType type;
  private final String className;
  private final ClosureExpression answer;

  public DetectedMockInitialization(MockType type, String className) {
    this(type, className, null);
  }

  public DetectedMockInitialization(MockType type, String className, ClosureExpression answer) {
    this.type = Objects.requireNonNull(type);
    this.className = Objects.requireNonNull(className);
    this.answer = answer;
  }

  public String className() {
    return className;
  }

  public ClosureExpression answer() {
    return answer;
  }

  public MockType mockType() {
    return type;
  }
}
