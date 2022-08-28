package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import org.codehaus.groovy.ast.ASTNode;

import java.util.Objects;

public class NodeTranslation<G extends ASTNode, J extends JAstNode> implements Translation<G, J> {

  public final TKey<G, J> key;
  public final Translator<G, J> translator;

  public NodeTranslation(TKey<G, J> key, Translator<G, J> translator) {
    this.key = Objects.requireNonNull(key);
    this.translator = Objects.requireNonNull(translator);
  }

  @Override
  public TKey<G, J> key() {
    return key;
  }

  @Override
  public Translator<G, J> translator() {
    return translator;
  }
}
