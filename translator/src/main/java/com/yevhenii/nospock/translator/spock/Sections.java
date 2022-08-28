package com.yevhenii.nospock.translator.spock;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Structured representation of spock test method statements.
 */
public class Sections implements Iterable<Section> {

  public static Sections group(List<Statement> statements) {
    final Sections sections = new Sections();
    Section section = null;
    int idx = 0;
    int subFlowIdx = 0;
    boolean whenSectionAppearedAtLeastOnce = false;
    for (Statement statement : statements) {
      final SpockLabel label = getSpockLabel(statement.getStatementLabels());
      // label occurrence within the text, e.g 'given:'
      if (label != null) {
        if (label == SpockLabel.WHEN) {
          if (whenSectionAppearedAtLeastOnce) {
            subFlowIdx++;
          }
          whenSectionAppearedAtLeastOnce = true;
        }

        if (section != null) {
          sections.add(section);
        }
        section = new Section(label, idx++, subFlowIdx);
      }

      // when label is used with comment, like where: "call the actual service"
      if (isConstant(statement) && label != null) {
        section.comment = statement.getText();
        sections.atLeastOneSectionHasComment = true;
      } else if (section != null) {
        section.statements.add(statement);
      }
    }
    sections.add(section);
    return sections;
  }

  public static SpockLabel getSpockLabel(List<String> labels) {
    if (labels != null) {
      for (String label : labels) {
        if (SpockLabel.isKnown(label)) {
          return SpockLabel.valueOf(label.toUpperCase());
        }
      }
    }
    return null;
  }

  private final List<Section> sections = new ArrayList<>();
  private final Map<Integer, Section> idx2section = new HashMap<>();
  private boolean atLeastOneSectionHasComment = false;

  private Sections() {
  }

  public boolean atLeastOneSectionHasComment() {
    return atLeastOneSectionHasComment;
  }

  public Section byLabel(SpockLabel label) {
    for (final Section section : sections) {
      if (section.label == label) {
        return section;
      }
    }
    return null;
  }

  public Section byIndex(int idx) {
    return idx2section.get(idx);
  }

  @Override
  public Iterator<Section> iterator() {
    return sections.iterator();
  }

  public Section preceding(Section section, Predicate<Section> matcher) {
    for (int i = section.idx - 1; i >= 0; i--) {
      final Section prev = sections.get(i);
      if (matcher.test(prev)) {
        return prev;
      }
    }
    return null;
  }

  public List<Section> allPrecedingByFlowId(Section section, int flowId) {
    final List<Section> result = new ArrayList<>();
    for (int i = section.idx - 1; i >= 0; i--) {
      final Section prev = sections.get(i);
      if (prev.subFlowIdx != flowId) {
        break;
      }
      result.add(prev);
    }
    return result;
  }

  public Section following(Section section, Predicate<Section> matcher) {
    for (int i = section.idx + 1; i < sections.size(); i++) {
      final Section next = sections.get(i);
      if (matcher.test(next)) {
        return next;
      }
    }
    return null;
  }

  public Section next(Section section) {
    if (section.idx == sections.size() - 1) {
      return null;
    } else {
      return sections.get(section.idx + 1);
    }
  }

  private void add(Section section) {
    sections.add(section);
    idx2section.put(section.idx, section);
  }

  private static boolean isConstant(Statement statement) {
    return (statement instanceof ExpressionStatement)
           && ((ExpressionStatement) statement).getExpression() instanceof ConstantExpression;
  }
}
