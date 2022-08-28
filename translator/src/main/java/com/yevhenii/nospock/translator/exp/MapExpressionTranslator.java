package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.spock.JForeignExpression;

import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapExpressionTranslator implements ExpressionTranslator<MapExpression, JExpression> {

  private final ExPool exPool;

  public MapExpressionTranslator(ExPool exPool) {
    this.exPool = Objects.requireNonNull(exPool);
  }

  @Override
  public Class<MapExpression> getTranslatedType() {
    return MapExpression.class;
  }

  @Override
  public JExpression translate(MapExpression node, TContext context) {
    return new JForeignExpression(
      new JMethodCallExpression(
        new JVariableExpression("Map"),
        "of",
        asArguments(node.getMapEntryExpressions(), context)
      ),
      List.of(new JImport("java.util.Map", false))
    );
  }

  private List<JExpression> asArguments(List<MapEntryExpression> meExpressions, TContext context) {
    if (meExpressions == null) {
      return List.of();
    }
    return meExpressions.stream()
      .flatMap(meExp -> Stream.of(meExp.getKeyExpression(), meExp.getValueExpression()))
      .map(ex -> exPool.translate(ex, context))
      .collect(Collectors.toList());
  }
}
