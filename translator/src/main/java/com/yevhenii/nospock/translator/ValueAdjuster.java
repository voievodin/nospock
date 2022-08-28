package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JExpression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A common component to contain logic converting values to match given types.
 * E.g. when method argument accepts BigDecimal, but number is provided
 * the component will convert the value to be BigDecimal.
 */
public class ValueAdjuster {

  private final List<Adjuster> adjusters;
  private final Set<Class<? extends JExpression>> canPossiblyAdjust;
  
  public ValueAdjuster() {
    this.adjusters = List.of(
      new ConstantIntToBoxedLongAdjuster(),
      new NumberToBigDecimalAdjuster(),
      new NumberToBigIntegerAdjuster(),
      new ConstantNumberToDoubleAdjuster()
    );
    this.canPossiblyAdjust = adjusters.stream()
      .flatMap(adj -> adj.canPossiblyAdjustExpressions().stream())
      .collect(Collectors.toUnmodifiableSet());
  }
  
  public boolean canPossiblyAdjust(JExpression value) {
    return value != null && canPossiblyAdjust.contains(value.getClass());
  }
  
  public JExpression adjust(JType type, JExpression value) {
    for (Adjuster adjuster : adjusters) {
      if (adjuster.canAdjust(type, value)) {
        final JExpression adjusted = adjuster.adjust(type, value);
        if (adjusted != null) {
          return adjusted;
        }
      }
    }
    return value;
  }

  private interface Adjuster {
    List<Class<? extends JExpression>> canPossiblyAdjustExpressions();
    boolean canAdjust(JType type, JExpression jExp);
    JExpression adjust(JType type, JExpression jExp);
  }

  private static class ConstantIntToBoxedLongAdjuster implements Adjuster {

    @Override
    public List<Class<? extends JExpression>> canPossiblyAdjustExpressions() {
      return List.of(JConstantExpression.class);
    }

    @Override
    public boolean canAdjust(JType type, JExpression jExp) {
      return "Long".equals(type.name())
        && jExp instanceof JConstantExpression
        && ((JConstantExpression) jExp).value() instanceof Integer;
    }

    @Override
    public JConstantExpression adjust(JType type, JExpression jExp) {
      return new JConstantExpression(((Integer) ((JConstantExpression) jExp).value()).longValue());
    }
  }
  
  private static class NumberToBigDecimalAdjuster implements Adjuster {

    @Override
    public List<Class<? extends JExpression>> canPossiblyAdjustExpressions() {
      return List.of(JConstantExpression.class);
    }

    @Override
    public boolean canAdjust(JType type, JExpression jExp) {
      return "BigDecimal".equals(type.name())
      && jExp instanceof JConstantExpression
      && ((JConstantExpression) jExp).value() instanceof Number;
    }

    @Override
    public JExpression adjust(JType type, JExpression jExp) {
      return new JConstructorCallExpression(new JType(BigDecimal.class), List.of(jExp));
    }
  }

  private static class NumberToBigIntegerAdjuster implements Adjuster {

    @Override
    public List<Class<? extends JExpression>> canPossiblyAdjustExpressions() {
      return List.of(JConstantExpression.class);
    }

    @Override
    public boolean canAdjust(JType type, JExpression jExp) {
      return "BigInteger".equals(type.name())
             && jExp instanceof JConstantExpression
             && ((JConstantExpression) jExp).value() instanceof Number;
    }

    @Override
    public JExpression adjust(JType type, JExpression jExp) {
      return new JConstructorCallExpression(new JType(BigInteger.class), List.of(jExp));
    }
  }
  
  private static class ConstantNumberToDoubleAdjuster implements Adjuster {

    @Override
    public List<Class<? extends JExpression>> canPossiblyAdjustExpressions() {
      return List.of(JConstantExpression.class);
    }

    @Override
    public boolean canAdjust(JType type, JExpression jExp) {
      return "Double".equals(type.name())
             && jExp instanceof JConstantExpression
             && ((JConstantExpression) jExp).value() instanceof Number;
    }

    @Override
    public JConstantExpression adjust(JType type, JExpression jExp) {
      return new JConstantExpression(((Number) ((JConstantExpression) jExp).value()).doubleValue());
    }
  }
}
