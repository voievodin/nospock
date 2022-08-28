package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.exp.JExpression;

import org.codehaus.groovy.ast.expr.Expression;

/**
 * Expression translators must use this interface to part of the pool {@link ExPool}.
 */
public interface ExpressionTranslator<G_NODE extends Expression, J_NODE extends JExpression> extends Translator<G_NODE, J_NODE> {
  Class<G_NODE> getTranslatedType();
}
