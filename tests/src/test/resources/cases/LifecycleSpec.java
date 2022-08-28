package cases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LifecycleSpec {

  private static final List<String> called = new ArrayList<>();

  @BeforeEach
  void setup() {
    called.add("setup");
  }

  @BeforeAll
  static void setupSpec() {
    called.add("setupSpec");
  }

  @AfterEach
  void cleanup() {
    called.add("cleanup");
  }

  @AfterAll
  static void cleanupSpec() {
    if (!called.equals(Arrays.asList("setupSpec", "setup", "test", "cleanup"))) {
      throw new AssertionError("Wrong order" + called);
    }
  }

  @Test
  void test() {
    called.add("test");

    Assertions.assertEquals(1, 1);
  }
}
