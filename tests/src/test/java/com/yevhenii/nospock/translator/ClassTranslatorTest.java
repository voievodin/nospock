package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Ast;
import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.CodeStyle;
import com.yevhenii.nospock.jast.JClass;

import org.codehaus.groovy.ast.ClassNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassTranslatorTest {

  private final Translator<ClassNode, JClass> translator = Defaults.translators().translator(TKey.CLASS);

  @AfterEach
  public void cleanup() {
    TransformationsQueue.instance().reset();
  }

  @Test
  public void modifiersCorrectlyTranslated() {
    String source = "private static final class X {}";

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals("private static final class X {\n}\n", jClass.asCode(new CodeStyle()));
  }

  @Test
  public void superclassCorrectlyTranslated() {
    String source = "class X extends Y<String> {}\n class Y<E> {}";

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals("public class X extends Y<String> {\n}\n", jClass.asCode(new CodeStyle()));
  }

  @Test
  public void superclassCorrectlyTranslatedWhenGenericsUsed() {
    String source = "class X<T1, T2> extends Y<String, T1, T2> {}\n class Y<A, B, C> { }";

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals("public class X<T1, T2> extends Y<String, T1, T2> {\n}\n", jClass.asCode(new CodeStyle()));
  }

  @Test
  public void interfacesCorrectlyTranslated() {
    String source = "class X implements Comparable<X>, Map<String, String>, Y {}\n interface Y {}";

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals("public class X implements Comparable<X>, Map<String, String>, Y {\n}\n", jClass.asCode(new CodeStyle()));
  }

  @Test
  public void annotationsAreProperlyTranslated() {
    String source =
      "@A\n@B(b = \"bbb\")\n@C(123)\nclass X {}\n@interface A {}\n@interface B { String b(); }\n@interface C { int value(); }";

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals("@A\n@B(b = \"bbb\")\n@C(123)\npublic class X {\n}\n", jClass.asCode(new CodeStyle()));
  }

  @Test
  public void fieldsAreProperlyTranslated() {
    String source = """
      class X {
        public static final String X = "X";
        public String f1;
        public final int a = 1;
        private int b = 2;
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public static final String X = "X";
          
          public String f1;
          public final int a = 1;
          private int b = 2;
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void fieldCanBeInitialisedUsingMethodCall() {
    String source = """
      class X {
        public String f1 = call(123)
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public String f1 = call(123);
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void fieldCanBeInitialisedReferringOtherField() {
    String source = """
      class X {
        public String f1 = "x1";
        public String f2 = f1;
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public String f1 = "x1";
          public String f2 = f1;
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void fieldCanBeInitialisedReferringToFieldOfOtherClass() {
    String source = """
      class X {
        public int f1 = Integer.MAX_VALUE;
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public int f1 = Integer.MAX_VALUE;
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void fieldInitialisedUsingMethodCall() {
    String source = """
      class X {
        public int f1 = this.f2.f3.call();
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public int f1 = this.f2.f3.call();
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void fieldCanBeInitialisedCreatingNewInstance() {
    String source = """
      class X {
        public Y f1 = new Y();
      }
      """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
                
          public Y f1 = new Y();
        }
        """,
      jClass.asCode(new CodeStyle())
    );
  }

  @Test
  public void classWithMostOfTheMembersInIt() {
    String source =
      """
        class X {
          public Y f1 = new Y()
          public final String x = "123"
          
          public void x() {
            if (x.toUpperCase().equals("123")) {
              System.out.println(f1);
            }
            def v = 3
            int k = v
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
              
          public Y f1 = new Y();
          public final String x = "123";
          
          public void x() {
            if (x.toUpperCase().equals("123")) {
              System.out.println(f1);
            }
            var v = 3;
            int k = v;
          }
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void anInterface() {
    String source =
      """
        interface X {
          void a();
          int b(int c);
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public interface X {
             
          void a();
             
          int b(int c);
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void anAbstractClass() {
    String source =
      """
        abstract class X {
          void a() {
          }
          
          abstract int b(int c);
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public abstract class X {
             
          public void a() {
          }
             
          public abstract int b(int c);
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void simpleEnum() {
    String source =
      """
        enum X {
          A, B;
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public enum X {
          A,
          B;
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void classWithConstructor() {
    String source =
      """
        class X {
          final int a
          final int b
              
          X(int a, int b) {
            this.a = a
            this.b = b
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
              
          private final int a;
          private final int b;
              
          public X(int a, int b) {
            this.a = a;
            this.b = b;
          }
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void methodWithDefaultValues() {
    String source =
      """
        class X {
          void x(int a, int b = 2, int c = 3) {
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context().deepen(CPath.Seg.forClass("X")));

    assertEquals(
      """
        public class X {
              
          public void x(int a, int b, int c) {
          }
              
          public void x(int a, int b) {
            x(a, b, 3);
          }
              
          public void x(int a) {
            x(a, 2);
          }
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void innerClasses() {
    String source =
      """
        class X {
          
          interface A {
            void a();
          }
          
          class B implements A {
            @Override
            void a() { }
          }
          
          static class C extends B {
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {

          public interface A {
         \s
            void a();
          }

          public class B implements A {
         \s
            @Override
            public void a() {
            }
          }

          public static class C extends B {
          }
        }     
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void namedParametersConstructorCall() {
    String source =
      """
        class X {
          
          String f1
          String f2
          X object
          
          X createX() {
            return new X(f1: "f1", f2: "f2", object: createEmpty())
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
              
          private String f1;
          private String f2;
          private X object;
              
          public X createX() {
            return new X() {
              {
                setF1("f1");
                setF2("f2");
                setObject(createEmpty());
              }
            };
          }
        }   
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void closureUsage() {
    String source =
      """
        class X {
          void x() {
            new Thread({ ->
              call()
            });
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
              
          public void x() {
            new Thread(() -> call());
          }
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }

  @Test
  public void closureWithParametersUsage() {
    String source =
      """
        class X {
          void x() {
            List.of(1, 2).stream().map({ v -> v.toString() }).toList();
          }
        }
        """;

    JClass jClass = translator.translate(Ast.locators(source).class0(), Defaults.context());

    assertEquals(
      """
        public class X {
              
          public void x() {
            List.of(1, 2).stream().map((v) -> v.toString()).toList();
          }
        }
        """,
      jClass.asCode(Defaults.CODE_STYLE)
    );
  }
}
