package cases

import spock.lang.Specification

import java.time.LocalDateTime

class AssertionsSpec extends Specification {

  def "simple assertions"() {
    given:
      def v = 3
      v++
      String v2 = null
      String v3 = "hello"
      boolean b1 = true
      def b2 = Boolean.FALSE
    when:
      v = v + 2
    then:
      v == 6
      1 == 2 - 1
      1 == 1 && 2 == 2
      Boolean.TRUE
      BigDecimal.TEN
      getTrue()
      newObject()
      voidCall()
      LocalDateTime.now().isBefore(LocalDateTime.now().plusDays(1))
      !(1 == 2)
      1 != 2
      1 < 2
      2 > 1
      1 <= 2
      2 >= 1
      !(new Object() instanceof LocalDateTime)
      new Object[]{1, 2, 3}
      BigDecimal.TEN instanceof Object
      v2 == null
      v3 != null
      b1
      !b2
      !v2
  }

  private static boolean getTrue() {
    return true
  }

  private static Object newObject() {
    return new Object()
  }

  private static void voidCall() {
  }
}
