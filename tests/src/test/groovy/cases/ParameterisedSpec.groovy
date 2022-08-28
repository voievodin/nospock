package cases

import resources.Namespace
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.stream.Collectors
import java.util.stream.Stream

import static java.lang.Thread.State
import static java.lang.Thread.State.RUNNABLE
import static java.lang.Thread.State.TERMINATED
import static java.math.BigInteger.ONE
import static java.math.BigInteger.TWO

class ParameterisedSpec extends Specification {

  @Unroll
  def "#a + #b + #c = #result"() {
    when:
      def actualResult = a + b + c
    then:
      actualResult == result
    where:
      a | b | c | result
      1 | 1 | 1 | 3
      2 | 2 | 3 | 7
  }

  def "#x1 & #x2 types derived correctly"() {
    expect:
      1 == 1
    where:
      x1                 | x2
      new Namespace()    | Namespace.create()
      Namespace.create() | new Namespace()
      Namespace.DEFAULT  | Namespace.DEFAULT
  }

  def "#x1 & x2 types derived correctly from usage"() {
    expect:
      useX(x1)
      useX(x2)
      1 == 1
    where:
      x1                                                    | x2
      ((Callable<Namespace>) { -> new Namespace() }).call() | ((Callable<Namespace>) { -> new Namespace() }).call()
  }

  @Unroll
  def "#x1 + #x2 == #res is a positive integer"() {
    expect:
      x1 + x2 == res
    where:
      x1 << [1, 2, 3]
      x2 << [1, 1, 1]
      res << [2, 3, 4]
  }

  def "statically imported parameter values get correct types"() {
    expect:
      1 == 1
    where:
      x1         | x2  | x3            | x4
      RUNNABLE   | ONE | State.NEW     | Thread.State.NEW.name()
      TERMINATED | TWO | State.WAITING | Thread.State.WAITING.name()
  }

  def "empty where values section is ignored"() {
    expect:
      a + b == c
    where:
      a | b || c
      1 | 2 || 3
      2 | 3 || 5
  }

  def "null is correctly passed when using single element parameterization"() {
    expect:
      x == x
    where:
      x << [null, 1, 2, 3, 4]
  }

  def "collection can  be supplier of where expression values"() {
    expect:
      x == x
    where:
      x << Set.of(1, 2, 3, 4, 5)
  }

  def "array can be supplier of where expression values"() {
    expect:
      x.startsWith("a")
    where:
      x << new String[]{"aa", "ab", "ac"}
  }

  def "predefined list and values providing collection sources can be used to provide values"() {
    expect:
      x + y == 4
    where:
      x << [1, 2, 3]
      y << Stream.of(3, 2, 1).collect(Collectors.toList())
  }

  def "two collections can be used to provide values"() {
    expect:
      x + y == 4
    where:
      x << List.of(1, 2, 3)
      y << Stream.of(3, 2, 1).collect(Collectors.toList())
  }

  def "where can have values directly assigned used"() {
    expect:
      x + y == z
    where:
      x = 1
      y = "2"
      z = "12"
  }

  def "multiple columns can follow empty section"() {
    expect:
      a + b == r1
      a * b == r2
      a - b == r3
    where:
      a | b || r1 | r2 | r3
      1 | 2 || 3  | 2  | -1
      2 | 3 || 5  | 6  | -1
  }

  def "all columns can be split using double pipe"() {
    expect:
      a + b == r
    where:
      a || b || r
      1 || 2 || 3
      2 || 3 || 5
  }
  
  String useX(Namespace x) {
    return "used x" + x
  }
}
