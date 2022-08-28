package com.yevhenii.nospock.translator.exp;

import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.exp.JLambdaExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.translator.CPath;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.ExpressionTranslator;
import com.yevhenii.nospock.translator.IdSequence;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.Translator;

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClosureExpressionTranslator implements ExpressionTranslator<ClosureExpression, JLambdaExpression> {

  private final Translator<BlockStatement, JBlockStatement> blockTranslator;
  private final Translator<Parameter, JParameter> parameterTranslator;
  private final StPool stPool;

  public ClosureExpressionTranslator(
    Translator<BlockStatement, JBlockStatement> blockTranslator,
    Translator<Parameter, JParameter> parameterTranslator,
    StPool stPool
  ) {
    this.blockTranslator = Objects.requireNonNull(blockTranslator);
    this.parameterTranslator = Objects.requireNonNull(parameterTranslator);
    this.stPool = Objects.requireNonNull(stPool);
  }

  @Override
  public JLambdaExpression translate(ClosureExpression node, TContext context) {
    context = context.deepen(CPath.Seg.forLambda(Integer.toString(IdSequence.next())));
    final List<JParameter> parameters;
    if (node.getParameters() == null) {
      parameters = List.of();
    } else {
      parameters = TranslateHelper.translate(
        Arrays.asList(node.getParameters()),
        parameterTranslator,
        context
      );
    }
    context.declareLambdaParameters(parameters, context.path());
    final JBlockStatement block;
    if (node.getCode() instanceof BlockStatement) {
      block = blockTranslator.translate(((BlockStatement) node.getCode()), context);
    } else {
      block = new JBlockStatement(stPool.translate(node.getCode(), context));
    }
    block.formatting().doNotUseBracesIfSingleStatement();
    return new JLambdaExpression(parameters, block);
  }

  @Override
  public Class<ClosureExpression> getTranslatedType() {
    return ClosureExpression.class;
  }
}
