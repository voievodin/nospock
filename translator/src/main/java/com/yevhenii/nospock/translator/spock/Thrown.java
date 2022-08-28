package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsDetector;

import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Thrown {

  public static Thrown build(Sections sections) {
    final var thrown = new Thrown();
    for (Section section : sections) {
      for (Statement statement : section.statements) {
        if (!isThrown(statement)) {
          continue;
        }

        Section wrapped = sections.preceding(section, s -> s.label == SpockLabel.WHEN);
        if (wrapped == null) {
          throw new TranslationException("Expected to have 'when:' section when thrown statement is used");
        }

        thrown.thrown2wrapped.put(section.idx, new ArrayList<>());
        thrown.wrap(section, wrapped);

        // every 'and:' following 'when:' is also wrapped. We don't know which 
        // section to wrap in case 'and:' sections follow 'when:' section
        while ((wrapped = sections.next(wrapped)) != null && wrapped.label == SpockLabel.AND) {
          thrown.wrap(section, wrapped);
        }

        break;
      }
    }
    return thrown;
  }

  private final Map<Integer, List<Integer>> thrown2wrapped = new HashMap<>();
  private final Set<Integer> wrapped = new HashSet<>();

  private Thrown() {
  }

  public boolean wraps(Section section) {
    return wrapped.contains(section.idx);
  }

  public List<Statement> wrapped(Section thrownSection, Sections sections) {
    final List<Integer> indexes = thrown2wrapped.get(thrownSection.idx);
    if (indexes == null) {
      return List.of();
    } else {
      return indexes.stream()
        .map(sections::byIndex)
        .flatMap(s -> s.statements.stream())
        .collect(Collectors.toList());
    }
  }

  private void wrap(Section thrown, Section wrapped) {
    this.thrown2wrapped.get(thrown.idx).add(wrapped.idx);
    this.wrapped.add(wrapped.idx);
  }

  private static boolean isThrown(Statement statement) {
    if (!(statement instanceof ExpressionStatement)) {
      return false;
    }
    final var expression = ((ExpressionStatement) statement).getExpression();
    if (expression instanceof MethodCallExpression) {
      return AssertionsDetector.isThrown(((MethodCallExpression) expression));
    }
    if (expression instanceof DeclarationExpression) {
      return AssertionsDetector.isThrown(((DeclarationExpression) expression));
    }
    return false;
  }
}
