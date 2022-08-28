package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.translator.spock.JForeignAstNode;

import org.codehaus.groovy.ast.expr.Expression;

import java.util.HashMap;
import java.util.Map;

public class ExPool implements Translator<Expression, JExpression> {

  private final Map<Class<? extends Expression>, ExpressionTranslator<?, ?>> registry = new HashMap<>();
  private final Map<Class<? extends Expression>, ExpressionTranslator<?, ?>> derivedCache = new HashMap<>();

  public void register(ExpressionTranslator<?, ?> translator) {
    final ExpressionTranslator<?, ?> existingTranslator = registry.put(translator.getTranslatedType(), translator);
    if (existingTranslator != null) {
      throw new TranslationException(
        "Expression translator registered twice for type " + translator.getTranslatedType() +
        ", existing: " + existingTranslator +
        " new: " + translator
      );
    }
  }

  @Override
  @SuppressWarnings("all")
  public JExpression translate(Expression node, TContext context) {
    final ExpressionTranslator translator = getTranslator(node.getClass());
    if (translator == null) {
      throw new TranslationException("Not supported expression type " + node);
    } else {
      final var translated = translator.translate(node, context);
      if (translated instanceof JForeignAstNode) {
        TransformationsQueue.instance().enqueueNewImports(((JForeignAstNode) translated).imports());
      }
      return (JExpression) translated;
    }
  }

  @SuppressWarnings("all")
  private ExpressionTranslator<?, ?> getTranslator(Class<?> clazz) {
    ExpressionTranslator<?, ?> translator = registry.get(clazz);
    if (translator == null) {
      translator = derivedCache.get(clazz);
      if (translator == null) {
        for (Class<? extends Expression> type : TranslateHelper.getAssignableTypes(Expression.class, clazz)) {
          translator = registry.get(type);
          if (translator != null) {
            derivedCache.put((Class<? extends Expression>) clazz, translator);
            break;
          }
        }
      }
    }
    return translator;
  }
}
