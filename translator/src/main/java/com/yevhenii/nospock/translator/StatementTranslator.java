package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.stmt.JStatement;

import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Statement translators must use this interface to part of the pool {@link StPool}.
 */
public interface StatementTranslator<G_NODE extends Statement, J_NODE extends JStatement> extends Translator<G_NODE, J_NODE> {
  Class<G_NODE> getTranslatedType();
}
