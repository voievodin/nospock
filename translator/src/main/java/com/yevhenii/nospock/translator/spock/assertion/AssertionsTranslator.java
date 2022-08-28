package com.yevhenii.nospock.translator.spock.assertion;


import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.translator.*;
import com.yevhenii.nospock.translator.spi.assertion.AssertionsTemplate;
import com.yevhenii.nospock.translator.spock.JForeignStatement;

import org.codehaus.groovy.ast.expr.Expression;

import java.util.Objects;
import java.util.Set;

public class AssertionsTranslator {

  private final AssertionsTemplate assertionsTemplate;
  private final ExPool exPool;
  private final StPool stPool;
  private final ValueAdjuster valueAdjuster;
  private final RuntimeLookup runtimeLookup;

  public AssertionsTranslator(
    AssertionsTemplate assertionsTemplate,
    ExPool exPool,
    StPool stPool,
    ValueAdjuster valueAdjuster,
    RuntimeLookup runtimeLookup
  ) {
    this.assertionsTemplate = Objects.requireNonNull(assertionsTemplate);
    this.exPool = Objects.requireNonNull(exPool);
    this.stPool = Objects.requireNonNull(stPool);
    this.valueAdjuster = Objects.requireNonNull(valueAdjuster);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  public JForeignStatement translate(Assertion assertion, TContext context) {
    switch (assertion.getOperation()) {
      case EQUALS:
        return translateEquals(assertion, context);
      case THROWS:
        return translateThrows((ThrowsAssertion) assertion, context);
      case IS_TRUE:
        return translateIsTrue(assertion, context);
      case IS_NOT_NULL:
        return translateIsNotNull(assertion, context);
      case NOT_EQUALS:
        return translateNotEquals(assertion, context);
      case IS_NULL:
        return translateIsNull(assertion, context);
      case IS_FALSE:
        return translateIsFalse(assertion, context);
      default:
        throw new TranslationException("Not supported assertion operation " + assertion.getOperation());
    }
  }

  private JForeignStatement translateNotEquals(Assertion assertion, TContext context) {
    final EqualsTranslation equalsTranslation = new EqualsTranslation(assertion.getLeft(), assertion.getRight(), context);
    return assertionsTemplate.assertNotEquals(equalsTranslation.comparedAgainst, equalsTranslation.actual);
  }

  private JForeignStatement translateIsNotNull(Assertion assertion, TContext context) {
    return assertionsTemplate.assertNotNull(exPool.translate(assertion.getLeft(), context));
  }

  private JForeignStatement translateEquals(Assertion assertion, TContext context) {
    final EqualsTranslation equalsTranslation = new EqualsTranslation(assertion.getLeft(), assertion.getRight(), context);
    return assertionsTemplate.assertEquals(equalsTranslation.comparedAgainst, equalsTranslation.actual);
  }

  private JForeignStatement translateThrows(ThrowsAssertion assertion, TContext context) {
    return assertionsTemplate.assertThrows(
      translateThrowsLeftDeclaration(assertion.getLeft(), context),
      new JFieldAccessExpression(
        new JVariableExpression(TranslateHelper.classNameFromClassLiteralExpression(assertion.getRight())),
        new JConstantExpression("class")
      ),
      new JBlockStatement(
        TranslateHelper.translate(
          assertion.wrappedStatements(),
          stPool,
          context
        )
      )
    );
  }

  private JForeignStatement translateIsTrue(Assertion assertion, TContext context) {
    return assertionsTemplate.assertTrue(exPool.translate(assertion.getLeft(), context));
  }

  private JExpression translateThrowsLeftDeclaration(Expression expression, TContext context) {
    if (expression == null) {
      return null;
    }
    final var translated = exPool.translate(expression, context);
    if (translated instanceof JVariableExpression) {
      final JVariableExpression jVar = (JVariableExpression) translated;
      jVar.useVar(true);
      jVar.isDeclaration(true);
    }
    return translated;
  }

  private JForeignStatement translateIsNull(Assertion assertion, TContext context) {
    return assertionsTemplate.assertNull(exPool.translate(assertion.getLeft(), context));
  }

  private JForeignStatement translateIsFalse(Assertion assertion, TContext context) {
    return assertionsTemplate.assertFalse(exPool.translate(assertion.getLeft(), context));
  }

  private class EqualsTranslation {
    JExpression actual;
    JExpression comparedAgainst;
    
    EqualsTranslation(Expression left, Expression right, TContext context) {
      comparedAgainst = exPool.translate(right, context);
      actual = exPool.translate(left, context);
      if (valueAdjuster.canPossiblyAdjust(comparedAgainst)) {
        if (actual.resolveType() != null && !actual.resolveType().isJavaLangObject()) {
          comparedAgainst = valueAdjuster.adjust(actual.resolveType(), comparedAgainst);
        } else if (actual instanceof JVariableExpression) {
          final Set<JType> types = TypeCollector.collect(left, runtimeLookup, context);
          if (types.size() == 1) {
            comparedAgainst = valueAdjuster.adjust(types.iterator().next(), comparedAgainst);
          }
        }
      }
    }
  }
}

