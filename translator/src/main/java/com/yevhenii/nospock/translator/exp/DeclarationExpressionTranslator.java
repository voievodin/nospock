package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JDeclarationExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.translator.ConstructorCallInitializedWithSetters;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.GroovyImplicitImports;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TransformationsQueue;
import com.yevhenii.nospock.translator.ValueAdjuster;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;

import org.codehaus.groovy.ast.expr.DeclarationExpression;

import java.util.Objects;
import java.util.stream.Collectors;

public class DeclarationExpressionTranslator implements ExpressionTranslator<DeclarationExpression, JDeclarationExpression> {

  private final ExPool exPool;
  private final MockTranslator mockTranslator;
  private final RuntimeLookup runtimeLookup;
  private final ValueAdjuster valueAdjuster;

  public DeclarationExpressionTranslator(
    ExPool exPool, 
    MockTranslator mockTranslator, 
    RuntimeLookup runtimeLookup,
    ValueAdjuster valueAdjuster
  ) {
    this.exPool = Objects.requireNonNull(exPool);
    this.mockTranslator = Objects.requireNonNull(mockTranslator);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
    this.valueAdjuster = Objects.requireNonNull(valueAdjuster);
  }

  @Override
  public JDeclarationExpression translate(DeclarationExpression node, TContext context) {
    final JExpression left = exPool.translate(node.getLeftExpression(), context);
    final JExpression right = exPool.translate(node.getRightExpression(), context);

    // In case local variable is defined using 'def' (which usually resolves to object) we use 'var'
    // maybe this is something worth tweaking. Note that otherwise we will have to
    // either use Object type or try to derive type from the right side, which is not
    // always possible or requires high effort which I'm not yet ready to put into this.
    if (left instanceof JVariableExpression) {
      final var jVExp = ((JVariableExpression) left);
      
      // declaration expression always makes variable a declaration
      if (!jVExp.isDeclaration()) {
        jVExp.isDeclaration(true);
        if (jVExp.type().isJavaLangObject()) {
          jVExp.useVar(true);
        }
      }

      // even though var is used in case dyn typed, try deriving type from right
      // to ensure we have fully scoped context for further analysis
      if (jVExp.type().isJavaLangObject()) {
        final Class<?> c = runtimeLookup.classes.resolvedBy(node.getRightExpression(), context);
        if (c != null) {
          jVExp.type(new JType(c));
        }
      }
    }

    JDeclarationExpression adjusted = mockTranslator.adjustTypes(new JDeclarationExpression(left, right), context);

    // when constructor is used with init section that has setters only
    // inline it to be declaration + init statements following the declaration
    if (adjusted.right() instanceof ConstructorCallInitializedWithSetters && adjusted.left() instanceof JVariableExpression) {
      final var cCall = (ConstructorCallInitializedWithSetters) adjusted.right();
      final var variableName = new JVariableExpression(((JVariableExpression) adjusted.left()).name());
      cCall.anonymousType(null);
      TransformationsQueue.instance().enqueue(
        TransformationsQueue.Target.BLOCK,
        context.path(),
        TransformationsQueue.Priority.IMMEDIATE,
        block -> ((JBlockStatement) block).statements().addAll(
          cCall.setterCallsForVariable(variableName)
            .stream()
            .map(JExpressionStatement::new)
            .collect(Collectors.toList())
        )
      );
    }

    if (valueAdjuster.canPossiblyAdjust(adjusted.right()) && adjusted.left() instanceof JVariableExpression) {
      adjusted = new JDeclarationExpression(
        adjusted.left(), 
        valueAdjuster.adjust(
          ((JVariableExpression) adjusted.left()).type(), 
          adjusted.right()
        )
      );
    }

    // declare once all types resolved
    context.declareVariable(left, right);

    GroovyImplicitImports.explicitlyImport(adjusted.left().resolveType());
    if (adjusted.right() != null) {
      GroovyImplicitImports.explicitlyImport(adjusted.right().resolveType());
    }
    
    return adjusted;
  }

  @Override
  public Class<DeclarationExpression> getTranslatedType() {
    return DeclarationExpression.class;
  }
}
