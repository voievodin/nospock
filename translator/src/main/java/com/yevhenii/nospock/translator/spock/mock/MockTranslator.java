package com.yevhenii.nospock.translator.spock.mock;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JBinaryExpression;
import com.yevhenii.nospock.jast.exp.JClassLiteralExpression;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JDeclarationExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.exp.JLambdaExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JReturnStatement;
import com.yevhenii.nospock.translator.ConstructorCallInitializedWithSetters;
import com.yevhenii.nospock.translator.ConstructorToVariableExtractor;
import com.yevhenii.nospock.translator.RuntimeLookup;
import com.yevhenii.nospock.translator.TransformationsQueue;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.Translator;
import com.yevhenii.nospock.translator.TypeLoader;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.JField;
import com.yevhenii.nospock.translator.ExPool;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate.ArgumentsAssignment;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate.Times;
import com.yevhenii.nospock.translator.spock.JForeignStatement;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate.ArgumentMatcher;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate.ArgumentMatcher.Rule;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MockTranslator {

  private static final Logger LOG = Logger.getLogger(MockTranslator.class.getName());

  private final MockTemplate mockTemplate;
  private final ExPool exPool;
  private final RuntimeLookup runtimeLookup;
  private final Translator<ClosureExpression, JLambdaExpression> closureTranslator;

  public MockTranslator(
    MockTemplate mockTemplate,
    ExPool exPool,
    RuntimeLookup runtimeLookup,
    Translator<ClosureExpression, JLambdaExpression> closureTranslator
  ) {
    this.mockTemplate = Objects.requireNonNull(mockTemplate);
    this.exPool = Objects.requireNonNull(exPool);
    this.runtimeLookup = Objects.requireNonNull(runtimeLookup);
    this.closureTranslator = Objects.requireNonNull(closureTranslator);
  }

  public TranslatedMockInitialization translateInitialization(DetectedMockInitialization initialization, TContext context) {
    final var result = mockTemplate.initialization(
      initialization.mockType(),
      new JFieldAccessExpression(
        new JVariableExpression(initialization.className()),
        new JConstantExpression("class")
      ),
      initialization.answer() == null ? null : closureTranslator.translate(initialization.answer(), context)
    );
    return new TranslatedMockInitialization(
      initialization.mockType(),
      initialization.className(),
      result
    );
  }

  public JForeignStatement translateInteraction(DetectedMockInteraction interaction, TContext context) {
    switch (interaction.operation()) {
      case MOCK_RETURN_VALUE:
        return translateMockReturnValue(interaction, context);
      case VERIFY_METHOD_CALLED:
        return translateVerifyMethodCalled(interaction, context);
      default:
        throw new TranslationException("Not supported operation on mock interaction " + interaction.operation());
    }
  }

  /**
   * Overrides initialization of mock if necessary.
   * For example, in case mock definition is split from the initialization itself, like:
   *
   * <pre>{@code
   *
   * def mock;
   *
   * setup() {
   *   mock = Mock(X.class);
   * }
   * }</pre>
   * <p>
   * Will resolve the field type to be X. And vise-versa, if definition contains
   * type while initialization does not, like:
   *
   * <pre>{@code
   *
   * X mock;
   *
   * setup() {
   *   mock = Mock();
   * }
   * }</pre>
   * <p>
   * The {@code X} type will be used within the initialization.
   */
  public JBinaryExpression adjustTypes(JBinaryExpression binary, TContext context) {
    if (!"=".equals(binary.operation())
        || !(binary.right() instanceof TranslatedMockInitialization)
        || !(binary.left() instanceof JVariableExpression)) {
      return binary;
    }

    final var left = (JVariableExpression) binary.left();
    final var right = (TranslatedMockInitialization) binary.right();
    final var field = context.field(left.name(), context.path().containingClass());
    if (field != null) {
      // Dynamic field type and known type from mock initialization def x = Mockito.mock(X.class)
      if (field.type().isJavaLangObject() && !"java.lang.Object".equals(right.className())) {
        field.type(new JType(right.className()));

        // If field type is know, but mock initialization doesn't resolve it X x = Mockito.mock(java.lang.Object.class)
      } else if (!field.type().isJavaLangObject() && "java.lang.Object".equals(right.className())) {
        return new JBinaryExpression(
          left,
          translateInitialization(
            new DetectedMockInitialization(right.mockType(), field.type().fqn().asString()),
            context
          ),
          binary.operation()
        );
      }
    }

    return binary;
  }

  /**
   * Overrides initialization of mock if necessary. For example converts the declaration
   * {@code X mock = Mock()} to {@code X x mock = Mock(X.class)}.
   */
  public JDeclarationExpression adjustTypes(JDeclarationExpression declaration, TContext context) {
    if (!(declaration.right() instanceof TranslatedMockInitialization) || !(declaration.left() instanceof JVariableExpression)) {
      return declaration;
    }

    final var left = (JVariableExpression) declaration.left();
    final var right = ((TranslatedMockInitialization) declaration.right());
    if (left.isDeclaration() && !left.isThis() && !left.type().isJavaLangObject() && "java.lang.Object".equals(right.className())) {
      final TranslatedMockInitialization translatedInitialization = translateInitialization(
        new DetectedMockInitialization(right.mockType(), left.type().name()),
        context
      );
      TransformationsQueue.instance().enqueueNewImports(translatedInitialization.imports());
      return new JDeclarationExpression(left, translatedInitialization);
    }

    return declaration;
  }

  /**
   * Overrides initialization of field if necessary. Ensures that for both fields in the example below
   * <pre>{@code
   *
   * def mock = Mock(X.class);
   * X mock = Mock();
   * }</pre>
   * <p>
   * types can be resolved to be X.
   */
  public void adjustTypes(JField field, TContext context) {
    if (!(field.initExpression() instanceof TranslatedMockInitialization)) {
      return;
    }

    final var initialization = (TranslatedMockInitialization) field.initExpression();
    // Trying to figure out the type in case of missing types.
    // Dynamic field type and known type from mock initialization def x = Mockito.mock(X.class)
    if (field.type().isJavaLangObject() && !"java.lang.Object".equals(initialization.className())) {
      field.type(new JType(initialization.className()));

      // If field type is know, but mock initialization doesn't resolve it X x = Mockito.mock(java.lang.Object.class)
    } else if (!field.type().isJavaLangObject() && "java.lang.Object".equals(initialization.className())) {
      field.initExpression(
        translateInitialization(
          new DetectedMockInitialization(
            initialization.mockType(),
            field.type().fqn().last().asString()
          ),
          context
        )
      );
      TransformationsQueue.instance().enqueueNewImports(initialization.imports());
    }
  }

  private JForeignStatement translateMockReturnValue(DetectedMockInteraction interaction, TContext context) {
    if (interaction.left() instanceof PropertyExpression) {
      LOG.fine("Property -> method call. Otherwise return value mocking wouldn't be possible. Property " + interaction.right());
      interaction = new DetectedMockInteraction(
        interaction.operation(),
        propertyReadToMethodCall((PropertyExpression) interaction.left(), context),
        interaction.right(),
        interaction.verificationClosure()
      );
    }
    if (!(interaction.left() instanceof MethodCallExpression)) {
      throw new TranslationException("For mocking interaction the left expression excepted to be a method call " + interaction.left());
    }
    final var leftAsMCall = (MethodCallExpression) interaction.left();
    if (!(leftAsMCall.getArguments() instanceof ArgumentListExpression)) {
      throw new TranslationException("For mocking interaction method call must have arguments as ArgumentListExpression " + leftAsMCall);
    }
    final var mCallArgs = (ArgumentListExpression) leftAsMCall.getArguments();

    // mock.call >> { args -> ... }
    if (interaction.right() instanceof ClosureExpression) {
      return translateClosureReturnValue(
        (ClosureExpression) interaction.right(),
        leftAsMCall,
        context
      );
    }

    JExpression thenReturn = exPool.translate(interaction.right(), context);
    if (thenReturn instanceof ConstructorCallInitializedWithSetters) {
      final var extractor = new ConstructorToVariableExtractor((ConstructorCallInitializedWithSetters) thenReturn);
      thenReturn = extractor.extractBeforeCurrentStatement(context);
    }
    return mockTemplate.mockReturnValue(
      new JVariableExpression(leftAsMCall.getObjectExpression().getText()),
      leftAsMCall.getMethodAsString(),
      argsAsMatchers(leftAsMCall, mCallArgs, context),
      thenReturn
    );
  }

  /**
   * Closure translation is something that can be improved to be much smarter.
   * For example, rather than having 'doAnswer' that embeds assertions what could be used
   * is set of argument captors. However, in order to generate proper captors the body
   * must contain no logic that does something else but assertions against captured values,
   * in this regard translating closure return value to 'doAnswer' is a solution that
   * does not require analysis of the closure statements.
   */
  private JForeignStatement translateClosureReturnValue(ClosureExpression cExp, MethodCallExpression mCall, TContext context) {
    // doAnswer(...).when(mock).call(...)
    final var lambda = (JLambdaExpression) exPool.translate(cExp, context);
    final var mCallArgs = ((ArgumentListExpression) mCall.getArguments());

    // mock.call(*_) >> { ... }
    if (lambda.parameters().isEmpty()) {
      return mockTemplate.doAnswer(
        new ArgumentsAssignment(JVariableExpression.var(JType.object(), "it")),
        lambda.block(),
        new JVariableExpression(mCall.getObjectExpression().getText()),
        mCall.getMethodAsString(),
        argsAsMatchers(mCall, mCallArgs, context)
      );
    }

    // mock.call(*_) >> { args -> ... }
    if (lambda.parameters().size() == 1 && lambda.parameters().get(0).type().isJavaLangObject()) {
      return mockTemplate.doAnswer(
        new ArgumentsAssignment(JVariableExpression.var(JType.object(), lambda.parameters().get(0).name())),
        lambda.block(),
        new JVariableExpression(mCall.getObjectExpression().getText()),
        mCall.getMethodAsString(),
        argsAsMatchers(mCall, mCallArgs, context)
      );
    }

    // mock.call(*_) >> { String arg1, int arg2 -> ... }
    // mock.call(*_) >> { MyType arg1 -> ... }
    return mockTemplate.doAnswer(
      new ArgumentsAssignment(
        lambda.parameters()
          .stream()
          .map(p -> new JVariableExpression(p.type(), p.name(), true))
          .collect(Collectors.toList())
      ),
      lambda.block(),
      new JVariableExpression(mCall.getObjectExpression().getText()),
      mCall.getMethodAsString(),
      argsAsMatchers(mCall, mCallArgs, context)
    );
  }

  private JForeignStatement translateVerifyMethodCalled(DetectedMockInteraction interaction, TContext context) {
    // could be that method call is used without parenthesis ...
    if (interaction.right() instanceof PropertyExpression) {
      LOG.fine("Property -> method call. Otherwise verification wouldn't be possible. Property " + interaction.right());
      interaction = new DetectedMockInteraction(
        interaction.operation(),
        interaction.left(),
        propertyReadToMethodCall((PropertyExpression) interaction.right(), context),
        interaction.verificationClosure()
      );
    }
    if (!(interaction.right() instanceof MethodCallExpression)) {
      throw new TranslationException(
        "For verification interaction the right expression excepted to be a method call " + interaction.right()
      );
    }
    final var rightAsMCall = ((MethodCallExpression) interaction.right());
    if (!(rightAsMCall.getArguments() instanceof ArgumentListExpression)) {
      throw new TranslationException(
        "For verification interaction method call must have arguments as ArgumentListExpression " + rightAsMCall
      );
    }
    final var mCallArgs = ((ArgumentListExpression) rightAsMCall.getArguments());

    if (interaction.verificationClosure() == null) {
      return mockTemplate.verifyTimesCalled(
        new JVariableExpression(rightAsMCall.getObjectExpression().getText()),
        rightAsMCall.getMethodAsString(),
        argsAsMatchers(rightAsMCall, mCallArgs, context),
        asTimes(exPool.translate(interaction.left(), context))
      );
    }

    // very limited support for argThat atm

    if (mCallArgs.getExpressions().size() != 1) {
      throw new TranslationException(
        String.format(
          "For a complex verification having verification closure the expectation is that " +
          "method has single argument that matches any value (e.g. _ or *_), while method " +
          "'%s' has '%d' arguments",
          rightAsMCall.getMethodAsString(),
          mCallArgs.getExpressions().size()
        )
      );
    }

    final JExpression translatedClosure = exPool.translate(interaction.verificationClosure(), context);
    if (!(translatedClosure instanceof JLambdaExpression)) {
      throw new TranslationException(
        "Expected to have closure translated to lambda, while having " + translatedClosure
      );
    }

    final JLambdaExpression lambda = ((JLambdaExpression) translatedClosure);
    final List<JReturnStatement> returnStatements = TranslateHelper.findNestedStatementsOfType(lambda.block(), JReturnStatement.class);
    if (returnStatements.isEmpty()) {
      lambda.block().statements().add(new JReturnStatement(new JConstantExpression(true)));
    }

    return mockTemplate.verifyTimesCalled(
      new JVariableExpression(rightAsMCall.getObjectExpression().getText()),
      rightAsMCall.getMethodAsString(),
      List.of(new ArgumentMatcher(Rule.ARG_THAT, translatedClosure)),
      asTimes(exPool.translate(interaction.left(), context))
    );
  }

  private List<ArgumentMatcher> argsAsMatchers(MethodCallExpression mCall, ArgumentListExpression mCallArgs, TContext context) {
    final List<Method> methods = runtimeLookup.methods.homonyms(mCall, context);
    final List<ArgumentMatcher> matchers = new ArrayList<>();
    if (mCallArgs.getExpressions() == null || mCallArgs.getExpressions().isEmpty()) {
      return matchers;
    }

    // mock.call(*_)
    if (mCallArgs.getExpression(0) instanceof SpreadExpression) {
      // real method has to be mocked in such case, we don't care which one
      if (methods.isEmpty()) {
        LOG.warning(
          String.format(
            "Cannot properly translate spread expression, methods not loaded for method call '%s'",
            mCall.getMethodAsString()
          )
        );
        matchers.add(new ArgumentMatcher(Rule.ANY, null));
      } else {
        final var method = methods.get(0);
        for (int i = 0; i < method.getParameterTypes().length; i++) {
          matchers.add(new ArgumentMatcher(anyMatcherRule(method.getParameterTypes()[i]), null));
        }
      }
      return matchers;
    }

    // among methods that we have, find the one that fits the context best
    // for now it's fine if number of arguments == number of parameters defined
    // can also check types if that becomes annoying 
    final Method method = methods
      .stream()
      .filter(m -> m.getParameterCount() == mCallArgs.getExpressions().size())
      .findAny()
      .orElse(null);

    if (method == null) {
      LOG.warning(
        String.format(
          "Among methods %d, %s didn't find the one that matches number of arguments to be %d, " +
          "cannot guarantee correct translation of argument matchers",
          methods.size(),
          mCall.getMethodAsString(),
          mCallArgs.getExpressions().size()
        )
      );
      for (Expression arg : mCallArgs) {
        if (isUnderscore(arg)) {
          matchers.add(new ArgumentMatcher(Rule.ANY, null));
        } else if (arg instanceof ClosureExpression) {
          matchers.add(new ArgumentMatcher(Rule.ARG_THAT, asArgThatMatcherLambda((ClosureExpression) arg, context, null)));
        } else if (arg instanceof CastExpression && isUnderscore(((CastExpression) arg).getExpression())) {
          matchers.add(new ArgumentMatcher(Rule.ANY_OF_TYPE, new JClassLiteralExpression(new JType(arg.getType().getName()))));
        } else if (arg instanceof CastExpression && ((CastExpression) arg).getExpression() instanceof ClosureExpression) {
          matchers.add(new ArgumentMatcher(Rule.ARG_THAT, asArgThatMatcherLambda((ClosureExpression) ((CastExpression) arg).getExpression(), context, null)));
        } else {
          matchers.add(new ArgumentMatcher(Rule.EQ, exPool.translate(arg, context)));
        }
      }
    } else {
      for (int i = 0; i < mCallArgs.getExpressions().size(); i++) {
        final Expression arg = mCallArgs.getExpression(i);
        final Class<?> pType = method.getParameterTypes()[i];
        if (isUnderscore(arg)) {
          matchers.add(new ArgumentMatcher(anyMatcherRule(method.getParameterTypes()[i]), null));
        } else if (arg instanceof ClosureExpression) {
          matchers.add(new ArgumentMatcher(Rule.ARG_THAT, asArgThatMatcherLambda((ClosureExpression) arg, context, pType)));
        } else if (arg instanceof CastExpression && isUnderscore(((CastExpression) arg).getExpression())) {
          matchers.add(new ArgumentMatcher(Rule.ANY_OF_TYPE, new JClassLiteralExpression(new JType(arg.getType().getName()))));
        } else if (arg instanceof CastExpression && ((CastExpression) arg).getExpression() instanceof ClosureExpression) {
          matchers.add(new ArgumentMatcher(Rule.ARG_THAT, asArgThatMatcherLambda((ClosureExpression) ((CastExpression) arg).getExpression(), context, pType)));
        } else {
          matchers.add(eqMatcher(pType, exPool.translate(arg, context)));
        }
      }
    }

    return matchers;
  }

  private MethodCallExpression propertyReadToMethodCall(PropertyExpression property, TContext context) {
    final Method getter = runtimeLookup.methods.getterUnchecked(property, context);
    if (getter == null) {
      return new MethodCallExpression(
        property.getObjectExpression(),
        property.getProperty().getText(),
        new ArgumentListExpression(List.of())
      );
    } else {
      return new MethodCallExpression(
        property.getObjectExpression(),
        getter.getName(),
        new ArgumentListExpression(List.of())
      );
    }
  }

  private static ArgumentMatcher eqMatcher(Class<?> c, JExpression translated) {
    // x(Long): instead of x(eq(123)) do x(eq(123L))
    if (c == Long.class && translated instanceof JConstantExpression) {
      final var translatedConstant = ((JConstantExpression) translated);
      if (translatedConstant.value() instanceof Integer) {
        return new ArgumentMatcher(Rule.EQ, new JConstantExpression(((Integer) translatedConstant.value()).longValue()));
      }
    }
    return new ArgumentMatcher(Rule.EQ, translated);
  }

  private static Rule anyMatcherRule(Class<?> c) {
    if (c == byte.class) {
      return Rule.ANY_BYTE;
    } else if (c == short.class) {
      return Rule.ANY_SHORT;
    } else if (c == int.class) {
      return Rule.ANY_INT;
    } else if (c == long.class) {
      return Rule.ANY_LONG;
    } else if (c == float.class) {
      return Rule.ANY_FLOAT;
    } else if (c == double.class) {
      return Rule.ANY_DOUBLE;
    } else if (c == char.class) {
      return Rule.ANY_CHAR;
    } else if (c == boolean.class) {
      return Rule.ANY_BOOLEAN;
    } else {
      return Rule.ANY;
    }
  }

  private Times asTimes(JExpression expression) {
    if (isUnderscore(expression)) {
      return new Times(Times.Type.AT_LEAST, new JConstantExpression(0));
    }
    final Integer times = asInteger(expression);
    if (times == null || times > 1) {
      return new Times(Times.Type.EXACTLY, expression);
    } else if (times == 0) {
      return new Times(Times.Type.NEVER, expression);
    } else {
      return new Times(Times.Type.ONCE, expression);
    }
  }

  private static Integer asInteger(JExpression expression) {
    if (expression instanceof JConstantExpression) {
      final JConstantExpression cExp = (JConstantExpression) expression;
      if (cExp.value() instanceof Integer) {
        return ((Integer) cExp.value());
      } else {
        try {
          return Integer.parseInt(cExp.value().toString());
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return null;
  }

  private static boolean isUnderscore(JExpression expression) {
    return expression instanceof JVariableExpression && ((JVariableExpression) expression).name().equals("_");
  }

  private static boolean isUnderscore(Expression expression) {
    return expression instanceof VariableExpression && ((VariableExpression)expression).getName().equals("_");
  }

  private JLambdaExpression asArgThatMatcherLambda(ClosureExpression closure, TContext context, Class<?> argType) {
    // the parameter is defined in closure to make sure that it gets propagated to the translation context of the lambda block
    final Parameter[] params = closure.getParameters(); 
    if (params != null) {
      if (params.length == 0) {
        closure = new ClosureExpression(
          new Parameter[] {new Parameter(new ClassNode(argType == null ? Object.class : argType), "it")},
          closure.getCode()
        );
      } else if (params.length == 1 && argType != null && params[0].getType().getName().equals("java.lang.Object")) {
        closure = new ClosureExpression(
          new Parameter[] {new Parameter(new ClassNode(argType), params[0].getName())},
          closure.getCode()
        );
      }
    }
    return ((JLambdaExpression) exPool.translate(closure, context));
  }
}
