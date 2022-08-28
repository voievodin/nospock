package cases;

import java.util.concurrent.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

class MockInitializationSpec {

  private X x1 = Mockito.mock(X.class);
  private X x2 = Mockito.mock(X.class);
  private X x3 = Mockito.mock(X.class);
  private X x4;
  private X x5;
  private X x6;
  private X x7;
  private Flow.Subscriber<X> subscriber1 = Mockito.mock(Flow.Subscriber.class);
  private Flow.Subscriber subscriber2;
  private Flow.Subscriber subscriber3;
  private Flow.Subscriber subscriber4;
  private X x8;
  private X x9;

  @BeforeEach
  void setup() {
    x4 = Mockito.mock(X.class);
    x5 = Mockito.mock(X.class);
    x6 = Mockito.mock(X.class);
    new Y(x7 = Mockito.mock(X.class));
    subscriber2 = Mockito.mock(Flow.Subscriber.class);
    subscriber3 = Mockito.mock(Flow.Subscriber.class);
    subscriber4 = Mockito.mock(Flow.Subscriber.class);
    x8 = Mockito.mock(X.class, (invocation) -> {
      // TODO: cannot initialize mock with preset answer (not yet supported)
      // calculate() >> 123
      return null;
    });
    x9 = // TODO: cannot initialize spy with preset answer, do it yourself: Mockito.spy(X.class);
  }

  public interface X {

    int calculate();
  }

  public static class Y {

    public Y(X x1) {
    }
  }
}
