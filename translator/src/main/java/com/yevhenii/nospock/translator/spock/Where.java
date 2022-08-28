package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.translator.spi.TranslationException;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yevhenii.nospock.translator.TranslateHelper.extractExpression;

/**
 * Represents 'where' block in as a collection of value columns,
 * where each column defines values supplied to test.
 */
public class Where {

  public static Where from(List<Statement> statements) {
    if (statements.isEmpty()) {
      throw new TranslationException("Where clause with empty statements");
    }
    Statement first = statements.get(0);
    if (!(first instanceof ExpressionStatement)) {
      throw new TranslationException("Expected first statement in where clause to be expression statement");
    }
    final var firstExpression = (ExpressionStatement) first;
    if (!(firstExpression.getExpression() instanceof BinaryExpression)) {
      throw new TranslationException("Expected first expression to binary");
    }
    final var firstBinary = ((BinaryExpression) firstExpression.getExpression());
    if (firstBinary.getOperation().getText().equals("|") || firstBinary.getOperation().getText().equals("||")) {
      return buildFromTable(firstBinary, statements);
    } else if (firstBinary.getOperation().getText().equals("<<")) {
      return buildFromShiftOperators(statements);
    } else if (firstBinary.getOperation().getText().equals("=")) {
      return buildFromDirectlyAssignedValues(statements);
    } else {
      throw new TranslationException(
        String.format(
          "Binary operation '%s' isn't supported for where clause",
          firstBinary.getOperation().getText()
        )
      );
    }
  }

  private final List<Column> columns;

  private Where(List<Column> columns) {
    this.columns = columns;
  }

  public List<Column> columns() {
    return columns;
  }

  static class Column {
    final String name;
    final List<Expression> values;
    final Expression valuesProvider;

    private Column(String name) {
      this(name, new ArrayList<>());
    }

    private Column(String name, List<Expression> values) {
      this.name = name;
      this.values = new ArrayList<>(values);
      this.valuesProvider = null;
    }

    private Column(String name, Expression valuesProvider) {
      this.name = name;
      this.values = List.of();
      this.valuesProvider = valuesProvider;
    }
  }

  private static Where buildFromTable(BinaryExpression header, List<Statement> statements) {
    final var where = new Where(new ArrayList<>());
    for (Expression columnName : getTableRowValues(header)) {
      where.columns.add(new Column(columnName.getText()));
    }
    for (int i = 1; i < statements.size(); i++) {
      final List<Expression> values = getTableRowValues(extractExpression(statements.get(i), BinaryExpression.class));
      for (int j = 0; j < values.size(); j++) {
        where.columns.get(j).values.add(values.get(j));
      }
    }
    Collections.reverse(where.columns);
    return where;
  }

  private static Where buildFromShiftOperators(List<Statement> statements) {
    final var where = new Where(new ArrayList<>());
    for (Statement statement : statements) {
      final BinaryExpression binary = extractExpression(statement, BinaryExpression.class);
      if (!(binary.getOperation().getText().equals("<<"))) {
        throw new TranslationException("Expected to have << binary expressions in where clause");
      }
      if (binary.getRightExpression() instanceof ListExpression) {
        where.columns.add(
          new Column(
            binary.getLeftExpression().getText(),
            ((ListExpression) binary.getRightExpression()).getExpressions()
          )
        );
      } else {
        where.columns.add(new Column(binary.getLeftExpression().getText(), binary.getRightExpression()));
      }
    }
    return where;
  }

  private static Where buildFromDirectlyAssignedValues(List<Statement> statements) {
    final var where = new Where(new ArrayList<>());
    for (Statement statement : statements) {
      final BinaryExpression binary = extractExpression(statement, BinaryExpression.class);
      if (!(binary.getLeftExpression() instanceof VariableExpression)) {
        throw new TranslationException("Expected to have left of '=' binary expression as variable expression");
      }
      final VariableExpression vExp = (VariableExpression) binary.getLeftExpression();
      where.columns.add(new Column(vExp.getName(), List.of(binary.getRightExpression())));
    }
    return where;
  }

  private static List<Expression> getTableRowValues(BinaryExpression row) {
    final var results = new ArrayList<Expression>();
    walkThroughBinaryTreeRightToLeftDeeply(row, results);
    return results;
  }

  /**
   * Binary expressions form binary trees. In order to get all row values we need to walk through such tree from right to left.
   * The key advantage of such algorithm is that it handles expressions with single and double pipe operators.
   *
   * <pre>
   *   Where header: a | b | c || x | y.
   *   Binary expression tree:
   *
   *             "||"
   *             /  \
   *            /    \
   *           /      \
   *         "|"      "|"
   *         / \      / \
   *        /  "c"  "x" "y"
   *      "|"
   *      / \
   *    "a" "b"
   * </pre>
   * <p>
   * By walking the tree above from right to left we get the desired set of values: {@code "y", "x", "c", "b", "a"}.
   */
  private static void walkThroughBinaryTreeRightToLeftDeeply(BinaryExpression root, List<Expression> values) {
    final var right = root.getRightExpression();
    final var left = root.getLeftExpression();
    if (right instanceof BinaryExpression) {
      walkThroughBinaryTreeRightToLeftDeeply((BinaryExpression) right, values);
    } else {
      values.add(right);
    }
    if (left instanceof BinaryExpression) {
      walkThroughBinaryTreeRightToLeftDeeply((BinaryExpression) left, values);
    } else {
      values.add(left);
    }
  }
}
