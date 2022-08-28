package com.yevhenii.nospock.translator.spi.engine;

import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.util.List;
import java.util.Objects;

public class ArgumentValues {
  public final JParameter parameter;
  public final List<JExpression> values;
  public final JExpression valuesProvider;

  public ArgumentValues(JParameter parameter, List<JExpression> values, JExpression  valuesProvider) {
    this.parameter = Objects.requireNonNull(parameter);
    this.values = values;
    this.valuesProvider = valuesProvider;
    if ((values.isEmpty()) == (valuesProvider == null)) {
      throw new IllegalArgumentException("Either values or values provider must be used");
    }
  }
}
