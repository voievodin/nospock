package com.yevhenii.nospock;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.Fqn;
import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JFile;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.translator.SpockSourceFile;
import com.yevhenii.nospock.translator.TranslationConfig;
import com.yevhenii.nospock.translator.TranslationConfig.SpockLabelPresenceMode;
import com.yevhenii.nospock.translator.Translators;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.translator.spi.assertion.AssertionsTemplate;
import com.yevhenii.nospock.translator.spi.assertion.impl.junit.JUnitAssertionsTemplate;
import com.yevhenii.nospock.translator.spi.engine.TestEngineTemplate;
import com.yevhenii.nospock.translator.spi.engine.impl.junit.JUnitEngineTemplate;
import com.yevhenii.nospock.translator.spi.mock.MockTemplate;
import com.yevhenii.nospock.translator.spi.mock.impl.mockito.MockitoMockTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = {"translate", "s2j"})
class TranslateCommand {

  @Parameter(names = "-rename-pattern")
  private String renameTestClassPattern = "Spec -> Test, Specification -> Test";

  @Parameter(names = "-assertions-provider")
  private String assertionsProvider = "junit";

  @Parameter(names = "-mocks-provider")
  private String mocksProvider = "mockito";

  @Parameter(names = "-engine-provider")
  private String engineProvider = "junit";

  @Parameter(names = "--static-imports-assertions")
  private boolean useStaticImportsForAssertions;

  @Parameter(names = "--static-imports-mocks")
  private boolean useStaticImportsForMocks;

  @Parameter(names = {"-input-file", "-i"}, required = true)
  private String inputFile;

  @Parameter(names = {"-output-directory", "-o"})
  private String outputDirectory;

  @Parameter(names = "-remove-superclasses")
  private String removeSuperclasses;

  @Parameter(names = "-spock-labels-presence-mode")
  private String spockLabelsPresenceMode = SpockLabelPresenceMode.MISSING.name();

  @Parameter(names = "--text-blocks-enabled")
  private boolean textBlocksEnabled;

  void run() throws IOException {
    final Translators translators = new Translators(
      new TranslationConfig()
        .spockLabelsPresenceMode(SpockLabelPresenceMode.valueOf(spockLabelsPresenceMode))
        .enableTextBlocks(textBlocksEnabled),
      TranslateCommand.class.getClassLoader(),
      selectEngineProvider(),
      selectAssertionsProvider(),
      selectMocksProvider()
    );
    final JFile jFile = translators.fileTranslator().translate(new SpockSourceFile(new File(inputFile).toPath()));

    final JClass mainClass = jFile.mainClass();
    if (mainClass != null && removeSuperclasses != null && !removeSuperclasses.isBlank()) {
      for (String superclass : removeSuperclasses.split(",")) {
        if (mainClass.superclass() != null && mainClass.superclass().name().equals(superclass)) {
          mainClass.superclass(null);
          jFile.imports().removeIf(anImport -> anImport.fqn().last().asString().equals(superclass));
        }
      }
    }

    if (renameTestClassPattern != null && !renameTestClassPattern.isBlank()) {
      new Renamer(renameTestClassPattern).rename(jFile);
    }

    final CodeStyle style = new CodeStyle().setIndent("    ");
    if (outputDirectory == null) {
      System.out.println(jFile.asCode(style));
    } else {
      jFile.write(new File(outputDirectory).toPath(), style);
    }
  }

  private TestEngineTemplate selectEngineProvider() {
    switch (engineProvider) {
      case "junit":
        return new JUnitEngineTemplate();
      default:
        throw new IllegalStateException("Not supported engine provider " + engineProvider);
    }
  }

  private AssertionsTemplate selectAssertionsProvider() {
    switch (assertionsProvider) {
      case "junit":
        return new JUnitAssertionsTemplate(useStaticImportsForAssertions);
      default:
        throw new IllegalStateException("Not supported assertions provider " + engineProvider);
    }
  }

  private MockTemplate selectMocksProvider() {
    switch (mocksProvider) {
      case "mockito":
        return new MockitoMockTemplate(useStaticImportsForMocks);
      default:
        throw new IllegalStateException("Not supported mocks provider " + engineProvider);
    }
  }

  private static class Renamer {

    private final List<String[]> pairs = new ArrayList<>();

    Renamer(String patterns) {
      for (String pattern : patterns.split(",")) {
        final String[] pair = pattern.split("->");
        if (pair.length != 2) {
          throw new TranslationException(
            String.format(
              "Invalid pattern format, each pattern must have format of 'from -> to' separated by commas. While got '%s'",
              patterns
            )
          );
        }
        pair[0] = pair[0].trim();
        pair[1] = pair[1].trim();
        pairs.add(pair);
      }
    }

    void rename(JFile file) {
      String fileName = file.name();
      for (String[] pair : pairs) {
        fileName = fileName.replace(pair[0], pair[1]);
      }
      file.rename(fileName);

      final JType currentSuperclass = file.mainClass().superclass();
      if (currentSuperclass != null) {
        String newSuperclassName = currentSuperclass.name();
        for (String[] pair : pairs) {
          newSuperclassName = newSuperclassName.replace(pair[0], pair[1]);
        }
        if (!newSuperclassName.equals(currentSuperclass.name())) {
          file.mainClass().superclass(
            new JType(
              new Fqn(newSuperclassName),
              newSuperclassName,
              currentSuperclass.generics(),
              currentSuperclass.useGenerics()
            )
          );
          for (JImport anImport : file.imports()) {
            if (anImport.fqn().last().asString().equals(currentSuperclass.name())) {
              anImport.fqn(anImport.fqn().withoutLastOrEmpty().add(newSuperclassName));
              break;
            }
          }
        }
      }
    }
  }
}
