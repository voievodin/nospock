package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;

import org.codehaus.groovy.ast.ASTNode;

/**
 * Translates groovy ast node to java ast node.
 */
public interface Translator<T extends ASTNode, R extends JAstNode> {
  R translate(T node, TContext context);
}
