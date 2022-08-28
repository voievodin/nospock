package cases;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import resources.Namespace;

class ArbitrarySpec {

  private Namespace f1 = new Namespace();
  private Namespace.Inner1 f2 = new Namespace.Inner1();
  private Namespace f3;
  private String f4 = Thread.State.NEW.name();
  private Namespace f5 = Mockito.mock(Namespace.class);

  @BeforeEach
  void setup() {
    f3 = new Namespace();
    Mockito.when(f5.compute(Mockito.any())).thenReturn("preset-result");
  }

  @Test
  void derivesTypeFromVariableDeclarationWhenDeclarationIsInnerClass() {
    Namespace.Inner1 inner1 = Mockito.mock(Namespace.Inner1.class);

    inner1.accept(123L);

    Mockito.verify(inner1).accept(123L);
  }

  @Test
  void arrayInitializationIsProperlyHandled() {
    var arr1 = new int[2][2];
    int[][] arr2 = new int[][];
    int[] arr3 = new int[] {1, 2, 3, 4, 5};

    Assertions.assertNotNull(new Object[] {"1", null});
    Assertions.assertNotNull(new Object[2][2]);
  }

  @Test
  void mapInitializationSupport() {
    var empty = Map.of();
    var mapWithValues = Map.of(
      "x",
      123,
      "y",
      321,
      "z",
      333
    );

    Assertions.assertNotNull(empty.isEmpty());
    Assertions.assertEquals(123, mapWithValues.get("x"));
    Assertions.assertEquals(321, mapWithValues.get("y"));
    Assertions.assertEquals(333, mapWithValues.get("z"));
  }

  @Test
  void whenLabelsHaveComments() {
    var x = 1;

    var y = 1;

    var z = x + y;

    var xx = x * x;

    Assertions.assertEquals(2, z);

    Assertions.assertEquals(1, xx);
  }

  @Test
  void mockingWhereAsConstructIsUsed() {
    var x = Mockito.mock(Namespace.class);
    Mockito.when(x.compute(Mockito.any(String.class))).thenReturn("test");

    Assertions.assertEquals("test", x.compute("123"));
    Mockito.verify(x).compute(Mockito.any(String.class));
  }

  @Test
  void constructorWithManyArgumentsGetsProperlyFormatted() {
    Assertions.assertNotNull(new Namespace.Inner2(
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9
    ));
  }

  @Test
  void convertsIntToLongForPropertyAccessThatIsConvertedToSetter() {
    var x = new Namespace.Inner3();

    x.setF1(123L);

    Assertions.assertEquals(123, x.f1);
  }

  @Test
  void ternaryOperatorWorks() {
    Assertions.assertTrue(true ? true : false);
  }

  @Test
  void groovyMethodReferenceWorks() {
    String str = "hello-world";

    Assertions.assertEquals(2, Stream.of("a", "hell", "hello").filter(str::startsWith).count());
  }

  @Test
  void groovyQuestionMarkSupport() {
    var namespace = new Namespace();

    Assertions.assertNull(namespace.inner3.f1);
  }

  @Test
  void constructorWithDefaultParameterIsReplacedWithASetOfSetterCalls() {
    var v1 = new Namespace.Inner4();
    v1.setF1("f1");
    v1.setF2(2);
    v1.setF3(3L);
    var v2 = new Namespace.Inner4();
    v2.setF2(123);

    Assertions.assertEquals("f1", v1.f1);
    Assertions.assertNull(v2.f1);
  }

  @Test
  void noExceptionThrownUsedWithOtherStatementsWorks() {
    var x = 1;

    // noExceptionThrown

    Mockito.verify(f5, Mockito.never()).getStringValue();
  }

  @Test
  void propertyUnderstoodAsMethodCallByTranslatorWhenInteractingWithMocks() {
    var x = Mockito.mock(Namespace.class);
    Mockito.when(x.getStringValue()).thenReturn("str");

    var res = x.getStringValue();

    Assertions.assertEquals("str", res);
    Mockito.verify(x, Mockito.never()).namespace();
  }

  @Test
  void verificationReliesOnVariableDefinedInGivenAnd() {
    var namespace = Mockito.mock(Namespace.class);

    var y = 2;
    Mockito.when(namespace.compute2(y, y)).thenReturn(y);

    namespace.compute2(y, y);

    Mockito.verify(namespace).compute2(y, y);
  }

  @Test
  void elvisOperatorIsSupported() {
    Assertions.assertTrue(true ? true : false);
  }

  @Test
  void getterOnGetterIsCorrectlyResolved() {
    var namespace = new Namespace();

    Assertions.assertNotNull(namespace.getSelf().getSelf());
  }

  @Test
  void constantAdjustedToHaveProperTypeWhenNecessary() {
    Long x = 123L;
    long x2 = 123;
    BigDecimal x3 = new BigDecimal(123);
    BigInteger x4 = new BigInteger(123);
    Double d = 333.0;

    Assertions.assertEquals(123L, x);
    Assertions.assertEquals(123L, x2);
    Assertions.assertEquals(new BigDecimal(123), x3);
    Assertions.assertNotEquals(new BigInteger(124), x4);
    Assertions.assertEquals(333.0, d);
  }

  @Test
  void booleanGetterIsProperlyRecognizedWhenPropertyIsUsed() {
    Assertions.assertTrue(Namespace.isActive());
  }
}
