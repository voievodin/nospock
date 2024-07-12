package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JType;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;

public class TypeTranslator implements Translator<ClassNode, JType> {
  @Override
  public JType translate(ClassNode node, TContext context) {
    final var jType = new JType(node.isArray() ? node.getComponentType().toString(false) + "[]" : node.getUnresolvedName());
    if (node.getGenericsTypes() != null) {
      jType.useGenerics(true);
      for (GenericsType genericsType : node.getGenericsTypes()) {
        jType.addGeneric(translate(genericsType.getType(), context));
      }
    }
    return jType;
  }
}
