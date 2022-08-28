package com.yevhenii.nospock.translator.spock;

import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.translator.SpockSourceFile;
import com.yevhenii.nospock.translator.TransformationsQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CasesTest {

  @AfterEach
  void cleanup() {
    TransformationsQueue.instance().reset();
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "AssertionsSpec",
      "MockInitializationSpec",
      "MockInteractionSpec",
      "ParameterisedSpec",
      "ThrownSpec",
      "LifecycleSpec",
      "StringsSpec",
      "ArbitrarySpec"
    }
  )
  void translatedCorrectly(String caseName) throws Exception {
    final var groovyPath = getGroovyPath(caseName);
    final var javaPath = getJavaPath(caseName);

    assertEquals(
      Files.readString(javaPath),
      Defaults.translators().fileTranslator().translate(new SpockSourceFile(groovyPath)).asCode(Defaults.CODE_STYLE)
    );
  }

  private static Path getGroovyPath(String name) {
    return Paths.get("src", "test", "groovy", "cases", name + ".groovy");
  }

  private static Path getJavaPath(String name) {
    return Paths.get("src", "test", "resources", "cases", name + ".java");
  }
}
