package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;

public class BlockTranslation {

  private final JBlockStatement block = new JBlockStatement();
  private boolean over = false;

  public void add(JStatement statement, TContext context) {
    if (over) {
      throw new IllegalArgumentException("Cannot add statements, translation for block is over");
    }
    block.statements().add(statement);
    TransformationsQueue.transform(
      TransformationsQueue.Target.BLOCK,
      context.path(),
      TransformationsQueue.Priority.IMMEDIATE,
      block
    );
  }

  public void add(int index, JStatement statement, TContext context) {
    if (over) {
      throw new IllegalArgumentException("Cannot add statements, translation for block is over");
    }
    block.statements().add(index, statement);
    TransformationsQueue.transform(
      TransformationsQueue.Target.BLOCK,
      context.path(),
      TransformationsQueue.Priority.IMMEDIATE,
      block
    );
  }

  public <G extends Statement, J extends JStatement> void addTranslating(
    List<G> statements,
    Translator<G, J> translator,
    TContext context
  ) {
    if (statements != null) {
      for (G statement : statements) {
        add(translator.translate(statement, context), context);
      }
    }
  }

  public int statementsSize() {
    return block.statements().size();
  }

  public JBlockStatement end(TContext context) {
    over = true;
    TransformationsQueue.transformAll(
      TransformationsQueue.Target.BLOCK,
      context.path(),
      block
    );
    return block;
  }
}
