package com.yevhenii.nospock.translator.spi.engine.impl.junit;

import com.yevhenii.nospock.jast.*;
import com.yevhenii.nospock.jast.exp.JArrayExpression;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JDeclarationExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.jast.stmt.JReturnStatement;
import com.yevhenii.nospock.jast.stmt.JWhileStatement;
import com.yevhenii.nospock.translator.TransformationsQueue;
import com.yevhenii.nospock.translator.spi.engine.ArgumentValues;
import com.yevhenii.nospock.translator.spi.engine.TestEngineTemplate;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitEngineTemplate implements TestEngineTemplate {

  @Override
  public void customizeTestMethod(JMethod method) {
    method.annotate(new JAnnotationUsage("Test"));
    TransformationsQueue.instance().enqueueNewImports(
      new JImport("org.junit.jupiter.api.Test", false)
    );
  }

  @Override
  public void customizeBeforeEachMethod(JMethod method) {
    method.annotate(new JAnnotationUsage("BeforeEach"));
    TransformationsQueue.instance().enqueueNewImports(
      List.of(new JImport("org.junit.jupiter.api.BeforeEach", false))
    );
  }

  @Override
  public void customizeAfterEachMethod(JMethod method) {
    method.annotate(new JAnnotationUsage("AfterEach"));
    TransformationsQueue.instance().enqueueNewImports(
      List.of(new JImport("org.junit.jupiter.api.AfterEach", false))
    );
  }

  @Override
  public void customizeBeforeAll(JMethod method) {
    method.annotate(new JAnnotationUsage("BeforeAll"));
    method.modifiers(method.modifiers() | Modifier.STATIC);
    TransformationsQueue.instance().enqueueNewImports(
      List.of(new JImport("org.junit.jupiter.api.BeforeAll", false))
    );
  }

  @Override
  public void customizeAfterAll(JMethod method) {
    method.annotate(new JAnnotationUsage("AfterAll"));
    method.modifiers(method.modifiers() | Modifier.STATIC);
    TransformationsQueue.instance().enqueueNewImports(
      List.of(new JImport("org.junit.jupiter.api.AfterAll", false))
    );
  }

  @Override
  public JMethod createArgumentsProviderMethod(String name, List<ArgumentValues> values) {
    int predefined = 0;
    int dynamic = 0;
    for (ArgumentValues value : values) {
      if (value.valuesProvider == null) {
        predefined++;
      } else {
        dynamic++;
      }
    }

    if (dynamic == 0) {
      return apForPredefinedValues(name, values);
    } else if (predefined > 0) {
      return apForPredefinedAndDynamicValues(name, values);
    } else if (dynamic == 1) {
      return apForOneDynamicSource(name, values);
    } else {
      return apForManyDynamicSources(name, values);
    }
  }

  @Override
  public void customizeParameterizedTestMethod(JMethod method, String providerMethodName) {
    TransformationsQueue.instance().enqueueNewImports(
      new JImport("org.junit.jupiter.params.ParameterizedTest", false),
      new JImport("org.junit.jupiter.params.provider.MethodSource", false)
    );
    method.annotate(new JAnnotationUsage("ParameterizedTest"));
    method.annotate(new JAnnotationUsage("MethodSource", new JConstantExpression(providerMethodName)));
  }

  /**
   * Generates {@code private static Stream<Arguments> providerName() { ... }}.
   */
  private JMethod apForPredefinedValues(String name, List<ArgumentValues> values) {
    TransformationsQueue.instance().enqueueNewImports(
      List.of(
        new JImport("java.util.stream.Stream", false),
        new JImport("org.junit.jupiter.params.provider.Arguments", false)
      )
    );

    int valuesSize = values.get(0).values.size();
    final List<JExpression> argumentsOf = new ArrayList<>(valuesSize);
    if (values.size() == 1) {
      for (JExpression value : values.get(0).values) {
        if (value instanceof JConstantExpression && ((JConstantExpression) value).isNull()) {
          // special case when you cannot pass varargs correctly without explicitly wrapping in array
          argumentsOf.add(
            new JMethodCallExpression(
              new JType("Arguments"),
              "of",
              List.of(new JArrayExpression(new JType("Object[]"), List.of(value), List.of()))
            )
          );
        } else {
          argumentsOf.add(new JMethodCallExpression(new JType("Arguments"), "of", List.of(value)));
        }
      }
    } else {
      for (int i = 0; i < valuesSize; i++) {
        final var fi = i;
        argumentsOf.add(
          new JMethodCallExpression(
            new JType("Arguments"),
            "of",
            values.stream().map(v -> v.values.get(fi)).collect(Collectors.toList())
          )
        );
      }
    }

    return new JMethod(
      name,
      new JType("Stream", List.of("Arguments")),
      Modifier.PRIVATE | Modifier.STATIC,
      new JBlockStatement(
        new JReturnStatement(
          new JMethodCallExpression(new JType("Stream"), "of", argumentsOf)
        )
      ),
      List.of(),
      List.of()
    );
  }

  /**
   * Generates {@code private static Object providerName() { return valueProvider; }}.
   */
  private JMethod apForOneDynamicSource(String name, List<ArgumentValues> values) {
    return new JMethod(
      name,
      JType.object(),
      Modifier.PRIVATE | Modifier.STATIC,
      new JBlockStatement(new JReturnStatement(values.get(0).valuesProvider)),
      List.of(),
      List.of()
    );
  }

  /**
   * Delegates to {@link #apForManyDynamicSources} by converting predefined to dynamic.
   */
  private JMethod apForPredefinedAndDynamicValues(String name, List<ArgumentValues> values) {
    final var dynamicSources = new ArrayList<ArgumentValues>(values.size());
    for (ArgumentValues argValues : values) {
      if (argValues.valuesProvider == null) {
        dynamicSources.add(
          new ArgumentValues(
            argValues.parameter,
            List.of(),
            new JMethodCallExpression(
              new JVariableExpression("List"),
              "of",
              argValues.values
            )
          )
        );
      } else {
        dynamicSources.add(argValues);
      }
    }
    // since it's called there is at least one predefined values provider
    TransformationsQueue.instance().enqueueNewImports(new JImport("java.util.List", false));
    return apForManyDynamicSources(name, dynamicSources);
  }

  /**
   * Generates {@code private static Stream<Arguments> providerName() { ... }}, where
   * each value provider is assumed to be {@link Iterable}. The arguments are filled by iterating over
   * every dynamic source.
   */
  private JMethod apForManyDynamicSources(String name, List<ArgumentValues> argValuesList) {
    TransformationsQueue.instance().enqueueNewImports(
      List.of(
        new JImport("java.util.ArrayList", false),
        new JImport("java.util.List", false),
        new JImport("org.junit.jupiter.params.provider.Arguments", false)
      )
    );
    
    final var block = new JBlockStatement();

    // for each values provider declare iterator variable named equally to parameter name, so we have
    // var v1 = collection().iterator();
    // var v2 = MyEnum.values().iterator();
    for (ArgumentValues argValues : argValuesList) {
      block.statements().add(
        new JExpressionStatement(
          new JDeclarationExpression(
            JVariableExpression.var(new JType(Iterator.class), argValues.parameter.name()),
            new JMethodCallExpression(
              argValues.valuesProvider,
              "iterator"
            )
          )
        )
      );
    }

    // create a collection that will be arguments container
    // var provider = new ArrayList<Arguments>();
    final JType listOfArgumentsType = new JType("ArrayList", List.of("Arguments"));
    final JVariableExpression provider = JVariableExpression.var(listOfArgumentsType, "provider");
    block.statements().add(
      new JExpressionStatement(
        new JDeclarationExpression(
          provider,
          new JConstructorCallExpression(listOfArgumentsType)
        )
      )
    );

    // iterator over all the value providers and create arguments for every iterator value
    // while (v1.hasNext()) {
    //   provider.add(Arguments.of(v1.next(), v2.next());
    // }
    final String firstIteratorName = argValuesList.get(0).parameter.name();
    block.statements().add(
      new JWhileStatement(
        new JMethodCallExpression(new JVariableExpression(firstIteratorName), "hasNext", List.of()),
        new JBlockStatement(
          new JExpressionStatement(
            new JMethodCallExpression(
              new JVariableExpression(provider.name()),
              "add",
              List.of(
                new JMethodCallExpression(
                  new JVariableExpression("Arguments"),
                  "of",
                  argValuesList.stream()
                    .map(
                      argValues -> new JMethodCallExpression(
                        new JVariableExpression(argValues.parameter.name()),
                        "next",
                        List.of()
                      )
                    )
                    .collect(Collectors.toList())
                )
              )
            )
          )
        )
      )
    );
    
    // return provider
    block.statements().add(new JReturnStatement(new JVariableExpression(provider.name())));
    
    return new JMethod(
      name,
      new JType("List", List.of("Arguments")),
      Modifier.PRIVATE | Modifier.STATIC,
      block,
      List.of(),
      List.of()
    );
  }
}
