package cases

import resources.Namespace
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Stream

class ArbitrarySpec extends Specification {

  @Shared
  def f1 = new Namespace()
  def f2 = new Namespace.Inner1()
  def f3
  def f4 = Thread.State.NEW.name()
  def f5 = Mock(Namespace)

  void setup() {
    f3 = new Namespace()
    f5.compute(*_) >> "preset-result"
  }

  def "derives type from variable declaration, when declaration is inner class"() {
    given:
      Namespace.Inner1 inner1 = Mock()
    when:
      inner1.accept(123)
    then:
      1 * inner1.accept(123)
  }

  def "array initialization is properly handled"() {
    given:
      def arr1 = new int[2][2]
      int[][] arr2 = new int[][]{}
      int[] arr3 = new int[]{1, 2, 3, 4, 5}
    expect:
      new Object[]{"1", null}
      new Object[2][2]
  }

  def "map initialization support"() {
    when:
      def empty = [:]
      def mapWithValues = [
        x  : 123,
        y  : 321,
        "z": 333
      ]
    then:
      empty.isEmpty()
      mapWithValues.get("x") == 123
      mapWithValues.get("y") == 321
      mapWithValues.get("z") == 333
  }

  def "when labels have comments"() {
    given: "comment1"
      def x = 1
    and: "comment2"
      def y = 1
    when: "comment3"
      def z = x + y
    and: "comment4"
      def xx = x * x
    then: "comment5"
      z == 2
    and: "comment6"
      xx == 1
  }

  def "mocking where as construct is used"() {
    given:
      def x = Mock(Namespace)
      1 * x.compute(_ as String) >> "test"
    expect:
      x.compute("123") == "test"
  }

  def "constructor with many arguments gets properly formatted"() {
    expect:
      new Namespace.Inner2(1, 2, 3, 4, 5, 6, 7, 8, 9) != null
  }

  def "converts int to long for property access that is converted to setter"() {
    given:
      def x = new Namespace.Inner3()
    when:
      x.f1 = 123
    then:
      x.f1 == 123
  }

  def "ternary operator works"() {
    expect:
      true ? true : false
  }

  def "groovy method reference works"() {
    given:
      String str = "hello-world"
    expect:
      Stream.of("a", "hell", "hello")
        .filter(str.&startsWith)
        .count() == 2
  }

  def "groovy question mark support"() {
    given:
      def namespace = new Namespace()
    expect:
      namespace?.inner3?.f1 == null
  }

  def "constructor with default parameter is replaced with a set of setter calls"() {
    given:
      def v1 = new Namespace.Inner4(f1: "f1", f2: 2, f3: 3L)
      def v2 = new Namespace.Inner4(f2: 123)
    expect:
      v1.f1 == "f1"
      v2.f1 == null
  }

  def "noExceptionThrown used with other statements works"() {
    when:
      def x = 1
    then:
      noExceptionThrown()
    and:
      0 * f5.getStringValue()
  }

  def "property understood as method call by translator when interacting with mocks"() {
    given:
      def x = Mock(Namespace)
      x.stringValue >> "str"
    when:
      def res = x.getStringValue()
    then:
      res == "str"
      0 * x.namespace
  }

  def "verification relies on variable defined in given and"() {
    given:
      def namespace = Mock(Namespace)
    and:
      def y = 2
    when:
      namespace.compute2(y, y)
    then:
      1 * namespace.compute2(y, y) >> y
  }

  def "elvis operator is supported"() {
    expect:
      true ?: false
  }

  def "getter on getter is correctly resolved"() {
    given:
      def namespace = new Namespace()
    expect:
      namespace.self.self != null
  }

  def "constant adjusted to have proper type when necessary"() {
    given:
      Long x = 123
      long x2 = 123
      BigDecimal x3 = 123
      BigInteger x4 = 123
      Double d = 333.0
    expect:
      x == 123L
      x2 == 123L
      x3 == 123
      x4 != 124
      d == 333.0
  }

  def "boolean getter is properly recognized when property is used"() {
    expect:
      Namespace.active
  }
}
