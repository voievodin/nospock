
package com.yevhenii.nospock;

import com.yevhenii.nospock.translator.TranslationConfig;
import com.yevhenii.nospock.translator.Translators;
import com.yevhenii.nospock.translator.spi.assertion.impl.junit.JUnitAssertionsTemplate;
import com.yevhenii.nospock.translator.spi.engine.impl.junit.JUnitEngineTemplate;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.translator.spi.mock.impl.mockito.MockitoMockTemplate;
import com.yevhenii.nospock.translator.CPath;
import com.yevhenii.nospock.translator.TContext;

import java.util.List;

public class Defaults {

  public static Translators translators() {
    return new Translators(
      new TranslationConfig(),
      Defaults.class.getClassLoader(),
      new JUnitEngineTemplate(),
      new JUnitAssertionsTemplate(false),
      new MockitoMockTemplate(false)
    );
  }

  public static final CodeStyle CODE_STYLE = new CodeStyle()
    .setNlAfterPackage(1)
    .setNlAfterNonStaticImports(1)
    .setNlAfterStaticImports(1)
    .setNlBeforeFieldsGroup(1)
    .setNlBeforeMethod(1)
    .setNlBeforeArbitraryAnonymousClassBlock(0)
    .setNlSequence("\n")
    .setMultilineArgumentsThreshold(3)
    .setIndent("  ");

  public static TContext context() {
    return new TContext(
      null,
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      CPath.ROOT
    );
  }
}
