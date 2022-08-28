package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JArrayElementAccessExpression;
import com.yevhenii.nospock.jast.exp.JBinaryExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class BinaryExpressionTranslator implements ExpressionTranslator<BinaryExpression, JExpression> {

  private final ExPool exPool;
  private final MockTranslator mockTranslator;
  private final RuntimeLookup runtimeLookup;

  public BinaryExpressionTranslator(ExPool exPool, MockTranslator mockTranslator, RuntimeLookup runtimeLookup) {
    this.exPool = Objects.requireNonNull(exPool);
    this.mockTranslator = Objects.requireNonNull(mockTranslator);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  @Override
  public JExpression translate(BinaryExpression node, TContext context) {
    final var left = exPool.translate(node.getLeftExpression(), context);
    final var right = exPool.translate(node.getRightExpression(), context);
    final var op = node.getOperation().getText();

    // args[0]
    if ("[".equals(op)) {
      return new JArrayElementAccessExpression(left, right);
    }

    // Turns 'mock = MocK()' into 'mock = Mock(X.class)' if has enough context.
    final JBinaryExpression result = mockTranslator.adjustTypes(new JBinaryExpression(left, right, op), context);

    // object.a = 123, when there is a getter for a :(
    // usually the getter will be used instead of the property access
    // let's stick to the strategy to prefer using setters, as it's likely
    // that generated property access won't be legit
    if ("=".equals(op) && node.getLeftExpression() instanceof PropertyExpression) {
      final Method setter = runtimeLookup.methods.setter(((PropertyExpression) node.getLeftExpression()), context);
      if (setter != null) {
        return new JMethodCallExpression(
          exPool.translate(((PropertyExpression) node.getLeftExpression()).getObjectExpression(), context),
          setter.getName(),
          List.of(TranslateHelper.correctType(setter.getParameterTypes()[0], result.right()))
        );
      }
    }

    adjustFieldTypeIfNecessary(node, context);

    return result;
  }

  @Override
  public Class<BinaryExpression> getTranslatedType() {
    return BinaryExpression.class;
  }

  // note that does not handle cases when variable name equals to field name
  private void adjustFieldTypeIfNecessary(BinaryExpression binary, TContext context) {
    if ("=".equals(binary.getOperation().getText()) && binary.getLeftExpression() instanceof VariableExpression) {
      final var variable = (VariableExpression) binary.getLeftExpression();
      final var field = context.accessibleField(variable.getName());
      if (field != null && field.type().isJavaLangObject()) {
        final Class<?> c = runtimeLookup.classes.resolvedBy(binary.getRightExpression(), context);
        if (c != null) {
          field.type(new JType(c));
        }
      }
    }
  }
}
