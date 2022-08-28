package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.JMethod;
import com.yevhenii.nospock.translator.BlockTranslation;
import com.yevhenii.nospock.translator.CPath;
import com.yevhenii.nospock.translator.StPool;
import com.yevhenii.nospock.translator.TContext;
import com.yevhenii.nospock.translator.TransformationsQueue;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.Translator;
import com.yevhenii.nospock.translator.spi.engine.TestEngineTemplate;
import com.yevhenii.nospock.translator.spock.mock.DetectedMockInteraction;
import com.yevhenii.nospock.translator.spock.mock.MockDetector;
import com.yevhenii.nospock.translator.spock.mock.MockTranslator;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LifecycleTestMethodTranslator implements Translator<MethodNode, JMethod> {

  private static final Set<String> LC_METHODS = Set.of("setup", "cleanup", "setupSpec", "cleanupSpec");

  private final StPool stPool;
  private final TestEngineTemplate testLifecycleTemplate;
  private final MockDetector mockDetector;
  private final MockTranslator mockTranslator;

  public LifecycleTestMethodTranslator(
    StPool stPool,
    TestEngineTemplate testLifecycleTemplate,
    MockDetector mockDetector,
    MockTranslator mockTranslator
  ) {
    this.stPool = Objects.requireNonNull(stPool);
    this.testLifecycleTemplate = Objects.requireNonNull(testLifecycleTemplate);
    this.mockDetector = Objects.requireNonNull(mockDetector);
    this.mockTranslator = Objects.requireNonNull(mockTranslator);
  }

  @Override
  public JMethod translate(MethodNode node, TContext context) {
    context = context.deepen(CPath.Seg.forMethod(node.getName()));
    final var blockTranslation = new BlockTranslation();
    for (Statement statement : ((BlockStatement) node.getCode()).getStatements()) {
      final List<DetectedMockInteraction> interactions = mockDetector.detectInteractions(statement);
      if (interactions.isEmpty()) {
        blockTranslation.add(stPool.translate(statement, context), context);
      } else {
        for (DetectedMockInteraction interaction : interactions) {
          final JForeignStatement translated = mockTranslator.translateInteraction(interaction, context);
          TransformationsQueue.instance().enqueueNewImports(translated.imports);
          blockTranslation.add(translated, context);
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

    switch (method.name()) {
      case "setup":
        testLifecycleTemplate.customizeBeforeEachMethod(method);
        break;
      case "cleanup":
        testLifecycleTemplate.customizeAfterEachMethod(method);
        break;
      case "setupSpec":
        testLifecycleTemplate.customizeBeforeAll(method);
        break;
      case "cleanupSpec":
        testLifecycleTemplate.customizeAfterAll(method);
        break;
    }

    return method;
  }

  public boolean canTranslate(MethodNode node) {
    if (node.getParameters() != null && node.getParameters().length > 0) {
      return false;
    }
    return LC_METHODS.contains(node.getName());
  }
}

