package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAnnotationUsage;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.JField;
import com.yevhenii.nospock.translator.spock.mock.MockDetector;
import com.yevhenii.nospock.translator.spock.mock.DetectedMockInitialization;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;
import com.yevhenii.nospock.translator.spock.mock.TranslatedMockInitialization;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.util.Objects;
import java.util.Set;

public class FieldTranslator implements Translator<FieldNode, JField> {

  private static final Set<String> IGNORED_ANNOTATIONS = Set.of("Shared");

  private final Translator<ClassNode, JType> typeTranslator;
  private final Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator;
  private final ExPool exPool;
  private final MockDetector mockDetector;
  private final MockTranslator mockTranslator;
  private final RuntimeLookup runtimeLookup;

  public FieldTranslator(
    Translator<ClassNode, JType> typeTranslator,
    Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator,
    ExPool exPool,
    MockDetector mockDetector,
    MockTranslator mockTranslator,
    RuntimeLookup runtimeLookup
  ) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.annotationUsageTranslator = Objects.requireNonNull(annotationUsageTranslator);
    this.exPool = Objects.requireNonNull(exPool);
    this.mockDetector = Objects.requireNonNull(mockDetector);
    this.mockTranslator = Objects.requireNonNull(mockTranslator);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
  }

  @Override
  public JField translate(FieldNode node, TContext context) {
    final var field = new JField(typeTranslator.translate(node.getType(), context), node.getName());
    field.modifiers(node.getModifiers());
    if (node.getAnnotations() != null) {
      for (AnnotationNode annotation : node.getAnnotations()) {
        if (!IGNORED_ANNOTATIONS.contains(annotation.getClassNode().getUnresolvedName())) {
          field.addAnnotation(annotationUsageTranslator.translate(annotation, context));
        }
      }
    }
    if (node.getInitialExpression() != null) {
      setInitExpression(field, node.getInitialExpression(), context);
    }
    GroovyImplicitImports.explicitlyImport(field.type());
    mockTranslator.adjustTypes(field, context);
    // 'def field = new X()' becomes: 'X field = new X()'
    if (field.type().isJavaLangObject() && node.getInitialExpression() != null) {
      final Class<?> c = runtimeLookup.classes.resolvedBy(node.getInitialExpression(), context);
      if (c != null) {
        field.type(new JType(c));
      }
    }
    return field;
  }

  private void setInitExpression(JField field, Expression expression, TContext context) {
    if (expression instanceof MethodCallExpression) {
      final DetectedMockInitialization mockInitialization = mockDetector.detectInitialization(((MethodCallExpression) expression));
      if (mockInitialization != null) {
        final TranslatedMockInitialization translatedInitialization = mockTranslator.translateInitialization(mockInitialization, context);
        field.initExpression(translatedInitialization);
        TransformationsQueue.instance().enqueueNewImports(translatedInitialization.imports());
        return;
      }
    }

    field.initExpression(exPool.translate(expression, context));
  }
}