package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JPackage;

import org.codehaus.groovy.ast.PackageNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PackageTranslatorTest {

  Translator<PackageNode, JPackage> translator = Defaults.translators().translator(TKey.PACKAGE);

  @Test
  public void packageGeneration() {
    PackageNode node = Ast.locators("package x.y.z; class X {}").package0();

    JPackage p = translator.translate(node, Defaults.context());

    Assertions.assertEquals("package x.y.z;", p.asCode(new CodeStyle()));
  }
}
