package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JClassLiteralExpression;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.GroovyImplicitImports;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.Translator;
import com.yevhenii.nospock.translator.TypeTranslator;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;

import java.util.Objects;

public class ClassExpressionTranslator implements ExpressionTranslator<ClassExpression, JClassLiteralExpression> {

  private final Translator<ClassNode, JType> typeTranslator;

  public ClassExpressionTranslator(Translator<ClassNode, JType> typeTranslator) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
  }

  @Override
  public Class<ClassExpression> getTranslatedType() {
    return ClassExpression.class;
  }

  @Override
  public JClassLiteralExpression translate(ClassExpression node, TContext context) {
    final var type = typeTranslator.translate(node.getType(), context);
    GroovyImplicitImports.explicitlyImport(type);
    return new JClassLiteralExpression(type);
  }
}
