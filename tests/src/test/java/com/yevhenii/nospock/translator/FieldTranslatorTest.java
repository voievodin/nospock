package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.JField;

import org.codehaus.groovy.ast.FieldNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTranslatorTest {

  Translator<FieldNode, JField> translator = Defaults.translators().translator(TKey.FIELD);

  @Test
  public void unassignedField() {
    FieldNode node = Ast.locators("class X { String f1; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("private String f1", jField.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void publicUnassignedField() {
    FieldNode node = Ast.locators("class X { public String f1; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("public String f1", jField.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void protectedField() {
    FieldNode node = Ast.locators("class X { protected String f1; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("protected String f1", jField.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void publicStaticFinalUnassignedField() {
    FieldNode node = Ast.locators("class X { public static final String f1; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("public static final String f1", jField.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void fieldWithValueAssigned() {
    FieldNode node = Ast.locators("class X { public String f1 = \"x\"; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("public String f1 = \"x\"", jField.asCode(Defaults.CODE_STYLE));
  }

  @Test
  public void annotatedField() {
    FieldNode node = Ast.locators("class X { @Autowired public String f1 = \"x\"; }").class0loc().field0();

    JField jField = translator.translate(node, Defaults.context());

    assertEquals("@Autowired\npublic String f1 = \"x\"", jField.asCode(Defaults.CODE_STYLE));
  }
}
