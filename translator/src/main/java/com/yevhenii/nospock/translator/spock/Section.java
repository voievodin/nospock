package com.yevhenii.nospock.translator.spock;

import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.List;

public class Section {
  final SpockLabel label;
  final int idx;
  final int subFlowIdx;
  final List<Statement> statements = new ArrayList<>();

  String comment = "";

  Section(SpockLabel label, int idx, int subFlowIdx) {
    this.label = label;
    this.idx = idx;
    this.subFlowIdx = subFlowIdx;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Section)) {
      return false;
    }
    final Section that = (Section) obj;
    return idx == that.idx;
  }

  @Override
  public int hashCode() {
    return idx;
  }

  public String getLabelWithComment() {
    if (comment.isEmpty()) {
      return label.name().toLowerCase();
    } else {
      return label.name().toLowerCase() + ": " + comment;
    }
  }
}
