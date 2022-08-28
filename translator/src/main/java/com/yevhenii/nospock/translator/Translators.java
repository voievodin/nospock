package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.translator.exp.ArrayExpressionTranslator;
import com.yevhenii.nospock.translator.exp.BinaryExpressionTranslator;
import com.yevhenii.nospock.translator.exp.BooleanExpressionTranslator;
import com.yevhenii.nospock.translator.exp.CastExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ClassExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ClosureExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ConstantExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ConstructorCallExpressionTranslator;
import com.yevhenii.nospock.translator.exp.DeclarationExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ElvisOperatorExpressionTranslator;
import com.yevhenii.nospock.translator.exp.FieldExpressionTranslator;
import com.yevhenii.nospock.translator.exp.GStringExpressionTranslator;
import com.yevhenii.nospock.translator.exp.ListExpressionTranslator;
import com.yevhenii.nospock.translator.exp.MapExpressionTranslator;
import com.yevhenii.nospock.translator.exp.MethodCallExpressionTranslator;
import com.yevhenii.nospock.translator.exp.MethodPointerExpressionTranslator;
import com.yevhenii.nospock.translator.exp.NotExpressionTranslator;
import com.yevhenii.nospock.translator.exp.PostfixExpressionTranslator;
import com.yevhenii.nospock.translator.exp.PrefixExpressionTranslator;
import com.yevhenii.nospock.translator.exp.PropertyExpressionTranslator;
import com.yevhenii.nospock.translator.exp.SpreadExpressionTranslator;
import com.yevhenii.nospock.translator.exp.TernaryExpressionTranslator;
import com.yevhenii.nospock.translator.exp.VariableExpressionTranslator;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeForConstantExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeForConstructorCallExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeForMethodCallExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeForPropertyExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeForVariableExpression;
import com.yevhenii.nospock.translator.resolver.ResolvesTypeToNull;
import com.yevhenii.nospock.translator.resolver.TypeResolver;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.translator.spi.assertion.AssertionsTemplate;
import com.yevhenii.nospock.translator.spi.engine.TestEngineTemplate;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate;
import com.yevhenii.nospock.translator.spock.LifecycleTestMethodTranslator;
import com.yevhenii.nospock.translator.spock.TestMethodTranslator;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsDetector;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsTranslator;
import com.yevhenii.nospock.translator.spock.mock.MockDetector;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;
import com.yevhenii.nospock.translator.stmt.AssertStatementTranslator;
import com.yevhenii.nospock.translator.stmt.BlockStatementTranslator;
import com.yevhenii.nospock.translator.stmt.BreakStatementTranslator;
import com.yevhenii.nospock.translator.stmt.ContinueStatementTranslator;
import com.yevhenii.nospock.translator.stmt.EmptyStatementTranslator;
import com.yevhenii.nospock.translator.stmt.ExpressionStatementTranslator;
import com.yevhenii.nospock.translator.stmt.ForStatementTranslator;
import com.yevhenii.nospock.translator.stmt.IfStatementTranslator;
import com.yevhenii.nospock.translator.stmt.ReturnStatementTranslator;
import com.yevhenii.nospock.translator.stmt.SwitchStatementTranslator;
import com.yevhenii.nospock.translator.stmt.ThrowStatementTranslator;
import com.yevhenii.nospock.translator.stmt.TryCatchStatementTranslator;
import com.yevhenii.nospock.translator.stmt.WhileStatementTranslator;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;

import java.util.HashMap;
import java.util.Map;

public final class Translators {

  private final Map<TKey<?, ?>, Translation<?, ?>> registry = new HashMap<>();
  private final ExPool exPool = new ExPool();
  private final StPool stPool = new StPool();
  private final TypeResolver typeResolver = new TypeResolver();

  private final FileTranslator fileTranslator;

  public Translators(
    TranslationConfig translationConfig,
    ClassLoader classLoader,
    TestEngineTemplate engineTemplate,
    AssertionsTemplate assertionsTemplate,
    MockTemplate mockTemplate
  ) {
    final var typeLoader = new TypeLoader(classLoader);
    final var valueAdjuster = new ValueAdjuster();
    final var runtimeLookup = new RuntimeLookup(typeLoader, typeResolver);
    final var mockDetector = new MockDetector();
    final var mockTranslator = new MockTranslator(mockTemplate, exPool, runtimeLookup, translator(TKey.EX_CLOSURE));
    final var assertionsTranslator = new AssertionsTranslator(assertionsTemplate, exPool, stPool, valueAdjuster, runtimeLookup);
    final var assertionsDetector = new AssertionsDetector(typeLoader, runtimeLookup);

    // all translators
    install(
      new NodeTranslation<>(
        TKey.TYPE,
        new TypeTranslator()
      )
    );
    install(
      new NodeTranslation<>(
        TKey.PARAMETER,
        new ParameterTranslator(translator(TKey.TYPE))
      )
    );
    install(
      new NodeTranslation<>(
        TKey.ANNOTATION_USAGE,
        new AnnotationUsageTranslator(exPool)
      )
    );
    install(
      new NodeTranslation<>(
        TKey.FIELD,
        new FieldTranslator(
          translator(TKey.TYPE),
          translator(TKey.ANNOTATION_USAGE),
          exPool,
          mockDetector,
          mockTranslator,
          runtimeLookup
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.CONSTRUCTOR,
        new ConstructorTranslator(
          translator(TKey.TYPE),
          translator(TKey.ANNOTATION_USAGE),
          stPool
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.METHOD,
        new MethodTranslator(
          translator(TKey.TYPE),
          translator(TKey.ST_BLOCK),
          translator(TKey.ANNOTATION_USAGE),
          translator(TKey.PARAMETER),
          exPool,
          typeLoader
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.TEST_METHOD,
        new TestMethodTranslator(
          translationConfig,
          assertionsTranslator,
          assertionsDetector,
          mockDetector,
          mockTranslator,
          stPool,
          engineTemplate,
          exPool,
          typeLoader,
          runtimeLookup
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.LIFECYCLE_TEST_METHOD,
        new LifecycleTestMethodTranslator(
          stPool,
          engineTemplate,
          mockDetector,
          mockTranslator
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.CLASS,
        new ClassTranslator(
          translator(TKey.ANNOTATION_USAGE),
          translator(TKey.TYPE),
          translator(TKey.FIELD),
          translator(TKey.CONSTRUCTOR),
          translator(TKey.METHOD),
          (TestMethodTranslator) translator(TKey.TEST_METHOD),
          (LifecycleTestMethodTranslator) translator(TKey.LIFECYCLE_TEST_METHOD)
        )
      )
    );
    install(
      new NodeTranslation<>(
        TKey.IMPORT,
        new ImportTranslator()
      )
    );
    install(
      new NodeTranslation<>(
        TKey.PACKAGE,
        new PackageTranslator()
      )
    );

    // expressions
    install(
      new ExpressionTranslation<>(
        TKey.EX_CLOSURE,
        new ClosureExpressionTranslator(translator(TKey.ST_BLOCK), translator(TKey.PARAMETER), stPool),
        new ResolvesTypeToNull<>(ClosureExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_BINARY,
        new BinaryExpressionTranslator(exPool, mockTranslator, runtimeLookup),
        new ResolvesTypeToNull<>(BinaryExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_CONSTANT,
        new ConstantExpressionTranslator(translationConfig),
        new ResolvesTypeForConstantExpression(typeLoader)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_CONSTRUCTOR_CALL,
        new ConstructorCallExpressionTranslator(translator(TKey.TYPE), translator(TKey.CLASS), exPool),
        new ResolvesTypeForConstructorCallExpression(typeLoader)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_DECLARATION,
        new DeclarationExpressionTranslator(exPool, mockTranslator, runtimeLookup, valueAdjuster),
        new ResolvesTypeToNull<>(DeclarationExpression.class)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_FIELD_ACCESS,
        new FieldExpressionTranslator(),
        new ResolvesTypeToNull<>(FieldExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_METHOD_CALL,
        new MethodCallExpressionTranslator(exPool, mockDetector, mockTranslator, runtimeLookup, valueAdjuster),
        new ResolvesTypeForMethodCallExpression(typeLoader)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_POSTFIX,
        new PostfixExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(PostfixExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_PROPERTY,
        new PropertyExpressionTranslator(exPool, runtimeLookup),
        new ResolvesTypeForPropertyExpression(typeLoader, runtimeLookup)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_VARIABLE,
        new VariableExpressionTranslator(),
        new ResolvesTypeForVariableExpression(typeLoader)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_BOOLEAN,
        new BooleanExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(BooleanExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_CAST,
        new CastExpressionTranslator(translator(TKey.TYPE), exPool),
        new ResolvesTypeToNull<>(CastExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_LIST,
        new ListExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(ListExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_NOT,
        new NotExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(NotExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_CLASS_LITERAL,
        new ClassExpressionTranslator(translator(TKey.TYPE)),
        new ResolvesTypeToNull<>(ClassExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_PREFIX,
        new PrefixExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(PrefixExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_ARRAY,
        new ArrayExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(ArrayExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_MAP,
        new MapExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(MapExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_SPREAD,
        new SpreadExpressionTranslator(),
        new ResolvesTypeToNull<>(SpreadExpression.class)
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_TERNARY,
        new TernaryExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(TernaryExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_METHOD_POINTER,
        new MethodPointerExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(MethodPointerExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_GSTRING,
        new GStringExpressionTranslator(exPool),
        new ResolvesTypeToNull<>(GStringExpression.class) // todo
      )
    );
    install(
      new ExpressionTranslation<>(
        TKey.EX_ELVIS,
        new ElvisOperatorExpressionTranslator(translator(TKey.EX_TERNARY)),
        new ResolvesTypeToNull<>(ElvisOperatorExpression.class) // todo
      )
    );

    // statements
    install(
      new StatementTranslation<>(
        TKey.ST_BLOCK,
        new BlockStatementTranslator(stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_BREAK,
        new BreakStatementTranslator()
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_CONTINUE,
        new ContinueStatementTranslator()
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_EMPTY,
        new EmptyStatementTranslator()
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_EXPRESSION,
        new ExpressionStatementTranslator(exPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_FOR,
        new ForStatementTranslator(translator(TKey.TYPE), exPool, stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_IF,
        new IfStatementTranslator(exPool, stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_RETURN,
        new ReturnStatementTranslator(exPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_SWITCH,
        new SwitchStatementTranslator(exPool, stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_THROW,
        new ThrowStatementTranslator(exPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_TRY_CATCH,
        new TryCatchStatementTranslator(translator(TKey.TYPE), stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_WHILE,
        new WhileStatementTranslator(exPool, stPool)
      )
    );
    install(
      new StatementTranslation<>(
        TKey.ST_ASSERT,
        new AssertStatementTranslator(assertionsDetector, assertionsTranslator, exPool)
      )
    );

    checkNoPendingInstallationsLeft();

    fileTranslator = new FileTranslator(translator(TKey.PACKAGE), translator(TKey.IMPORT), translator(TKey.CLASS));
  }

  public FileTranslator fileTranslator() {
    return fileTranslator;
  }

  @SuppressWarnings("unchecked")
  public <T extends ASTNode, F extends JAstNode> Translator<T, F> translator(TKey<T, F> key) {
    Translation<?, ?> translation = registry.get(key);
    if (translation == null) {
      translation = new PendingInstallationTranslation<>(key);
      registry.put(key, translation);
    }
    return (Translator<T, F>) translation.translator();
  }

  @SuppressWarnings("all")
  private void install(Translation<? extends ASTNode, ? extends JAstNode> translation) {
    final var pv = registry.put(translation.key(), translation);
    if (pv != null) {
      if (pv instanceof PendingInstallationTranslation) {
        final var pendingInstallation = (PendingInstallationTranslation) pv;
        pendingInstallation.lazyTranslator.delegate = translation.translator();
      } else {
        throw new TranslationException("Translation for key " + translation.key() + " already registered: " + pv);
      }
    }
    if (translation instanceof ExpressionTranslation) {
      exPool.register((ExpressionTranslator<?, ?>) translation.translator());
      typeResolver.register(((ExpressionTranslation<?, ?>) translation).typeResolver());
    } else if (translation instanceof StatementTranslation) {
      stPool.register((StatementTranslator<?, ?>) translation.translator());
    }
  }

  private void checkNoPendingInstallationsLeft() {
    for (Translation<?, ?> value : registry.values()) {
      if (value instanceof PendingInstallationTranslation) {
        throw new TranslationException(
          "Missing installation for key " + ((PendingInstallationTranslation<?, ?>) value).key
        );
      }
    }
  }

  private static class PendingInstallationTranslation<G extends ASTNode, J extends JAstNode> implements Translation<G, J> {
    final TKey<G, J> key;
    final LazyTranslator<G, J> lazyTranslator;

    PendingInstallationTranslation(TKey<G, J> key) {
      this.key = key;
      this.lazyTranslator = new LazyTranslator<>();
    }

    @Override
    public TKey<G, J> key() {
      return key;
    }

    @Override
    public Translator<G, J> translator() {
      return lazyTranslator;
    }
  }

  private static class LazyTranslator<G extends ASTNode, J extends JAstNode> implements Translator<G, J> {
    Translator<G, J> delegate;

    @Override
    public J translate(G node, TContext context) {
      return delegate.translate(node, context);
    }
  }
}
