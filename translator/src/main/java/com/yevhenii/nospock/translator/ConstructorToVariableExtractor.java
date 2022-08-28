package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.exp.JDeclarationExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;

import java.util.Objects;

/**
 * Handles extraction of constructor with declared setters to be variable.
 * For example, the construction within the following method call:
 * <pre>
 *   method.call(new MyObject() { { setA(1); setB("x"); } };
 * </pre>
 *
 * can be inlined within the block before the call. Like
 * 
 * <pre>
 *   var myObject = new MyObject();
 *   myObject.setA(1);
 *   myObject.setB("x");
 *   method.call(myObject);
 * </pre>
 */
public class ConstructorToVariableExtractor {

  private final ConstructorCallInitializedWithSetters constructorWithSetters;

  public ConstructorToVariableExtractor(
    ConstructorCallInitializedWithSetters constructorWithSetters) {
    this.constructorWithSetters = Objects.requireNonNull(constructorWithSetters);
  }

  public JExpression extractBeforeCurrentStatement(TContext context) {
    if (!context.path().last().isBlock() && !context.path().last().isMethod()) {
      return constructorWithSetters;
    }
    final UnusedVariableName name = UnusedVariableName.generate(
      TranslateHelper.uncapitalizeFirst(constructorWithSetters.type().name()), 
      context
    );
    final var variable = new JVariableExpression(name.name());
    constructorWithSetters.anonymousType(null);
    TransformationsQueue.instance().enqueue(
      TransformationsQueue.Target.BLOCK,
      context.path(),
      TransformationsQueue.Priority.IMMEDIATE,
      new DeclarationTransformation(variable, context)
    );
    return variable;
  }
  
  private class DeclarationTransformation implements TransformationsQueue.Transformation {
    
    final JVariableExpression variable;
    final TContext context;

    DeclarationTransformation(JVariableExpression variable, TContext context) {
      this.variable = variable;
      this.context = context;
    }

    @Override
    public void transform(JAstNode node) {
      final JBlockStatement block = (JBlockStatement) node;
      int pos = block.statements().size() - 1;
      final JDeclarationExpression declaration = new JDeclarationExpression(
        JVariableExpression.var(constructorWithSetters.type(), variable.name()),
        constructorWithSetters
      );
      block.statements().add(pos++, new JExpressionStatement(declaration));
      context.declareVariable(declaration.left(), declaration.right());
      for (JMethodCallExpression setter : constructorWithSetters.setterCallsForVariable(variable)) {
        block.statements().add(
          pos++,
          new JExpressionStatement(setter)
        );
      }
    }
  }
}
