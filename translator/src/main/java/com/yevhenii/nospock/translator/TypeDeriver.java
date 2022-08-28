package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeDeriver {

  private final TypeLoader typeLoader;

  public TypeDeriver(TypeLoader typeLoader) {
    this.typeLoader = Objects.requireNonNull(typeLoader);
  }

  public JType derive(Set<JType> types, TContext context) {
    if (types.isEmpty()) {
      return JType.object();
    } else if (types.size() == 1) {
      return types.iterator().next();
    } else if (isIntegerFamily(types)) {
      if (types.contains(JType.boxedLong())) {
        return JType.boxedLong();
      } else {
        return JType.boxedInt();
      }
    } else if (isDecimalFamily(types)) {
      return JType.boxedDouble();
    }

    // in order to derive common type we need to know the hierarchy
    // of those types, hence we try to load every such type and run derivation
    // based on what runtime data offers
    List<Class> loadedTypes = types.stream()
      .map(t -> (Class) typeLoader.tryLoad(t.fqn().asString(), context))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    if (loadedTypes.size() == types.size()) {
      return new JType(commonType(loadedTypes));
    }

    return JType.object();
  }

  private static Class<?> commonType(Class<?> a, Class<?> b) {
    final var aTypes = new ArrayList<Class<?>>();
    aTypes.add(a);
    addTypes(a, aTypes);

    for (Class<?> aType : aTypes) {
      if (aType.isAssignableFrom(b)) {
        return aType;
      }
    }

    return Object.class;
  }

  private static Class<?> commonType(List<Class> types) {
    if (types.size() == 1) {
      return types.get(0);
    } else {
      Class<?> common = types.get(0);
      for (int i = 1; i < types.size(); i++) {
        common = commonType(common, types.get(i));
      }
      return common;
    }
  }

  private static void addTypes(Class<?> c, List<Class<?>> allTypes) {
    if (c.getSuperclass() != null && c.getSuperclass() != Object.class) {
      allTypes.add(c.getSuperclass());
    }
    for (Class<?> anInterface : c.getInterfaces()) {
      allTypes.add(anInterface);
    }
    if (c.getSuperclass() != null) {
      addTypes(c.getSuperclass(), allTypes);
    }
    for (Class<?> anInterface : c.getInterfaces()) {
      addTypes(anInterface, allTypes);
    }
  }

  private static boolean isIntegerFamily(Collection<JType> types) {
    for (JType type : types) {
      if (
        !type.isInt()
        && !type.isShort()
        && !type.isLong()
        && !type.isByte()
      ) {
        return false;
      }
    }
    return !types.isEmpty();
  }

  private static boolean isDecimalFamily(Set<JType> types) {
    for (JType type : types) {
      if (!type.isDouble() && !type.isFloat()) {
        return false;
      }
    }
    return !types.isEmpty();
  }
}
