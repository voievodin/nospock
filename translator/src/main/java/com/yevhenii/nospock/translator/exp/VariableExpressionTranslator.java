package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.GroovyImplicitImports;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TypeTranslator;

import org.codehaus.groovy.ast.expr.VariableExpression;

public class VariableExpressionTranslator implements ExpressionTranslator<VariableExpression, JVariableExpression> {

  private final TypeTranslator typeTranslator = new TypeTranslator();

  @Override
  public JVariableExpression translate(VariableExpression node, TContext context) {
    final var result = new JVariableExpression(
      typeTranslator.translate(node.getOriginType(), context),
      node.getText()
    );
    GroovyImplicitImports.explicitlyImport(result.type());
    return result;
  }

  @Override
  public Class<VariableExpression> getTranslatedType() {
    return VariableExpression.class;
  }
}
