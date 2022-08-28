package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.*;
import com.yevhenii.nospock.translator.TransformationsQueue.Priority;
import com.yevhenii.nospock.translator.TransformationsQueue.Target;
import com.yevhenii.nospock.translator.spock.LifecycleTestMethodTranslator;
import com.yevhenii.nospock.translator.spock.TestMethodTranslator;

import org.codehaus.groovy.ast.*;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClassTranslator implements Translator<ClassNode, JClass> {

  private static final Set<String> IGNORED_ENUM_FIELDS = Set.of("MAX_VALUE", "MIN_VALUE", "$VALUES");

  private final Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator;
  private final Translator<ClassNode, JType> typeTranslator;
  private final Translator<FieldNode, JField> fieldTranslator;
  private final Translator<ConstructorNode, JConstructor> constructorTranslator;
  private final Translator<MethodNode, JMethod> methodTranslator;
  private final TestMethodTranslator testMethodTranslator;
  private final LifecycleTestMethodTranslator lifecycleTestMethodTranslator;

  public ClassTranslator(
    Translator<AnnotationNode, JAnnotationUsage> annotationUsageTranslator,
    Translator<ClassNode, JType> typeTranslator,
    Translator<FieldNode, JField> fieldTranslator,
    Translator<ConstructorNode, JConstructor> constructorTranslator,
    Translator<MethodNode, JMethod> methodTranslator,
    TestMethodTranslator testMethodTranslator,
    LifecycleTestMethodTranslator lifecycleTestMethodTranslator
  ) {
    this.annotationUsageTranslator = Objects.requireNonNull(annotationUsageTranslator);
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
    this.fieldTranslator = Objects.requireNonNull(fieldTranslator);
    this.constructorTranslator = Objects.requireNonNull(constructorTranslator);
    this.methodTranslator = Objects.requireNonNull(methodTranslator);
    this.testMethodTranslator = Objects.requireNonNull(testMethodTranslator);
    this.lifecycleTestMethodTranslator = Objects.requireNonNull(lifecycleTestMethodTranslator);
  }

  @Override
  public JClass translate(ClassNode node, TContext context) {
    context = context.deepen(CPath.Seg.forClass(node.getName()));
    final var jClass = new JClass(TranslateHelper.className(node.getUnresolvedName()));

    jClass.modifiers(node.getModifiers());

    if (node.isEnum()) {
      jClass.classType(JClassType.ENUM);
    } else if (node.isAnnotationDefinition()) {
      jClass.classType(JClassType.ANNOTATION);
    } else if (node.isInterface()) {
      jClass.classType(JClassType.INTERFACE);
    } else {
      jClass.classType(JClassType.CLASS);
    }

    if (node.getAnnotations() != null) {
      for (AnnotationNode annotation : node.getAnnotations()) {
        jClass.addAnnotation(annotationUsageTranslator.translate(annotation, context));
      }
    }

    // Might need more advanced representation
    if (node.getGenericsTypes() != null) {
      for (GenericsType genericsType : node.getGenericsTypes()) {
        jClass.addGeneric(typeTranslator.translate(genericsType.getType(), context));
      }
    }

    if (node.getUnresolvedSuperClass() != null
        && !node.getUnresolvedSuperClass().getName().equals("java.lang.Object")
        && !node.getUnresolvedSuperClass().getName().equals("Specification")) {
      jClass.superclass(typeTranslator.translate(node.getUnresolvedSuperClass(), context));
    }

    if (node.getInterfaces() != null) {
      for (ClassNode anInterface : node.getInterfaces()) {
        jClass.addInterfaces(typeTranslator.translate(anInterface, context));
      }
    }

    if (node.getFields() != null) {
      for (FieldNode fNode : node.getFields()) {
        // fields like MIN_VALUE, MAX_VALUE won't be added
        if (node.isEnum() && isIgnoredEnumField(fNode)) {
          continue;
        }

        final JField f = fieldTranslator.translate(fNode, context);
        if (fNode.isEnum()) {
          f.setIsEnumValue();
        }

        jClass.addField(f);
        context.declareField(f);
      }
    }

    if (node.getDeclaredConstructors() != null) {
      for (ConstructorNode constructor : node.getDeclaredConstructors()) {
        jClass.addConstructor(constructorTranslator.translate(constructor, context));
        TransformationsQueue.transform(Target.CLASS, context.path(), Priority.IMMEDIATE, jClass);
      }
    }

    if (node.getMethods() != null) {
      for (MethodNode method : node.getMethods()) {
        // e.g. enums have ~5 generated or synthetic methods
        if (!isGenerated(method) && !method.isSynthetic()) {
          jClass.addMethod(translator(method).translate(method, context));
          TransformationsQueue.transform(Target.CLASS, context.path(), Priority.IMMEDIATE, jClass);
        }
      }
    }

    if (node.getInnerClasses() != null) {
      final Iterator<InnerClassNode> it = node.getInnerClasses();
      while (it.hasNext()) {
        final InnerClassNode inClass = it.next();
        final JClass translated = translate(inClass, context);
        if (translated.classType() == JClassType.ENUM || translated.classType() == JClassType.INTERFACE) {
          translated.modifiers(translated.modifiers() & ~Modifier.STATIC);
        }
        jClass.addInnerClass(translated);
      }
    }

    TransformationsQueue.transformAll(Target.CLASS, context.path(), jClass);

    return jClass;
  }

  private Translator<MethodNode, JMethod> translator(MethodNode method) {
    if (testMethodTranslator.canTranslate(method)) {
      return testMethodTranslator;
    } else if (lifecycleTestMethodTranslator.canTranslate(method)) {
      return lifecycleTestMethodTranslator;
    } else {
      return methodTranslator;
    }
  }

  private boolean isGenerated(MethodNode node) {
    final List<AnnotationNode> annotations = node.getAnnotations();
    if (annotations == null || annotations.isEmpty()) {
      return false;
    }
    return "groovy.transform.Generated".equals(annotations.get(0).getClassNode().getName());
  }

  private boolean isIgnoredEnumField(FieldNode node) {
    return IGNORED_ENUM_FIELDS.contains(node.getName());
  }
}
