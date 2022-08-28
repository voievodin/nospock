package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.Defaults;
import com.yevhenii.nospock.jast.JType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeDeriverTest {

  private static final TypeLoader TL = new TypeLoader(TypeDeriverTest.class.getClassLoader());

  @AfterEach
  public void cleanup() {
    TransformationsQueue.instance().reset();
  }

  @Test
  public void derivesCommonType() {
    assertEquals(
      new JType(CharSequence.class),
      new TypeDeriver(TL).derive(
        Set.of(
          new JType(CharSequence.class),
          new JType(String.class)
        ),
        Defaults.context()
      )
    );
  }

  @Test
  public void derivesNumberAsCommonTypeForLongIntegerAndFloat() {
    assertEquals(
      new JType(Number.class),
      new TypeDeriver(TL).derive(
        Set.of(
          new JType(Integer.class),
          new JType(Long.class),
          new JType(Float.class)
        ),
        Defaults.context()
      )
    );
  }
}
