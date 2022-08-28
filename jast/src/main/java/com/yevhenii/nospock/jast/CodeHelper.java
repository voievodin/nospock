package com.yevhenii.nospock.jast;

import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JEmptyStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

public class CodeHelper {
  public static void appendX(StringBuilder sb, String what, int times) {
    sb.append(what.repeat(times));
  }

  public static void appendEachNl(
    StringBuilder sb,
    List<? extends JAstNode> jComponents,
    CodeStyle style
  ) {
    for (JAstNode jComponent : jComponents) {
      sb.append(jComponent.asCode(style)).append(style.nlSequence());
    }
  }

  public static void appendAccessModifier(StringBuilder sb, int modifiers) {
    if (Modifier.isPublic(modifiers)) {
      sb.append("public");
    } else if (Modifier.isPrivate(modifiers)) {
      sb.append("private");
    } else if (Modifier.isProtected(modifiers)) {
      sb.append("protected");
    }
  }

  /**
   * Appends to a string builder modifiers in the following order
   * - public
   * - abstract
   * - static
   * - final
   * <p>
   * + space ' '.
   */
  public static void appendModifiers(StringBuilder sb, int modifiers) {
    if (Modifier.isPublic(modifiers)) {
      sb.append("public ");
    } else if (Modifier.isPrivate(modifiers)) {
      sb.append("private ");
    } else if (Modifier.isProtected(modifiers)) {
      sb.append("protected ");
    }
    if (Modifier.isAbstract(modifiers)) {
      sb.append("abstract ");
    }
    if (Modifier.isStatic(modifiers)) {
      sb.append("static ");
    }
    if (Modifier.isFinal(modifiers)) {
      sb.append("final ");
    }
  }

  public static String indent(String code, String nl, String indent) {
    return indent + indentSkippingFirst(code, nl, indent);
  }

  public static String indentSkippingFirst(String code, String nl, String indent) {
    return String.join(nl + indent, code.split(nl));
  }

  /**
   * Common flow to reuse blocks for ifs, loops or other statements.
   *
   * <pre>
   * Having 'while (condition)' in the string builder already,
   * the method will append the following to the builder.
   *
   * - Empty statement.
   * while (condition);
   *
   * - Block statement
   * while (condition) {
   * }
   *
   * - Arbitrary statement (let's say if).
   * while (condition)
   *   if (statement) {
   *   }
   * </pre>
   */
  public static void appendStatementsBlock(
    StringBuilder sb,
    JAstNode block,
    CodeStyle style
  ) {
    if (block instanceof JEmptyStatement) {
      sb.append(";");
    } else if (block instanceof JBlockStatement) {
      sb.append(" ").append(block.asCode(style));
    } else if (block instanceof JStatement) {
      sb.append(style.nlSequence()).append(
        CodeHelper.indent(
          block.asCode(style),
          style.nlSequence(),
          style.indent()
        )
      );
      if (((JStatement) block).endsWithSemicolon()) {
        sb.append(';');
      }
    }
  }

  public static void appendArguments(
    StringBuilder sb,
    List<JAstNode> arguments,
    CodeStyle style
  ) {
    if (useMultilineArguments(arguments, style)) {
      sb.append(style.nlSequence()).append(
        indent(
          arguments.stream()
            .map(argNode -> argNode.asCode(style).trim())
            .collect(Collectors.joining("," + style.nlSequence())),
          style.nlSequence(),
          style.indent()
        )
      ).append(style.nlSequence());
    } else {
      sb.append(
        arguments.stream()
          .map(argNode -> argNode.asCode(style).trim())
          .collect(Collectors.joining(", "))
      );
    }
  }

  private static boolean hasSimpleRepresentationNodesOnly(List<JAstNode> nodes) {
    for (JAstNode argument : nodes) {
      if (!(argument instanceof JConstantExpression
            || argument instanceof JVariableExpression
            || argument instanceof JFieldAccessExpression)) {
        return false;
      }
    }
    return true;
  }

  private static boolean useMultilineArguments(List<JAstNode> arguments, CodeStyle style) {
    return arguments.size() >= style.multilineArgumentsThreshold() * 2 ||
           arguments.size() >= style.multilineArgumentsThreshold() && !hasSimpleRepresentationNodesOnly(arguments);
  }
}
