package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JImport;

import org.codehaus.groovy.ast.ImportNode;

public class ImportTranslator implements Translator<ImportNode, JImport> {

  @Override
  public JImport translate(ImportNode node, TContext context) {
    String tmpFqn;
    if (node.getType() == null) {
      // java.util
      tmpFqn = node.getPackageName().substring(0, node.getPackageName().length() - 1);
    } else {
      // java.util.List
      tmpFqn = node.getType().getName();
    }

    if (node.isStar()) {
      // java.util.*, java.util.List.*;
      tmpFqn += ".*";
    } else if (node.isStatic()) {
      // java.util.List.of;
      tmpFqn += '.' + node.getAlias();
    }

    return new JImport(tmpFqn, node.isStatic());
  }
}
