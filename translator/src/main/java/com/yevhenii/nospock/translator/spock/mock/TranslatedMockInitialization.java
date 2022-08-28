package com.yevhenii.nospock.translator.spock.mock;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.translator.spock.JForeignExpression;

import java.util.Objects;

/**
 * Wraps mock template results and provides additional details
 * to the infrastructure so that translators have enough context.
 */
public class TranslatedMockInitialization extends JForeignExpression {

  private final MockType type;
  private final String className;

  public TranslatedMockInitialization(
    MockType type,
    String className,
    JForeignExpression delegate
  ) {
    super(delegate, delegate.imports());
    this.className = Objects.requireNonNull(className);
    this.type = Objects.requireNonNull(type);
  }

  public String className() {
    return className;
  }

  public MockType mockType() {
    return type;
  }

  @Override
  public JType resolveType() {
    return new JType(className); 
  }
}
