package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.translator.spock.JForeignStatement;

import org.codehaus.groovy.ast.stmt.Statement;

import java.util.HashMap;
import java.util.Map;

public class StPool implements Translator<Statement, JStatement> {

  private final Map<Class<? extends Statement>, StatementTranslator<?, ?>> registry = new HashMap<>();
  private final Map<Class<? extends Statement>, StatementTranslator<?, ?>> derivedCache = new HashMap<>();

  public void register(StatementTranslator<?, ?> translator) {
    final StatementTranslator<?, ?> existingTranslator = registry.put(translator.getTranslatedType(), translator);
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
  public JStatement translate(Statement node, TContext context) {
    StatementTranslator translator = getTranslator(node.getClass());
    if (translator == null) {
      throw new TranslationException("Statement class handler isn't registered in the pool " + node.getClass());
    }
    final var translated = (JStatement) translator.translate(node, context);
    if (translated instanceof JForeignStatement) {
      TransformationsQueue.instance().enqueueNewImports(((JForeignStatement) translated).imports());
    }
    return translated;
  }

  @SuppressWarnings("all")
  private StatementTranslator<?, ?> getTranslator(Class<?> clazz) {
    StatementTranslator<?, ?> translator = registry.get(clazz);
    if (translator == null) {
      translator = derivedCache.get(clazz);
      if (translator == null) {
        for (Class<? extends Statement> type : TranslateHelper.getAssignableTypes(Statement.class, clazz)) {
          translator = registry.get(type);
          if (translator != null) {
            derivedCache.put((Class<? extends Statement>) clazz, translator);
            break;
          }
        }
      }
    }
    return translator;
  }
}
