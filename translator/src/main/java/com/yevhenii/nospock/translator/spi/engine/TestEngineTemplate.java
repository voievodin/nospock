package com.yevhenii.nospock.translator.spi.engine;

import com.yevhenii.nospock.jast.JMethod;

import java.util.List;

public interface TestEngineTemplate {

  void customizeTestMethod(JMethod method);

  void customizeBeforeEachMethod(JMethod method);

  void customizeAfterEachMethod(JMethod method);

  void customizeBeforeAll(JMethod method);

  void customizeAfterAll(JMethod method);

  JMethod createArgumentsProviderMethod(String name, List<ArgumentValues> values);

  void customizeParameterizedTestMethod(JMethod method, String providerMethodName);
}
