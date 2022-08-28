package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JIfStatement;
import com.yevhenii.nospock.jast.stmt.JThrowStatement;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.spock.JForeignStatement;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsDetector;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsTranslator;

import org.codehaus.groovy.ast.stmt.AssertStatement;

import java.util.List;
import java.util.Objects;

public class AssertStatementTranslator implements StatementTranslator<AssertStatement, JForeignStatement> {

  private final AssertionsDetector assertionsDetector;
  private final AssertionsTranslator assertionsTranslator;
  private final ExPool exPool;

  public AssertStatementTranslator(AssertionsDetector detector, AssertionsTranslator translator, ExPool exPool) {
    this.assertionsDetector = Objects.requireNonNull(detector);
    this.assertionsTranslator = Objects.requireNonNull(translator);
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<AssertStatement> getTranslatedType() {
    return AssertStatement.class;
  }

  @Override
  public JForeignStatement translate(AssertStatement node, TContext context) {
    final var assertion = assertionsDetector.detect(node, context);
    if (assertion != null) {
      return assertionsTranslator.translate(assertion, context);
    }
    return new JForeignStatement(
      new JIfStatement(
        exPool.translate(node.getBooleanExpression(), context), // TODO negation
        new JBlockStatement(
          new JThrowStatement(
            new JConstructorCallExpression(new JType(AssertionError.class))
          )
        ),
        null
      ),
      List.of()
    );
  }
}
