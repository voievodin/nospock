package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import org.codehaus.groovy.ast.ASTNode;

import java.util.Objects;

/**
 * Component that configures full support for arbitrary node translation.
 */
public interface Translation<G extends ASTNode, J extends JAstNode> {
  TKey<G, J> key();
  Translator<G, J> translator();
}
