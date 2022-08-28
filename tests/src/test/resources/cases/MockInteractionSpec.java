package cases;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MocksInteractionsSpec {

  private Dependency dependency = Mockito.mock(Dependency.class);
  private Service service = new Service(dependency);

  @Test
  void mocksAndVerifies() {
    var parameter = 3;
    Mockito.when(dependency.request(parameter)).thenReturn("response-" + parameter);

    var result = service.compute(parameter);

    Assertions.assertEquals("computed-response-3", result);
    Mockito.verify(dependency).request(parameter);
  }

  @Test
  void mocksAndVerifiesTimes() {
    var parameter = 3;
    Mockito.when(dependency.request(parameter)).thenReturn("response-" + parameter);

    var result = service.compute(parameter);
    service.compute(parameter);
    service.compute(parameter);

    Assertions.assertEquals("computed-response-3", result);
    Mockito.verify(dependency, Mockito.times(3)).request(parameter);
  }

  @Test
  void verifies0Interactions() {
    var parameter = 3;

    var result = service.fallback(parameter);

    Assertions.assertEquals("fallback-3", result);
    Mockito.verify(dependency, Mockito.never()).request(parameter);
  }

  @Test
  void verifiesAny() {
    var parameter = 3;
    Mockito.when(dependency.request(Mockito.anyInt())).thenReturn("response-" + parameter);

    var result = service.compute(parameter);

    Assertions.assertEquals("computed-response-3", result);
    Mockito.verify(dependency).request(Mockito.anyInt());
  }

  @Test
  void verifiesAnyWildcard() {
    var parameter = 3;
    Mockito.when(dependency.request2(Mockito.anyInt(), Mockito.any())).thenReturn("response-" + parameter + "-" + parameter);

    var result = service.compute2(parameter, parameter);

    Assertions.assertEquals("computed-response-3-3", result);
    Mockito.verify(dependency).request2(Mockito.anyInt(), Mockito.any());
  }

  @Test
  void mocksAndVerifiesClosure() {
    Mockito.doAnswer((invocation) -> {
      var args = invocation.getArguments();
      Assertions.assertEquals(1, args[0]);
      Assertions.assertEquals(2, args[1]);
      return null;
    }).when(dependency).request2(Mockito.anyInt(), Mockito.any());

    dependency.request2(1, 2);

    Mockito.verify(dependency).request2(Mockito.anyInt(), Mockito.any());
  }

  @Test
  void mockingOptionalResult() {
    Dependency d = Mockito.mock(Dependency.class);
    Mockito.when(d.getString()).thenReturn(Optional.of("result"));

    var r = d.getString();

    Assertions.assertEquals("result", r.get());
    Mockito.verify(d).getString();
  }

  @Test
  void passingIntegerConstantToMethodAcceptingLong() {
    Dependency d = Mockito.mock(Dependency.class);

    d.request3(123L);

    Mockito.verify(d).request3(123L);
  }

  @Test
  void fieldsAccessorsReplacedWithGettersAndSetters() {
    ClassWithFields p = new ClassWithFields();

    p.setA(123);
    p.setB("321");
    p.setC(33333L);

    Assertions.assertEquals(123, p.getA());
    Assertions.assertEquals("321", p.getB());
    Assertions.assertEquals(33333, p.getC());
  }

  @Test
  void spyIsCorrectlyTranslated() {
    var x = Mockito.spy(Service.class);
    Mockito.when(x.fallback2()).thenReturn("xxx");

    Assertions.assertEquals("fallback-1", x.fallback(1));
    Assertions.assertEquals("xxx", x.fallback2());
  }

  @Test
  void overrideMockResultsInNextWhen() {
    var dependency = Mockito.mock(Dependency.class);
    Mockito.when(dependency.request(1)).thenReturn("d1");

    var r1 = dependency.request(1);

    Assertions.assertEquals("d1", r1);
    Mockito.verify(dependency).request(1);

    Mockito.when(dependency.request(2)).thenReturn("d2");

    var r2 = dependency.request(2);

    Assertions.assertEquals("d2", r2);
    Mockito.verify(dependency).request(2);
  }

  @Test
  void explicitSingleArgumentUsedForInvocation() {
    Dependency x = Mockito.mock(Dependency.class);

    x.request(1);

    Mockito.verify(x).request(Mockito.argThat((arg1) -> {
      Assertions.assertEquals(1, arg1);
      return true;
    }));
  }

  @Test
  void explicitArgumentsUsedForInvocation() {
    Dependency x = Mockito.mock(Dependency.class);
    Mockito.doAnswer((invocation) -> {
      int a = ((int) invocation.getArguments()[0]);
      int b = ((int) invocation.getArguments()[1]);
      Assertions.assertEquals(1, a);
      Assertions.assertEquals(2, b);
      return null;
    }).when(x).request2(Mockito.anyInt(), Mockito.any());

    x.request2(1, 2);

    Mockito.verify(x).request2(Mockito.anyInt(), Mockito.any());
  }

  @Test
  void argumentsArrayUsedAsInvocation() {
    Dependency x = Mockito.mock(Dependency.class);
    Mockito.doAnswer((invocation) -> {
      var args = invocation.getArguments();
      Assertions.assertEquals(1, args[0]);
      Assertions.assertEquals(2, args[1]);
      return "result";
    }).when(x).request2(Mockito.anyInt(), Mockito.any());

    var r = x.request2(1, 2);

    Mockito.verify(x).request2(Mockito.anyInt(), Mockito.any());

    Assertions.assertEquals("result", r);
  }

  @Test
  void argumentParameterTypeIsDetectedAndGettersUsedForAssertions() {
    Dependency d = Mockito.mock(Dependency.class);

    d.request4(new ClassWithFields());

    Mockito.verify(d).request4(Mockito.argThat((c) -> {
      Assertions.assertEquals(0, c.getA());
      Assertions.assertNull(c.getB());
      Assertions.assertNull(c.getC());
      return true;
    }));
  }

  @Test
  void expressionCanBeUsedAsNumberOfVerification() {
    int n = 2;
    Dependency d = Mockito.mock(Dependency.class);

    d.request(1);
    d.request(2);

    Mockito.verify(d, Mockito.times(n)).request(Mockito.anyInt());
  }

  @Test
  void underscoreVerificationIsIgnoredThoughMocked() {
    var d = Mockito.mock(Dependency.class);
    Mockito.when(d.request(Mockito.anyInt())).thenReturn(2);

    d.request(1);
    d.request3(2L);

    Mockito.verify(d, Mockito.atLeast(0)).request(Mockito.anyInt());
    Mockito.verify(d, Mockito.atLeast(0)).request3(Mockito.any());
  }

  @Test
  void duplicateThenWorks() {
    var d = Mockito.mock(Dependency.class);
    Mockito.when(d.request(2)).thenReturn(2);

    d.request(1);
    d.request(2);

    Mockito.verify(d).request(1);
  }

  @Test
  void matchesArgumentUsingClosureMatcher() {
    var d = Mockito.mock(Dependency.class);

    var classWithFields1 = new ClassWithFields();
    classWithFields1.setA(1);
    d.request4(classWithFields1);
    var classWithFields2 = new ClassWithFields();
    classWithFields2.setB("str");
    d.request4(classWithFields2);

    Mockito.verify(d).request4(Mockito.argThat((it) -> it.getA() == 1));
    Mockito.verify(d).request4(Mockito.argThat((c) -> c.getB() == "str"));
  }

  @Test
  void matchesArgumentUsingClosureMatcherForMultipleParameters() {
    var d = Mockito.mock(Dependency.class);

    d.request2(1, 2);

    Mockito.verify(d).request2(Mockito.argThat((it) -> it == 1), Mockito.argThat((it) -> it == 2));
  }

  @Test
  void matchesArgumentUsingClosureMatcherAndAlsoMatchesInvocation() {
    var d = Mockito.mock(Dependency.class);
    Mockito.doAnswer((invocation) -> {
      var args = invocation.getArguments();
      Assertions.assertEquals(1, args[0]);
      return null;
    }).when(d).request(Mockito.argThat((it) -> it == 1));

    d.request(1);

    Mockito.verify(d).request(Mockito.argThat((it) -> it == 1));
  }

  @Test
  void ifThrowStatementIsLastInBlockThenReturnStatementIsntGenerated() {
    var d = Mockito.mock(Dependency.class);
    Mockito.doAnswer((invocation) -> {
      var it = invocation.getArguments();
      throw new RuntimeException("oops");
    }).when(d).request4(Mockito.any());

    Mockito.verify(d).request4(Mockito.any());

    Assertions.assertThrows(RuntimeException.class, () -> {
      d.request4(new ClassWithFields());
    });
  }

  @Test
  void extractVariableIfParameterisedConstructorUsedInThenReturn() {
    var d = Mockito.mock(Dependency.class);
    var classWithFields1 = new ClassWithFields();
    classWithFields1.setA(123);
    Mockito.when(d.cwf()).thenReturn(classWithFields1);

    Assertions.assertEquals(123, d.cwf().getA());
  }

  @Test
  void lambdaAnswerWithParameterCastCorrectlyTranslated() {
    var d = Mockito.mock(Dependency.class);

    var classWithFields1 = new ClassWithFields();
    classWithFields1.setA(1);
    d.request4(classWithFields1);

    Mockito.verify(d).request4(Mockito.argThat((c) -> c.getA() == 1));
  }

  @Test
  void argTypeIsAlwaysUsedAsLambdaParameterWithCastExpression() {
    var d = Mockito.mock(Dependency.class);

    var classWithFields1 = new ClassWithFields();
    classWithFields1.setA(1);
    d.sendMessage(classWithFields1);

    Mockito.verify(d).sendMessage(Mockito.argThat((cwf) -> cwf.getA() == 1));
  }
}

interface Dependency {

  String request(int parameter);

  String request2(int parameter1, Integer parameter2);

  Optional<String> getString();

  void request3(Long l);

  void request4(ClassWithFields c);

  ClassWithFields cwf();

  void sendMessage(Object message);
}

class Service {

  private final Dependency dependency;

  public Service(Dependency dependency) {
    this.dependency = dependency;
  }

  public String compute(int parameter) {
    return "computed-" + dependency.request(parameter);
  }

  public String compute2(int p1, int p2) {
    return "computed-" + dependency.request2(p1, p2);
  }

  public String fallback(int parameter) {
    return "fallback-" + parameter;
  }

  public String fallback2() {
    return "fallback2";
  }
}

class ClassWithFields {

  private int a;
  private String b;
  private Long c;

  public void setA(int a) {
    this.a = a;
  }

  public void setB(String b) {
    this.b = b;
  }

  public void setC(Long c) {
    this.c = c;
  }

  public int getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  public Long getC() {
    return c;
  }
}
