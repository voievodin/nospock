package cases

import resources.Namespace
import spock.lang.Specification

class ThrownSpec extends Specification {

  def "asserts exception is thrown"() {
    when:
      throwNew()
    then:
      thrown(Exception)
  }

  def "asserts exception is thrown checking instance"() {
    when:
      throwNew()
    then:
      def x = thrown(Exception)
      x.getMessage() == "x"
  }

  def "multiple thrown blocks"() {
    when:
      throwNew()
    then:
      thrown(Exception)
    when:
      throwNew()
    then:
      thrown(Exception)
  }

  def "inner class exception thrown"() {
    when:
      throwException(new Namespace.InnerException())
    then:
      thrown(Namespace.InnerException)
  }

  def "noExceptionIsThrown is properly handled"() {
    when:
      doNothing()
    then:
      noExceptionThrown()
  }

  def "thrown assertion is before verification"() {
    given:
      def x = Mock(Namespace)
      1 * x.compute(_) >> { throw new RuntimeException("oops") }
    when:
      x.compute("str")
    then:
      thrown(RuntimeException)
  }

  def "thrown wraps all and sections following when"() {
    when:
      new RuntimeException()
    and:
      throwException(new RuntimeException("a"))
    then:
      1 == 1
    and:
      thrown(RuntimeException)
  }

  def throwNew() {
    throw new Exception("x")
  }

  def throwException(Exception x) {
    throw x
  }

  def doNothing() {}
}
