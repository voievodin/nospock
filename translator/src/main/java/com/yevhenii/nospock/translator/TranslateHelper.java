package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.translator.spi.TranslationException;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TranslateHelper {

  private static final Map<Character, String> NAME_REPLACEMENTS = Map.of(
    '+', "plus",
    '-', "minus",
    '%', "mod",
    '/', "div",
    '*', "mul",
    '=', "equals",
    '&', "and"
  );

  /**
   * Translates a collection of groovy ast nodes to the collection
   * of java ast nodes using a giving translator. Returns a modifiable list of such items
   * which is never null.
   */
  public static <J_NODE extends JAstNode, G_NODE extends ASTNode> List<J_NODE> translate(
    List<G_NODE> groovyNodes,
    Translator<G_NODE, J_NODE> translator,
    TContext context
  ) {
    List<J_NODE> result = new ArrayList<>();
    if (groovyNodes != null) {
      for (G_NODE groovyNode : groovyNodes) {
        result.add(translator.translate(groovyNode, context));
      }
    }
    return result;
  }

  /**
   * For a field named {@code name} returns a setter name {@code setName}.
   */
  public static String setterForField(String field) {
    return "set" + capitalizeFirst(field);
  }

  /**
   * For a given {@code myVariableName} capitalizes first letter and returns {@code MyVariableName}.
   */
  public static String capitalizeFirst(String name) {
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * For a given {@code MyType} lowers case of the first letter and returns {@code myType}.
   */
  public static String uncapitalizeFirst(String name) {
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * Converts my.package0.Name0$Name1 to Name1.
   */
  public static String className(String name) {
    return lastSegment(lastSegment(name, "."), "$");
  }

  private static String lastSegment(String unresolvedName, String separator) {
    final var idx = unresolvedName.lastIndexOf(separator);
    if (idx == -1) {
      return unresolvedName;
    } else {
      return unresolvedName.substring(idx + 1);
    }
  }

  /**
   * Converts 'method name' to 'methodName' ignoring case of the first word, also
   * removes anything that is not number or character from the name except chars defined
   * in the {@link #NAME_REPLACEMENTS} map.
   */
  public static String asCamelCaseName(String name) {
    String[] parts = name.split("\\s+");
    if (parts.length == 1) {
      return name;
    } else {
      StringBuilder sb = new StringBuilder(nameReplace(parts[0]));
      for (int i = 1; i < parts.length; i++) {
        final var part = nameReplace(parts[i]);
        if (part.length() > 0) {
          sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
      }
      return sb.toString();
    }
  }

  public static <T> List<Class<? extends T>> getAssignableTypes(Class<T> assignableFrom, Class<?> base) {
    List<Class<? extends T>> types = new ArrayList<>();
    getAssignableTypes(assignableFrom, base, types);
    return types;
  }

  public static String classNameFromClassLiteralExpression(Expression expression) {
    if (expression instanceof VariableExpression) {
      // e.g. thrown(X)
      return expression.getText();
    } else if (expression instanceof PropertyExpression) {
      // e.g. thrown(X.class) or inner class, like Outer.InnerException, Outer.InnerException.class
      final var property = (PropertyExpression) expression;
      if (property.getPropertyAsString().equals("class")) {
        return property.getObjectExpression().getText();
      } else {
        return property.getText();
      }
    } else {
      return null;
    }
  }

  /**
   * Context independent correction of type for given expression knowing the type expected.
   * In case safe conversion is impossible the given expression is returned back.
   */
  public static JExpression correctType(Class<?> expected, JExpression exp) {
    if (exp instanceof JConstantExpression) {
      final var cExp = (JConstantExpression) exp;
      if (expected == Long.class && cExp.value() instanceof Integer) {
        return new JConstantExpression(((Integer) cExp.value()).longValue());
      }
    }
    return exp;
  }

  // for not all the nested statements because there are no translators for most of the statements yet
  public static <T extends JStatement> List<T> findNestedStatementsOfType(JBlockStatement block, Class<T> c) {
    final var result = new ArrayList<T>();
    Queue<JStatement> q = new ArrayDeque<>();
    q.add(block);
    while (!q.isEmpty()) {
      final JStatement statement = q.poll();
      if (c.isAssignableFrom(statement.getClass())) {
        result.add(c.cast(statement));
      }
      q.addAll(statement.getNestedStatements());
    }
    return result;
  }

  public static Set<Method> methods(Class<?> c) {
    final var methods = new HashSet<Method>();
    Collections.addAll(methods, c.getMethods());
    Collections.addAll(methods, c.getDeclaredMethods());
    return methods;
  }

  public static Field getField(Class<?> c, String name) {
    try {
      return c.getDeclaredField(name);
    } catch (NoSuchFieldException ignored) {
      try {
        return c.getField(name);
      } catch (NoSuchFieldException ignored2) {
        return null;
      }
    }
  }

  public static Class<?> getFieldType(Class<?> c, String name) {
    if (c == null) {
      return null;
    } else {
      final Field f = getField(c, name);
      if (f == null) {
        return null;
      } else {
        return f.getType();
      }
    }
  }

  public static <T extends Expression> T extractExpression(Statement statement, Class<T> exClass) {
    if (!(statement instanceof ExpressionStatement)) {
      throw new TranslationException("Cannot extract expression from on expression statement");
    }
    final var exStmt = (ExpressionStatement) statement;
    if (!exClass.isAssignableFrom(exStmt.getExpression().getClass())) {
      throw new TranslationException(String.format("Expression '%s' is not assignable from '%s'", exStmt.getExpression(), exClass));
    }
    return exClass.cast(exStmt.getExpression());
  }
  
  @SuppressWarnings("unchecked")
  private static <T> void getAssignableTypes(Class<T> assignableFrom, Class<?> base, List<Class<? extends T>> container) {
    if (assignableFrom.isAssignableFrom(base.getSuperclass())) {
      container.add((Class<T>) base.getSuperclass());
      getAssignableTypes(assignableFrom, base.getSuperclass(), container);
    }
    for (Class<?> anInterface : base.getInterfaces()) {
      if (assignableFrom.isAssignableFrom(anInterface)) {
        container.add((Class<T>) anInterface);
      }
    }
  }

  private static String nameReplace(String name) {
    final var sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      final var c = name.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        sb.append(c);
      } else {
        final var replacement = NAME_REPLACEMENTS.get(c);
        if (replacement != null) {
          sb.append(replacement);
        }
      }
    }
    return sb.toString();
  }
}
