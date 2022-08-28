package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JMethod;

import org.codehaus.groovy.ast.MethodNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodTranslatorTest {

  Translator<MethodNode, JMethod> translator = Defaults.translators().translator(TKey.METHOD);

  @Test
  public void voidMethodWithoutParameters() {
    String source = """
      class X {
        void x() {}
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals("public void x() {\n}\n", jMethod.asCode(new CodeStyle()));
  }

  @Test
  public void voidMethodWithParameters() {
    String source = """
      class X {
        void x(int a, String b, Object c) {}
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals("public void x(int a, String b, Object c) {\n}\n", jMethod.asCode(new CodeStyle()));
  }

  @Test
  public void nonVoidReturnTypeWithoutParameters() {
    String source = """
      class X {
        String x() {}
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals("public String x() {\n}\n", jMethod.asCode(new CodeStyle()));
  }

  @Test
  public void voidMethodWithBody() {
    String source = """
      class X {
        void x() {
          def x = 1
          int y = 3
          Object z = null
        }
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals(
      """
        public void x() {
          var x = 1;
          int y = 3;
          Object z = null;
        }
        """,
      jMethod.asCode(new CodeStyle())
    );
  }

  @Test
  public void methodThatReturnsValue() {
    String source = """
      class X {
        int x() {
          return 123
        }
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals(
      """
        public int x() {
          return 123;
        }
        """,
      jMethod.asCode(new CodeStyle())
    );
  }

  @Test
  public void voidMethodWithReturnStatement() {
    String source = """
      class X {
        void x() {
          return;
        }
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals(
      """
        public void x() {
          return;
        }
        """,
      jMethod.asCode(new CodeStyle())
    );
  }

  @Test
  public void dynamicObjectReturnType() {
    String source = """
      class X {
        def x() {
          return call(123)
        }
      }
      """;

    JMethod jMethod = translator.translate(
      Ast.locators(source).class0loc().method0(),
      new TContext(
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        CPath.ROOT.add(CPath.Seg.forClass("X"))
      )
    );

    assertEquals(
      """
        public Object x() {
          return call(123);
        }
        """,
      jMethod.asCode(new CodeStyle())
    );
  }

  @Test
  public void dynamicVoidReturnType() {
    String source = """
      class X {
        def x() {
          call(123)
        }
      }
      """;

    JMethod jMethod = translator.translate(
      Ast.locators(source).class0loc().method0(),
      new TContext(
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        CPath.ROOT.add(CPath.Seg.forClass("X"))
      )
    );

    assertEquals(
      """
        public void x() {
          call(123);
        }
        """,
      jMethod.asCode(new CodeStyle())
    );
  }

  @Test
  public void annotatedMethod() {
    String source = """
      class X {
        @Unroll
        def x() {
        }
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals(
      """
        @Unroll
        public void x() {
        }
        """,
      jMethod.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void methodThatIncludesWhitespacesInName() {
    String source = """
      class X {
        def "this is a typical test method"() {}
      }
      """;

    JMethod jMethod = translator.translate(Ast.locators(source).class0loc().method0(), Defaults.context());

    assertEquals("public void thisIsATypicalTestMethod() {\n}\n", jMethod.asCode(new CodeStyle()));
  }
}

