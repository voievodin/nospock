package cases;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import resources.Namespace;

class ThrownSpec {

  @Test
  void assertsExceptionIsThrown() {
    Assertions.assertThrows(Exception.class, () -> {
      throwNew();
    });
  }

  @Test
  void assertsExceptionIsThrownCheckingInstance() {
    var x = Assertions.assertThrows(Exception.class, () -> {
      throwNew();
    });
    Assertions.assertEquals("x", x.getMessage());
  }

  @Test
  void multipleThrownBlocks() {
    Assertions.assertThrows(Exception.class, () -> {
      throwNew();
    });

    Assertions.assertThrows(Exception.class, () -> {
      throwNew();
    });
  }

  @Test
  void innerClassExceptionThrown() {
    Assertions.assertThrows(Namespace.InnerException.class, () -> {
      throwException(new Namespace.InnerException());
    });
  }

  @Test
  void noExceptionIsThrownIsProperlyHandled() {
    doNothing();

    // noExceptionThrown
  }

  @Test
  void thrownAssertionIsBeforeVerification() {
    var x = Mockito.mock(Namespace.class);
    Mockito.doAnswer((invocation) -> {
      var it = invocation.getArguments();
      throw new RuntimeException("oops");
    }).when(x).compute(Mockito.any());

    Assertions.assertThrows(RuntimeException.class, () -> {
      x.compute("str");
    });
    Mockito.verify(x).compute(Mockito.any());
  }

  @Test
  void thrownWrapsAllAndSectionsFollowingWhen() {
    Assertions.assertEquals(1, 1);

    Assertions.assertThrows(RuntimeException.class, () -> {
      new RuntimeException();
      throwException(new RuntimeException("a"));
    });
  }

  public void throwNew() throws Exception {
    throw new Exception("x");
  }

  public void throwException(Exception x) {
    throw x;
  }

  public void doNothing() {
  }
}
