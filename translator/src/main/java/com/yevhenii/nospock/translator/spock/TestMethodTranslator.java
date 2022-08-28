package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.jast.*;
import com.yevhenii.nospock.jast.exp.JCommentExpression;
import com.yevhenii.nospock.jast.stmt.JEmptyStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.translator.*;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.translator.spi.engine.ArgumentValues;
import com.yevhenii.nospock.translator.spock.assertion.Assertion;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsDetector;
import com.yevhenii.nospock.translator.spock.assertion.AssertionsTranslator;
import com.yevhenii.nospock.translator.spock.assertion.ThrowsAssertion;
import com.yevhenii.nospock.translator.spi.engine.TestEngineTemplate;
import com.yevhenii.nospock.translator.spock.mock.MockDetector;
import com.yevhenii.nospock.translator.spock.mock.DetectedMockInteraction;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.*;
import java.util.stream.Collectors;

import static com.yevhenii.nospock.translator.TranslationConfig.SpockLabelPresenceMode.PRESENT;
import static com.yevhenii.nospock.translator.TranslationConfig.SpockLabelPresenceMode.PRESENT_ONLY_WHEN_HAVE_COMMENTS;

public class TestMethodTranslator implements Translator<MethodNode, JMethod> {

  private final AssertionsTranslator assertionsTranslator;
  private final AssertionsDetector assertionsDetector;
  private final MockDetector mockDetector;
  private final MockTranslator mockTranslator;
  private final StPool stPool;
  private final TestEngineTemplate engineTemplate;
  private final ExPool exPool;
  private final TypeLoader typeLoader;
  private final TranslationConfig translationConfig;
  private final RuntimeLookup runtimeLookup;

  public TestMethodTranslator(
    TranslationConfig translationConfig,
    AssertionsTranslator assertionsTranslator,
    AssertionsDetector assertionsDetector,
    MockDetector mockDetector,
    MockTranslator mockTranslator,
    StPool stPool,
    TestEngineTemplate testEngineTemplate,
    ExPool exPool,
    TypeLoader typeLoader,
    RuntimeLookup runtimeLookup
  ) {
    this.translationConfig = translationConfig;
    this.assertionsTranslator = assertionsTranslator;
    this.assertionsDetector = assertionsDetector;
    this.mockDetector = mockDetector;
    this.mockTranslator = mockTranslator;
    this.stPool = stPool;
    this.engineTemplate = testEngineTemplate;
    this.exPool = exPool;
    this.typeLoader = typeLoader;
    this.runtimeLookup = runtimeLookup;
  }

  public boolean canTranslate(MethodNode method) {
    if (method.getCode() != null) {
      for (Statement statement : ((BlockStatement) method.getCode()).getStatements()) {
        if (statement.getStatementLabels() != null && !statement.getStatementLabels().isEmpty()) {
          for (String statementLabel : statement.getStatementLabels()) {
            if (SpockLabel.isKnown(statementLabel)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public JMethod translate(MethodNode node, TContext context) {
    context = context.deepen(CPath.Seg.forMethod(node.getName()));
    final var blockTranslation = new BlockTranslation();

    // so now we have 1 section per 1 spock label, except and labels
    // 'and' statements added to the previous section
    final var statements = ((BlockStatement) node.getCode()).getStatements();

    final Sections sections = Sections.group(statements);

    // Spock has some complex structures, like 1 * mock.call(*_) >> 123
    // we want to be able to represent that using tools like mockito.
    // Mockito though splits mocking and verification, these
    // two things will be represented as separate MockInteraction instances.
    // Typically, this complex structure can be used in different sections, 
    // while we want to have mocking before 'when' is executed, so we 
    // first collect all the interactions and only then structure the method body.
    final Map<Section, List<DetectedMockInteraction>> section2interactions = new HashMap<>();
    for (Section section : sections) {
      final List<DetectedMockInteraction> sectionInteractions = new ArrayList<>();
      section2interactions.put(section, sectionInteractions);
      final Iterator<Statement> iterator = section.statements.iterator();
      while (iterator.hasNext()) {
        final List<DetectedMockInteraction> statementInteractions = mockDetector.detectInteractions(iterator.next());
        if (!statementInteractions.isEmpty()) {
          iterator.remove();
          sectionInteractions.addAll(statementInteractions);
        }
      }
    }

    // thrown(Exception) has is a complex assertion.
    // Every block that has thrown statement, should wrap the previous block
    // in a special type of assert or try catch with failure, in this regard
    // we need to have a clear understanding of sections where thrown statement
    // is placed to be able to skip generation of 'when' sections for such cases
    // and move that logic to assertion statement. Thrown structure is supposed to
    // serve exactly this purpose.
    final Thrown thrown = Thrown.build(sections);

    final SectionHandlers handlers = new SectionHandlers(context, sections, section2interactions, thrown, blockTranslation);
    for (Section section : sections) {
      int blockIndex = blockTranslation.statementsSize();
      handlers.forLabel(section.label).handle(section);
      if (blockIndex != blockTranslation.statementsSize()) {
        if (blockIndex != 0) {
          blockTranslation.add(blockIndex++, new JEmptyStatement(), context);
        }
        if (isConfiguredToAddLabelAsComment(sections)) {
          blockTranslation.add(
            blockIndex,
            new JExpressionStatement(new JCommentExpression(section.getLabelWithComment())),
            context
          );
        }
      }
    }

    final var method = new JMethod(
      TranslateHelper.asCamelCaseName(node.getName()),
      new JType("void"),
      0,
      blockTranslation.end(context),
      List.of(),
      List.of()
    );

    final var whereSection = sections.byLabel(SpockLabel.WHERE);
    if (whereSection == null) {
      engineTemplate.customizeTestMethod(method);
    } else {
      customiseAsParameterizedTest(method, whereSection, statements, context);
    }

    for (List<DetectedMockInteraction> interactions : section2interactions.values()) {
      if (!interactions.isEmpty()) {
        throw new TranslationException("Interactions remaining after translation " + interactions);
      }
    }

    return method;
  }

  interface SectionHandler {
    void handle(Section section);
  }

  private class SectionHandlers {

    final TContext context;
    final Sections sections;
    final Map<Section, List<DetectedMockInteraction>> section2interactions;
    final Thrown thrown;
    final BlockTranslation blockTranslation;
    final Map<SpockLabel, SectionHandler> handlers;
    final GivenSectionHandler givenSectionHandler;

    SectionHandlers(
      TContext context,
      Sections sections,
      Map<Section, List<DetectedMockInteraction>> section2interactions,
      Thrown thrown,
      BlockTranslation blockTranslation
    ) {
      this.context = context;
      this.sections = sections;
      this.section2interactions = section2interactions;
      this.thrown = thrown;
      this.blockTranslation = blockTranslation;
      this.givenSectionHandler = new GivenSectionHandler();
      this.handlers = Map.of(
        SpockLabel.SETUP, givenSectionHandler,
        SpockLabel.GIVEN, givenSectionHandler,
        SpockLabel.WHEN, new WhenHandler(),
        SpockLabel.THEN, new ThenOrExpectHandler(),
        SpockLabel.EXPECT, new ThenOrExpectHandler(),
        SpockLabel.AND, new AndHandler(),
        SpockLabel.CLEANUP, new CleanupHandler(),
        SpockLabel.WHERE, new WhereHandler()
      );
    }

    SectionHandler forLabel(SpockLabel label) {
      return handlers.get(label);
    }

    class GivenSectionHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
        blockTranslation.addTranslating(section.statements, stPool, context);
        processMockReturnValueInteractions(section);
      }

      boolean processMockReturnValueInteractions(Section section) {
        // In 'given' we process all the interactions that mock return value.
        // These interactions include the ones defined in 'given:' section directly or the following
        // 'and:' blocks or the first 'then:'/'expect:' that follows this given section.
        boolean processed = processInteractions(
          DetectedMockInteraction.Operation.MOCK_RETURN_VALUE,
          section2interactions.get(section),
          blockTranslation,
          context
        );
        
        // for now the interactions from 'then:'/'expect:' processed directly within 'given:'
        // section as it seems to be the most common pattern spread. However, note that 
        // in case the structure has 'and:' sections following this 'given:' section.
        // Note that this has to happen in the last and section of the block, otherwise variables
        // declared in 'and:' sections following given won't be correctly used in those mocks.
        final Section next = sections.next(section);
        if (next != null && next.label != SpockLabel.AND) {
          final Section thenOrExpect = sections.following(section, s -> s.label == SpockLabel.THEN || s.label == SpockLabel.EXPECT);
          if (thenOrExpect != null) {
            processed |= processInteractions(
              DetectedMockInteraction.Operation.MOCK_RETURN_VALUE,
              section2interactions.get(thenOrExpect),
              blockTranslation,
              context
            );

            Section thenOrExpectNext = thenOrExpect;
            while (
              (thenOrExpectNext = sections.next(thenOrExpectNext)) != null 
              && (thenOrExpectNext.label == SpockLabel.AND || thenOrExpectNext.label == SpockLabel.THEN)) {
              processed |= processInteractions(
                DetectedMockInteraction.Operation.MOCK_RETURN_VALUE,
                section2interactions.get(thenOrExpectNext),
                blockTranslation,
                context
              );
            }
          }
        }

        return processed;
      }
    }

    class WhenHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
        // if there is no given mocks have to be initialized somewhere
        if (givenSectionHandler.processMockReturnValueInteractions(section)) {
          blockTranslation.add(new JEmptyStatement(), context);
        }

        if (!thrown.wraps(section)) {
          blockTranslation.addTranslating(section.statements, stPool, context);
        }
      }
    }

    class ThenOrExpectHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
        for (Statement statement : section.statements) {
          final Assertion assertion = assertionsDetector.detect(statement, context);
          if (assertion instanceof ThrowsAssertion) {
            ((ThrowsAssertion) assertion).wrappedStatements(thrown.wrapped(section, sections));
          }
          if (assertion != null) {
            final JForeignStatement jAssertion = assertionsTranslator.translate(assertion, context);
            blockTranslation.add(jAssertion, context);
            TransformationsQueue.instance().enqueueNewImports(jAssertion.imports());
            continue;
          }

          // atm assuming engine shouldn't care about it, so simply removing the line otherwise 
          // if there are cases for this to be handled the engine provider must be asked to take care of it
          if (isNoExceptionThrownStatement(statement)) {
            blockTranslation.add(new JExpressionStatement(new JCommentExpression("noExceptionThrown")), context);
            continue;
          }

          blockTranslation.add(stPool.translate(statement, context), context);
        }

        processVerifyMethodCalledInteractions(section);
      }

      private void processVerifyMethodCalledInteractions(Section section) {
        // similar to mocking values which are defined in 'then:'/'expect:' we need to 
        // process verifications defined in all the sections preceding this.
        if (section.label == SpockLabel.THEN || section.label == SpockLabel.EXPECT) {
          for (Section preceding : sections.allPrecedingByFlowId(section, section.subFlowIdx)) {
            processInteractions(
              DetectedMockInteraction.Operation.VERIFY_METHOD_CALLED,
              section2interactions.get(preceding),
              blockTranslation,
              context
            );
          }
        }

        // 'and:' sections following 'then:'/'expect' end up here
        processInteractions(
          DetectedMockInteraction.Operation.VERIFY_METHOD_CALLED,
          section2interactions.get(section),
          blockTranslation,
          context
        );
      }
    }

    class AndHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
        final Section previous = sections.preceding(section, s -> s.label != SpockLabel.AND);
        if (section == null) {
          throw new TranslationException("No non 'AND' section found for section " + section);
        }
        handlers.get(previous.label).handle(section);
      }
    }

    class CleanupHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
        blockTranslation.addTranslating(section.statements, stPool, context);
      }
    }

    class WhereHandler implements SectionHandler {
      @Override
      public void handle(Section section) {
      }
    }
  }

  private void customiseAsParameterizedTest(
    JMethod method,
    Section whereSection,
    List<Statement> statements,
    TContext context
  ) {
    final var providerMethodName = method.name() + "ValuesProvider";
    engineTemplate.customizeParameterizedTestMethod(method, providerMethodName);

    Where where = Where.from(whereSection.statements);

    // analysing types to come up with declaration better than 'Object parameter'
    // we can look at values (e.g. numbers, strings, new Something, method signatures where those are passed)
    final var columns = where.columns();
    final var typeDeriver = new TypeDeriver(typeLoader);
    for (Where.Column column : columns) {
      final var typeCollector = new TypeCollector(runtimeLookup);
      typeCollector.collectFromInScopeUsage(column.name, statements, context);
      typeCollector.collectFromContextDeclarations(column.name, context);
      if (column.valuesProvider == null) {
        typeCollector.collectFromExpressionsThatResolveTypes(column.values, context);
      }
      final JType derivedType = shorten(typeDeriver.derive(typeCollector.types(), context), context);
      if (!derivedType.isJavaLang()
          && !derivedType.isInPackage(context.package0())
          && !derivedType.namespace().isEmpty()) {
        TransformationsQueue.instance().enqueueNewImports(new JImport(derivedType.fqn(), false));
      }
      method.parameters().add(new JParameter(derivedType, column.name));
    }

    final var fc = context;
    List<ArgumentValues> argValues = new ArrayList<>(columns.size());
    for (int i = 0; i < method.parameters().size(); i++) {
      final Where.Column column = columns.get(i);
      argValues.add(
        new ArgumentValues(
          method.parameters().get(i),
          column.values.stream().map(v -> exPool.translate(v, fc)).collect(Collectors.toList()),
          column.valuesProvider == null ? null : exPool.translate(column.valuesProvider, context)
        )
      );
    }

    TransformationsQueue.instance().enqueue(
      TransformationsQueue.Target.CLASS,
      context.path().containingClass(),
      TransformationsQueue.Priority.IN_THE_END,
      jAstNode -> {
        final var jClass = (JClass) jAstNode;
        jClass.addMethod(engineTemplate.createArgumentsProviderMethod(providerMethodName, argValues));
      }
    );
  }

  private boolean processInteractions(
    DetectedMockInteraction.Operation operation,
    List<DetectedMockInteraction> interactions,
    BlockTranslation blockTranslation,
    TContext context
  ) {
    if (interactions == null) {
      return false;
    }

    boolean processed = false;
    final Iterator<DetectedMockInteraction> iterator = interactions.iterator();
    while (iterator.hasNext()) {
      final DetectedMockInteraction interaction = iterator.next();
      if (interaction.operation() == operation) {
        iterator.remove();
        final JForeignStatement translated = mockTranslator.translateInteraction(interaction, context);
        blockTranslation.add(translated, context);
        TransformationsQueue.instance().enqueueNewImports(translated.imports());
        processed = true;
      }
    }
    return processed;
  }

  private boolean isConfiguredToAddLabelAsComment(Sections sections) {
    return translationConfig.spockLabelsPresenceMode() == PRESENT
           || translationConfig.spockLabelsPresenceMode() == PRESENT_ONLY_WHEN_HAVE_COMMENTS && sections.atLeastOneSectionHasComment();
  }

  private static JType shorten(JType type, TContext context) {
    // for inner classes we'd like to use shorter names if possible
    if (type.name().contains(".")) {
      for (JImport anImport : context.imports()) {
        if (anImport.isStatic() && anImport.fqn().equals(type.fqn())) {
          return new JType(
            type.fqn(),
            type.fqn().last().asString(),
            type.generics(),
            type.useGenerics()
          );
        }
      }
    }
    return type;
  }

  private static boolean isNoExceptionThrownStatement(Statement statement) {
    if (!(statement instanceof ExpressionStatement)) {
      return false;
    }
    final var exStatement = (ExpressionStatement) statement;
    if (!(exStatement.getExpression() instanceof MethodCallExpression)) {
      return false;
    }
    return ((MethodCallExpression) exStatement.getExpression()).getMethodAsString().equals("noExceptionThrown");
  }
}
