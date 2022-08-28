package com.yevhenii.nospock.translator.spock;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SpockLabel {

  GIVEN,
  WHEN,
  THEN,
  AND,
  EXPECT,
  SETUP,
  CLEANUP,
  WHERE;

  private static final Map<String, SpockLabel> map = Arrays.stream(values())
    .collect(
      Collectors.toMap(
        SpockLabel::name,
        Function.identity()
      )
    );

  public static boolean isKnown(String label) {
    return map.containsKey(label.toUpperCase());
  }
}
