package cases

import spock.lang.Specification

class LifecycleSpec extends Specification {

  static final List<String> called = new ArrayList<>()

  void setup() {
    called.add("setup")
  }

  void setupSpec() {
    called.add("setupSpec")
  }

  void cleanup() {
    called.add("cleanup")
  }

  void cleanupSpec() {
    if (!called.equals(["setupSpec", "setup", "test", "cleanup"])) {
      throw new AssertionError("Wrong order" + called)
    }
  }

  def "test"() {
    when:
      called.add("test")
    then:
      1 == 1
  }
}
