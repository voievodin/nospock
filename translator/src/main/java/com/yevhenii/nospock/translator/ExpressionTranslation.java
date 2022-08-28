package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesType;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Objects;

/**
 * Component that configures full support for expression translation.
 */
public class ExpressionTranslation<G extends Expression, J extends JExpression> extends NodeTranslation<G, J> {

  private final ResolvesType<G> typeResolver;

  public ExpressionTranslation(TKey<G, J> key, ExpressionTranslator<G, J> translator, ResolvesType<G> typeResolver) {
    super(key, translator);
    this.typeResolver = Objects.requireNonNull(typeResolver);
  }

  public ResolvesType<G> typeResolver() {
    return typeResolver;
  }
}
