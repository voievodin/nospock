package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAnnotationUsage;
import com.yevhenii.nospock.jast.JConstructor;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.Objects;

public class ConstructorTranslator implements Translator<ConstructorNode, JConstructor> {

  private final Translator<ClassNode, JType> typeTranslator;
  private final Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator;
  private final StPool stPool;

  public ConstructorTranslator(
    Translator<ClassNode, JType> typeTranslator,
    Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator,
    StPool stPool
  ) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.annotationUsageTranslator = Objects.requireNonNull(annotationUsageTranslator);
    this.stPool = Objects.requireNonNull(stPool);
  }

  @Override
  public JConstructor translate(ConstructorNode node, TContext context) {
    final var constructor = new JConstructor(
      TranslateHelper.className(node.getDeclaringClass().getName()),
      node.getModifiers()
    );
    if (node.getAnnotations() != null) {
      for (AnnotationNode annotation : node.getAnnotations()) {
        constructor.addAnnotation(annotationUsageTranslator.translate(annotation, context));
      }
    }
    if (node.getParameters() != null) {
      for (Parameter parameter : node.getParameters()) {
        constructor.addParameter(
          new JParameter(
            typeTranslator.translate(parameter.getType(), context),
            parameter.getName()
          )
        );
      }
    }
    final BlockStatement code = (BlockStatement) node.getCode();
    if (code != null) {
      for (Statement statement : code.getStatements()) {
        constructor.addStatement(stPool.translate(statement, context));
      }
    }

    return constructor;
  }
}
