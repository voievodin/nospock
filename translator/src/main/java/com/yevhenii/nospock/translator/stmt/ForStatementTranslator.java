package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.stmt.JForEachStatement;
import com.yevhenii.nospock.jast.stmt.JForStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.translator.*;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ForStatement;

import java.util.List;
import java.util.Objects;

public class ForStatementTranslator implements StatementTranslator<ForStatement, JStatement> {

  private static final String DUMMY_PARAMETER_NAME = "forLoopDummyParameter";

  private final Translator<ClassNode, JType> typeTranslator;
  private final ExPool exPool;
  private final StPool stPool;

  public ForStatementTranslator(Translator<ClassNode, JType> typeTranslator, ExPool exPool, StPool stPool) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.exPool = Objects.requireNonNull(exPool);
    this.stPool = Objects.requireNonNull(stPool);
  }

  @Override
  public JStatement translate(ForStatement node, TContext context) {
    if (isForEach(node)) {
      return translateForEach(node, context);
    } else {
      return translateFor(node, context);
    }
  }

  private JForEachStatement translateForEach(ForStatement node, TContext context) {
    return new JForEachStatement(
      new JParameter(
        typeTranslator.translate(node.getVariable().getType(), context),
        node.getVariable().getName()
      ),
      exPool.translate(node.getCollectionExpression(), context),
      stPool.translate(node.getLoopBlock(), context)
    );
  }

  private JForStatement translateFor(ForStatement node, TContext context) {
    final var fStmt = new JForStatement(stPool.translate(node.getLoopBlock(), context));
    final var cExp = ((ClosureListExpression) node.getCollectionExpression());
    List<Expression> expressions = cExp.getExpressions();
    if (expressions.size() != 3) {
      throw new RuntimeException("Not supported for loop type, expecting 3 expressions " + expressions.size());
    }

    if (expressions.get(0) instanceof ClosureListExpression) {
      final var clExp = ((ClosureListExpression) expressions.get(0));
      for (Expression expression : clExp.getExpressions()) {
        fStmt.vars().add(exPool.translate(expression, context));
      }
    } else {
      fStmt.vars().add(exPool.translate(expressions.get(0), context));
    }

    fStmt.condition(exPool.translate(expressions.get(1), context));

    if (expressions.get(2) instanceof ClosureListExpression) {
      final var clExp = ((ClosureListExpression) expressions.get(2));
      for (Expression expression : clExp.getExpressions()) {
        fStmt.actions().add(exPool.translate(expression, context));
      }
    } else {
      fStmt.actions().add(exPool.translate(expressions.get(2), context));
    }

    return fStmt;
  }

  private boolean isForEach(ForStatement statement) {
    return !statement.getVariable().getName().equals(DUMMY_PARAMETER_NAME);
  }

  @Override
  public Class<ForStatement> getTranslatedType() {
    return ForStatement.class;
  }
}
