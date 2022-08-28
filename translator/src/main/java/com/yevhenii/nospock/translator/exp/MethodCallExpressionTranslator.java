package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.ConstructorCallInitializedWithSetters;
import com.yevhenii.nospock.translator.ConstructorToVariableExtractor;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.GroovyImplicitImports;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.ValueAdjuster;
import com.yevhenii.nospock.translator.spock.mock.MockDetector;
import com.yevhenii.nospock.translator.spock.mock.DetectedMockInitialization;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.lang.reflect.Method;
import java.util.Objects;

public class MethodCallExpressionTranslator implements ExpressionTranslator<MethodCallExpression, JExpression> {

  private final ExPool exPool;
  private final MockDetector mockDetector;
  private final MockTranslator mockTranslator;
  private final RuntimeLookup runtimeLookup;
  private final ValueAdjuster valueAdjuster;

  public MethodCallExpressionTranslator(
    ExPool exPool,
    MockDetector mockDetector,
    MockTranslator mockTranslator,
    RuntimeLookup runtimeLookup,
    ValueAdjuster valueAdjuster
  ) {
    this.exPool = Objects.requireNonNull(exPool);
    this.mockDetector = Objects.requireNonNull(mockDetector);
    this.mockTranslator = Objects.requireNonNull(mockTranslator);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
    this.valueAdjuster = Objects.requireNonNull(valueAdjuster);
  }

  @Override
  public JExpression translate(MethodCallExpression node, TContext context) {
    final DetectedMockInitialization mockInitialization = mockDetector.detectInitialization(node);
    if (mockInitialization != null) {
      return mockTranslator.translateInitialization(mockInitialization, context);
    }

    final var expression = new JMethodCallExpression(
      exPool.translate(node.getObjectExpression(), context),
      node.getMethod().getText()
    );

    // Ensure we import types on which methods are called if that's the case
    // This covers method calls like Optional.of(x). [Imports Optional]
    if (expression.object() instanceof JVariableExpression) {
      final var jve = (JVariableExpression) expression.object();
      if (!jve.isThis()) {
        GroovyImplicitImports.explicitlyImport(new JType(jve.name()));
      }
    }

    expression.useThis(!node.isImplicitThis());
    if (node.getArguments() != null) {
      final var alExp = (ArgumentListExpression) node.getArguments();
      final var methodProvider = new CachingMethodProvider(node, alExp.getExpressions().size(), context);
      for (int i = 0; i < alExp.getExpressions().size(); i++) {
        var argTranslated = exPool.translate(alExp.getExpression(i), context);
        if (argTranslated instanceof ConstructorCallInitializedWithSetters) {
          final ConstructorCallInitializedWithSetters constructorWithSetters = (ConstructorCallInitializedWithSetters) argTranslated;
          final ConstructorToVariableExtractor extractor = new ConstructorToVariableExtractor(constructorWithSetters);
          argTranslated = extractor.extractBeforeCurrentStatement(context);
        }
        
        expression.addArgument(
          adjustArgumentValueToMatchSignatureType(
            argTranslated, 
            i, 
            methodProvider
          )
        );
      }
    }
    return expression;
  }

  @Override
  public Class<MethodCallExpression> getTranslatedType() {
    return MethodCallExpression.class;
  }
  
  private JExpression adjustArgumentValueToMatchSignatureType(JExpression argExp, int expIdx, CachingMethodProvider methodProvider) {
    if (valueAdjuster.canPossiblyAdjust(argExp)) {
      final Method method = methodProvider.get();
      if (method != null) {
        return valueAdjuster.adjust(new JType(methodProvider.get().getParameterTypes()[expIdx]), argExp);
      }
    }
    return argExp;
  }
  
  private class CachingMethodProvider {

    final MethodCallExpression mCall;
    final int argSize;
    final TContext context;
    
    boolean computed;
    Method method;

    CachingMethodProvider(MethodCallExpression mCall, int argSize, TContext context) {
      this.mCall = mCall;
      this.argSize = argSize;
      this.context = context;
    }

    public Method get() {
      if (computed) {
        return method;
      }
      for (Method method : runtimeLookup.methods.homonyms(mCall, context)) {
        if (method.getParameterCount() == argSize) {
          this.method = method;
          break;
        }
      }
      computed = true;
      return method;
    }
  }
}
