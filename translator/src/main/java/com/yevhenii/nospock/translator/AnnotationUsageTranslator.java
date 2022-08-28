package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAnnotationUsage;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Map;

public class AnnotationUsageTranslator implements Translator<AnnotationNode, JAnnotationUsage> {

  private final ExPool exPool;

  public AnnotationUsageTranslator(ExPool exPool) {
    this.exPool = exPool;
  }

  @Override
  public JAnnotationUsage translate(AnnotationNode node, TContext context) {
    final var usage = new JAnnotationUsage(node.getClassNode().getUnresolvedName());
    for (Map.Entry<String, Expression> mEntry : node.getMembers().entrySet()) {
      usage.addMember(mEntry.getKey(), exPool.translate(mEntry.getValue(), context));
    }
    return usage;
  }
}
