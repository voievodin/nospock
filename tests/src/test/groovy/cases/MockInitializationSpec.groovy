package cases

import spock.lang.Specification

import java.util.concurrent.Flow

class MockInitializationSpec extends Specification {

  X x1 = Mock()
  def x2 = Mock(X)
  def x3 = Mock(X.class)
  X x4
  X x5
  def x6
  X x7
  Flow.Subscriber<X> subscriber1 = Mock(Flow.Subscriber.class)
  Flow.Subscriber subscriber2
  Flow.Subscriber subscriber3
  def subscriber4
  def x8
  def x9

  def setup() {
    x4 = Mock(X)
    x5 = Mock()
    x6 = Mock(X)
    new Y(x7 = Mock())
    subscriber2 = Mock()
    subscriber3 = Mock(Flow.Subscriber)
    subscriber4 = Mock(Flow.Subscriber)
    x8 = Mock(X.class) {
      calculate() >> 123
    }
    x9 = Spy(X.class) {
      calculate() >> 123
    }
  }

  interface X {
    int calculate();
  }

  static class Y {
    Y(X x1) {}
  }
}
