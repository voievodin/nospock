package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JPackage;

import org.codehaus.groovy.ast.PackageNode;

public class PackageTranslator implements Translator<PackageNode, JPackage> {
  @Override
  public JPackage translate(PackageNode node, TContext context) {
    return new JPackage(node.getName().substring(0, node.getName().length() - 1));
  }
}
