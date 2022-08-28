package com.yevhenii.nospock.translator.spi.mock.impl.mockito;

import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.jast.stmt.JThrowStatement;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.exp.*;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.jast.stmt.JReturnStatement;
import com.yevhenii.nospock.translator.TranslateHelper;
import com.yevhenii.nospock.translator.spock.JForeignExpression;
import com.yevhenii.nospock.translator.spock.JForeignStatement;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate;
import com.yevhenii.nospock.translator.spock.mock.MockType;

import java.util.ArrayList;
import java.util.List;

public class MockitoMockTemplate implements MockTemplate {

  private final boolean useStaticImports;

  public MockitoMockTemplate(boolean useStaticImports) {
    this.useStaticImports = useStaticImports;
  }

  @Override
  public JForeignExpression initialization(
    MockType mockType,
    JExpression classReference,
    JLambdaExpression answer
  ) {
    if (mockType == MockType.MOCK) {
      return new JForeignExpression(
        new JMethodCallExpression(
          mockitoOrThis(),
          "mock",
          answer == null ? List.of(classReference) : List.of(classReference, convertAnswerToBeMockitoAnswer(answer))
        ),
        List.of(importFor("mock"))
      );
    } else if (mockType == MockType.SPY) {
      final var spyInitialization = new JForeignExpression(
        new JMethodCallExpression(
          mockitoOrThis(),
          "spy",
          List.of(classReference)
        ),
        List.of(importFor("spy"))
      );
      if (answer == null) {
        return spyInitialization;
      } else {
        return new JForeignExpression(
          new JCommentExpression(
            "TODO: cannot initialize spy with preset answer, do it yourself: " + spyInitialization.asCode(new CodeStyle())
          ),
          List.of()
        );
      }
    }
    throw new IllegalStateException("Not supported mock type " + mockType);
  }

  @Override
  public JForeignStatement mockReturnValue(
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers,
    JExpression thenReturn
  ) {
    final var mappedMatchers = new MappedMatchers(matchers);
    mappedMatchers.imports.add(importFor("when"));
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          new JMethodCallExpression(
            mockitoOrThis(),
            "when",
            List.of(
              new JMethodCallExpression(
                object,
                methodName,
                mappedMatchers.expressions
              )
            )
          ),
          "thenReturn",
          List.of(thenReturn)
        )
      ),
      mappedMatchers.imports
    );
  }

  // Mockito.verify(object).call(Mockito.eq(123))
  @Override
  public JForeignStatement verifyTimesCalled(
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers,
    Times times
  ) {
    final var mappedMatchers = new MappedMatchers(matchers);
    mappedMatchers.imports.add(importFor("verify"));
    switch (times.type) {
      case EXACTLY:
        mappedMatchers.imports.add(importFor("times"));
        return new JForeignStatement(
          new JExpressionStatement(
            new JMethodCallExpression(
              new JMethodCallExpression(
                mockitoOrThis(),
                "verify",
                List.of(
                  object,
                  new JMethodCallExpression(mockitoOrThis(), "times", List.of(times.expression))
                )
              ),
              methodName,
              mappedMatchers.expressions
            )
          ),
          mappedMatchers.imports
        );
      case ONCE:
        return new JForeignStatement(
          new JExpressionStatement(
            new JMethodCallExpression(
              new JMethodCallExpression(mockitoOrThis(), "verify", List.of(object)),
              methodName,
              mappedMatchers.expressions
            )
          ),
          mappedMatchers.imports
        );
      case NEVER:
        mappedMatchers.imports.add(importFor("never"));
        return new JForeignStatement(
          new JExpressionStatement(
            new JMethodCallExpression(
              new JMethodCallExpression(
                mockitoOrThis(),
                "verify",
                List.of(
                  object,
                  new JMethodCallExpression(mockitoOrThis(), "never", List.of())
                )
              ),
              methodName,
              mappedMatchers.expressions
            )
          ),
          mappedMatchers.imports
        );
      case AT_LEAST:
        mappedMatchers.imports.add(importFor("atLeast"));
        return new JForeignStatement(
          new JExpressionStatement(
            new JMethodCallExpression(
              new JMethodCallExpression(
                mockitoOrThis(),
                "verify",
                List.of(
                  object,
                  new JMethodCallExpression(mockitoOrThis(), "atLeast", List.of(times.expression))
                )
              ),
              methodName,
              mappedMatchers.expressions
            )
          ),
          mappedMatchers.imports
        );
      default:
        throw new TranslationException("Not supported times type " + times);
    }
  }

  @Override
  public JForeignStatement doAnswer(
    ArgumentsAssignment argumentsAssignment,
    JBlockStatement block,
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers
  ) {
    defineArgumentsBeforeOtherStatements(block, argumentsAssignment, "invocation");
    addReturnsNullIfNoReturnOrThrownStatements(block);
    final var doAnswer = new JMethodCallExpression(
      mockitoOrThis(),
      "doAnswer",
      List.of(
        new JLambdaExpression(
          List.of(new JParameter(JType.object(), "invocation")),
          block
        )
      )
    );

    final var mappedMatchers = new MappedMatchers(matchers);
    mappedMatchers.imports.add(importFor("doAnswer"));
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          new JMethodCallExpression(
            doAnswer,
            "when",
            List.of(object)
          ),
          methodName,
          mappedMatchers.expressions
        )
      ),
      mappedMatchers.imports
    );
  }

  private MappedMatcher mapArgumentMatcher(ArgumentMatcher matcher) {
    switch (matcher.rule) {
      case EQ:
        return new MappedMatcher(
          importFor("eq"),
          new JMethodCallExpression(mockitoOrThis(), "eq", List.of(matcher.value))
        );
      case ANY:
        return new MappedMatcher(
          importFor("any"),
          new JMethodCallExpression(mockitoOrThis(), "any", List.of())
        );
      case ANY_BYTE:
        return new MappedMatcher(
          importFor("anyByte"),
          new JMethodCallExpression(mockitoOrThis(), "anyByte", List.of())
        );
      case ANY_SHORT:
        return new MappedMatcher(
          importFor("anyShort"),
          new JMethodCallExpression(mockitoOrThis(), "anyShort", List.of())
        );
      case ANY_INT:
        return new MappedMatcher(
          importFor("anyInt"),
          new JMethodCallExpression(mockitoOrThis(), "anyInt", List.of())
        );
      case ANY_LONG:
        return new MappedMatcher(
          importFor("anyLong"),
          new JMethodCallExpression(mockitoOrThis(), "anyLong", List.of())
        );
      case ANY_CHAR:
        return new MappedMatcher(
          importFor("anyChar"),
          new JMethodCallExpression(mockitoOrThis(), "anyChar", List.of())
        );
      case ANY_BOOLEAN:
        return new MappedMatcher(
          importFor("anyBoolean"),
          new JMethodCallExpression(mockitoOrThis(), "anyBoolean", List.of())
        );
      case ANY_FLOAT:
        return new MappedMatcher(
          importFor("anyFloat"),
          new JMethodCallExpression(mockitoOrThis(), "anyFloat", List.of())
        );
      case ANY_DOUBLE:
        return new MappedMatcher(
          importFor("anyDouble"),
          new JMethodCallExpression(mockitoOrThis(), "anyDouble", List.of())
        );
      case ARG_THAT:
        return new MappedMatcher(
          importFor("argThat"),
          new JMethodCallExpression(mockitoOrThis(), "argThat", List.of(matcher.value))
        );
      case ANY_OF_TYPE:
        return new MappedMatcher(
          importFor("any"),
          new JMethodCallExpression(mockitoOrThis(), "any", List.of(matcher.value))
        );
      default:
        throw new TranslationException("Not supported matcher rule " + matcher.rule);
    }
  }

  private static class MappedMatcher {
    final JImport import0;
    final JExpression expression;

    MappedMatcher(JImport import0, JExpression expression) {
      this.import0 = import0;
      this.expression = expression;
    }
  }

  private class MappedMatchers {

    final List<JExpression> expressions = new ArrayList<>();
    final List<JImport> imports = new ArrayList<>();

    MappedMatchers(List<ArgumentMatcher> matchers) {
      // in case every matcher is eq matcher we can pass expressions directly
      if (isEveryMatcherIsEqMatcher(matchers)) {
        for (ArgumentMatcher matcher : matchers) {
          expressions.add(matcher.value);
        }
      } else {
        for (ArgumentMatcher matcher : matchers) {
          final var mappedMatcher = mapArgumentMatcher(matcher);
          expressions.add(mappedMatcher.expression);
          imports.add(mappedMatcher.import0);
        }
      }
    }

    private boolean isEveryMatcherIsEqMatcher(List<ArgumentMatcher> matchers) {
      for (ArgumentMatcher matcher : matchers) {
        if (matcher.rule != ArgumentMatcher.Rule.EQ) {
          return false;
        }
      }
      return true;
    }
  }

  private static void defineArgumentsBeforeOtherStatements(
    JBlockStatement invocationLambdaBlock,
    ArgumentsAssignment assignment,
    String invocationName
  ) {
    int pos = 0;

    if (assignment.argsVariable != null) {
      invocationLambdaBlock.statements().add(
        pos++,
        new JExpressionStatement(
          new JDeclarationExpression(
            assignment.argsVariable,
            new JMethodCallExpression(new JVariableExpression(invocationName), "getArguments")
          )
        )
      );
    }

    if (assignment.parameterVariables != null) {
      for (int i = 0; i < assignment.parameterVariables.size(); i++) {
        final JVariableExpression pVar = assignment.parameterVariables.get(i);
        final JArrayElementAccessExpression argumentValueAccessExp = new JArrayElementAccessExpression(
          new JMethodCallExpression(new JVariableExpression(invocationName), "getArguments"),
          new JConstantExpression(i)
        );
        invocationLambdaBlock.statements().add(
          pos++,
          new JExpressionStatement(
            new JDeclarationExpression(
              pVar,
              !pVar.isDeclaration() || pVar.type().isJavaLangObject() ?
              argumentValueAccessExp :
              new JCastExpression(pVar.type(), argumentValueAccessExp)
            )
          )
        );
      }
    }
  }

  private static void addReturnsNullIfNoReturnOrThrownStatements(JBlockStatement block) {
    final var returns = TranslateHelper.findNestedStatementsOfType(block, JReturnStatement.class);
    if (
      returns.isEmpty() && 
      !block.statements().isEmpty() 
      && !(block.statements().get(block.statements().size() - 1) instanceof JThrowStatement)
    ) {
      block.statements().add(new JReturnStatement(new JConstantExpression(null)));
    }
  }

  private JAstNode mockitoOrThis() {
    return useStaticImports ? JVariableExpression.this0() : new JType("Mockito");
  }

  private JImport importFor(String method) {
    final var fqn = "org.mockito.Mockito";
    if (useStaticImports) {
      return new JImport(fqn + '.' + method, true);
    } else {
      return new JImport(fqn, false);
    }
  }

  private static JLambdaExpression convertAnswerToBeMockitoAnswer(JLambdaExpression answer) {
    final var result = new JBlockStatement();
    result.statements().add(
      new JExpressionStatement(
        new JCommentExpression("TODO: cannot initialize mock with preset answer (not yet supported)")
      )
    );
    for (JStatement statement : answer.block().statements()) {
      result.statements().add(
        new JExpressionStatement(
          new JCommentExpression(statement.asCode(new CodeStyle()))
        )
      );
    }
    result.statements().add(new JReturnStatement(new JConstantExpression(null)));
    return new JLambdaExpression(List.of(new JParameter(new JType("MockInvocation"), "invocation")), result);
  }
}
