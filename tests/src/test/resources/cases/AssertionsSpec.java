package cases;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AssertionsSpec {

  @Test
  void simpleAssertions() {
    var v = 3;
    v++;
    String v2 = null;
    String v3 = "hello";
    boolean b1 = true;
    var b2 = Boolean.FALSE;

    v = v + 2;

    Assertions.assertEquals(6, v);
    Assertions.assertEquals(2 - 1, 1);
    Assertions.assertTrue((1 == 1) && (2 == 2));
    Assertions.assertTrue(Boolean.TRUE);
    Assertions.assertNotNull(BigDecimal.TEN);
    Assertions.assertTrue(getTrue());
    Assertions.assertNotNull(newObject());
    voidCall();
    Assertions.assertTrue(LocalDateTime.now().isBefore(LocalDateTime.now().plusDays(1)));
    Assertions.assertTrue(!(1 == 2));
    Assertions.assertNotEquals(2, 1);
    Assertions.assertTrue(1 < 2);
    Assertions.assertTrue(2 > 1);
    Assertions.assertTrue(1 <= 2);
    Assertions.assertTrue(2 >= 1);
    Assertions.assertFalse(new Object() instanceof LocalDateTime);
    Assertions.assertNotNull(new Object[] {1, 2, 3});
    Assertions.assertTrue(BigDecimal.TEN instanceof Object);
    Assertions.assertNull(v2);
    Assertions.assertNotNull(v3);
    Assertions.assertTrue(b1);
    Assertions.assertFalse(b2);
    Assertions.assertNull(v2);
  }

  private static boolean getTrue() {
    return true;
  }

  private static Object newObject() {
    return new Object();
  }

  private static void voidCall() {
  }
}
