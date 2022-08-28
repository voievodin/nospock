package com.yevhenii.nospock.translator;

import java.util.concurrent.atomic.AtomicInteger;

public class IdSequence {
  private static final AtomicInteger SEQ = new AtomicInteger(0);

  public static int next() {
    return SEQ.incrementAndGet();
  }
}
