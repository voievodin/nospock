package cases

import spock.lang.Specification

class MocksInteractionsSpec extends Specification {

  Dependency dependency = Mock()
  Service service = new Service(dependency)

  def "mocks and verifies"() {
    given:
      def parameter = 3
    when:
      def result = service.compute(parameter)
    then:
      1 * dependency.request(parameter) >> "response-" + parameter
      result == "computed-response-3"
  }

  def "mocks and verifies times"() {
    given:
      def parameter = 3
      3 * dependency.request(parameter) >> "response-" + parameter
    when:
      def result = service.compute(parameter)
      service.compute(parameter)
      service.compute(parameter)
    then:
      result == "computed-response-3"
  }

  def "verifies 0 interactions"() {
    given:
      def parameter = 3
      0 * dependency.request(parameter)
    when:
      def result = service.fallback(parameter)
    then:
      result == "fallback-3"
  }

  def "verifies any"() {
    given:
      def parameter = 3
    when:
      def result = service.compute(parameter)
    then:
      1 * dependency.request(_) >> "response-" + parameter
      result == "computed-response-3"
  }

  def "verifies any wildcard"() {
    given:
      def parameter = 3
    when:
      def result = service.compute2(parameter, parameter)
    then:
      1 * dependency.request2(*_) >> "response-" + parameter + "-" + parameter
      result == "computed-response-3-3"
  }

  def "mocks and verifies closure"() {
    when:
      dependency.request2(1, 2)
    then:
      1 * dependency.request2(*_) >> { args ->
        assert args[0] == 1
        assert args[1] == 2
      }
  }

  def "mocking optional result"() {
    given:
      Dependency d = Mock(Dependency.class)
    when:
      def r = d.getString()
    then:
      1 * d.getString() >> Optional.of("result")
      r.get() == "result"
  }

  def "passing integer constant to method accepting Long"() {
    given:
      Dependency d = Mock(Dependency.class)
    when:
      d.request3(123)
    then:
      1 * d.request3(123)
  }

  def "fields accessors replaced with getters and setters"() {
    given:
      ClassWithFields p = new ClassWithFields();
    when:
      p.a = 123
      p.b = "321"
      p.c = 33333
    then:
      p.a == 123
      p.b == "321"
      p.c == 33333
  }

  def "spy is correctly translated"() {
    given:
      def x = Spy(Service)
      x.fallback2() >> "xxx"
    expect:
      x.fallback(1) == "fallback-1"
      x.fallback2() == "xxx"
  }

  def "override mock results in next when"() {
    given:
      def dependency = Mock(Dependency)
    when:
      def r1 = dependency.request(1)
    then:
      1 * dependency.request(1) >> "d1"
      r1 == "d1"
    when:
      def r2 = dependency.request(2)
    then:
      1 * dependency.request(2) >> "d2"
      r2 == "d2"
  }

  def "explicit single argument used for invocation"() {
    given:
      Dependency x = Mock(Dependency)
    when:
      x.request(1)
    then:
      1 * x.request(*_) >> { int arg1 ->
        assert arg1 == 1
      }
  }

  def "explicit arguments used for invocation"() {
    given:
      Dependency x = Mock(Dependency)
    when:
      x.request2(1, 2)
    then:
      1 * x.request2(*_) >> { int a, int b ->
        assert a == 1
        assert b == 2
      }
  }

  def "arguments array used as invocation"() {
    given:
      Dependency x = Mock(Dependency)
    when:
      def r = x.request2(1, 2)
    then:
      1 * x.request2(*_) >> { args ->
        assert args[0] == 1
        assert args[1] == 2
        return "result"
      }
    and:
      r == "result"
  }

  def "argument parameter type is detected and getters used for assertions"() {
    given:
      Dependency d = Mock(Dependency)
    when:
      d.request4(new ClassWithFields())
    then:
      1 * d.request4(_) >> { ClassWithFields c ->
        assert c.a == 0
        assert c.b == null
        assert c.c == null
      }
  }

  def "expression can be used as number of verification"() {
    given:
      int n = 2
      Dependency d = Mock(Dependency)
    when:
      d.request(1)
      d.request(2)
    then:
      n * d.request(_)
  }

  def "underscore verification is ignored, though mocked"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request(1)
      d.request3(2)
    then:
      _ * d.request(_) >> 2
      _ * d.request3(_)
  }

  def "duplicate then works"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request(1)
      d.request(2)
    then:
      1 * d.request(1)
    then:
      d.request(2) >> 2
  }

  def "matches argument using closure matcher"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request4(new ClassWithFields(a: 1))
      d.request4(new ClassWithFields(b: "str"))
    then:
      1 * d.request4({ it.a == 1 })
      1 * d.request4({ c -> c.b == "str" })
  }

  def "matches argument using closure matcher for multiple parameters"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request2(1, 2)
    then:
      1 * d.request2({ it == 1 }, { it == 2 })
  }

  def "matches argument using closure matcher and also matches invocation"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request(1)
    then:
      1 * d.request({ it == 1 }) >> { args ->
        assert args[0] == 1
      }
  }
  
  def "if throw statement is last in block then return statement isn't generated"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request4(new ClassWithFields())
    then:
      1 * d.request4(_) >> {
        throw new RuntimeException("oops")
      }
    and:
      thrown(RuntimeException)
  }

  def "extract variable if parameterised constructor used in then return"() {
    given:
      def d = Mock(Dependency)
      d.cwf() >> new ClassWithFields(a: 123)
    expect:
      d.cwf().a == 123
  }
  
  def "lambda answer with parameter cast correctly translated"() {
    given:
      def d = Mock(Dependency)
    when:
      d.request4(new ClassWithFields(a: 1))
    then:
      1 * d.request4({ ClassWithFields c -> c.a == 1 } as ClassWithFields)
  }

  def "arg type is always used as lambda parameter with cast expression"() {
    given:
      def d = Mock(Dependency)
    when:
      d.sendMessage(new ClassWithFields(a: 1))
    then:
      1 * d.sendMessage({ ClassWithFields cwf -> cwf.a == 1 })
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
  final Dependency dependency

  Service(Dependency dependency) {
    this.dependency = dependency
  }

  String compute(int parameter) {
    return "computed-" + dependency.request(parameter)
  }

  String compute2(int p1, int p2) {
    return "computed-" + dependency.request2(p1, p2)
  }

  String fallback(int parameter) {
    return "fallback-" + parameter
  }

  String fallback2() {
    return "fallback2"
  }
}


class ClassWithFields {

  int a;
  String b;
  Long c;

  void setA(int a) {
    this.a = a;
  }

  void setB(String b) {
    this.b = b;
  }

  void setC(Long c) {
    this.c = c;
  }

  int getA() {
    return a;
  }

  String getB() {
    return b;
  }

  Long getC() {
    return c;
  }
}
