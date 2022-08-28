package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JImport;

import org.codehaus.groovy.ast.ImportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportTranslatorTest {

  Translator<ImportNode, JImport> translator = Defaults.translators().translator(TKey.IMPORT);

  @Test
  public void nonStaticImport() {
    ImportNode node = Ast.locators("import java.util.List; class X {}").import0();

    JImport jImport = translator.translate(node, Defaults.context());

    assertEquals(jImport.asCode(new CodeStyle()), "import java.util.List;");
  }

  @Test
  public void nonStaticStarImport() {
    ImportNode node = Ast.locators("import java.util.*; class X {}").import0();

    JImport jImport = translator.translate(node, Defaults.context());

    assertEquals(jImport.asCode(new CodeStyle()), "import java.util.*;");
  }

  @Test
  public void staticImport() {
    ImportNode node = Ast.locators("import static java.util.List.of; class X {}").import0();

    JImport jImport = translator.translate(node, Defaults.context());

    assertEquals("import static java.util.List.of;", jImport.asCode(new CodeStyle()));
  }

  @Test
  public void staticStarImport() {
    ImportNode node = Ast.locators("import static java.util.List.*; class X {}").import0();

    JImport jImport = translator.translate(node, Defaults.context());

    assertEquals("import static java.util.List.*;", jImport.asCode(new CodeStyle()));
  }
}
