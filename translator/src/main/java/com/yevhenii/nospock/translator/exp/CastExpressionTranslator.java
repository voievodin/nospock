package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JCastExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.GroovyImplicitImports;
import com.yevhenii.nospock.translator.Translator;
import com.yevhenii.nospock.translator.TypeTranslator;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.CastExpression;

import java.util.Objects;

public class CastExpressionTranslator implements ExpressionTranslator<CastExpression, JCastExpression> {

  private final Translator<ClassNode, JType> typeTranslator;
  private final ExPool exPool;

  public CastExpressionTranslator(Translator<ClassNode, JType> typeTranslator, ExPool exPool) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<CastExpression> getTranslatedType() {
    return CastExpression.class;
  }

  @Override
  public JCastExpression translate(CastExpression node, TContext context) {
    final JType type = typeTranslator.translate(node.getType(), context);
    GroovyImplicitImports.explicitlyImport(type);
    return new JCastExpression(
      type,
      exPool.translate(node.getExpression(), context)
    );
  }
}
