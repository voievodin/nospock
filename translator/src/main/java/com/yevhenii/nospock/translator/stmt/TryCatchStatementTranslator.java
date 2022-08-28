package com.yevhenii.nospock.translator.stmt;

import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.stmt.JCatchStatement;
import com.yevhenii.nospock.jast.stmt.JTryCatchStatement;
import com.yevhenii.nospock.translator.StatementTranslator;
import com.yevhenii.nospock.translator.Translator;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import java.util.Objects;

public class TryCatchStatementTranslator implements StatementTranslator<TryCatchStatement, JTryCatchStatement> {

  private final Translator<ClassNode, JType> typeTranslator;
  private final StPool stPool;

  public TryCatchStatementTranslator(Translator<ClassNode, JType> typeTranslator, StPool stPool) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.stPool = Objects.requireNonNull(stPool);
  }

  @Override
  public JTryCatchStatement translate(TryCatchStatement node, TContext context) {
    final var tc = new JTryCatchStatement(stPool.translate(node.getTryStatement(), context));
    tc.setFinally(stPool.translate(unwrapBlockStatement(node.getFinallyStatement()), context));
    for (CatchStatement catchStatement : node.getCatchStatements()) {
      tc.addCatch(
        new JCatchStatement(
          new JParameter(
            typeTranslator.translate(catchStatement.getVariable().getType(), context),
            catchStatement.getVariable().getName()
          ),
          stPool.translate(catchStatement.getCode(), context)
        )
      );
    }
    return tc;
  }

  // finally block is represented using 2 block statements, we care about nesteted one only
  private Statement unwrapBlockStatement(Statement statement) {
    if (statement instanceof BlockStatement) {
      final var bStmt = ((BlockStatement) statement);
      if (bStmt.getStatements().size() == 1 && bStmt.getStatements().get(0) instanceof BlockStatement) {
        return bStmt.getStatements().get(0);
      }
    }
    return statement;
  }

  @Override
  public Class<TryCatchStatement> getTranslatedType() {
    return TryCatchStatement.class;
  }
}
