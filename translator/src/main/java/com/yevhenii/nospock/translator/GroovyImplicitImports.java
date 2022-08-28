package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * List imports from default packages that groovy additionally defines except java.lang.
 */
public class GroovyImplicitImports {

  public static final List<String> PACKAGES = List.of(
    "java.lang",
    "java.util",
    "java.io",
    "java.net"
  );

  private static final Map<JType, JImport> TYPES;

  static {
    final var tmp = new HashMap<JType, JImport>();

    // java.util
    register(List.class, tmp);
    register(ArrayList.class, tmp);
    register(LinkedList.class, tmp);
    register(Set.class, tmp);
    register(HashSet.class, tmp);
    register(LinkedHashSet.class, tmp);
    register(TreeSet.class, tmp);
    register(Map.class, tmp);
    register(HashMap.class, tmp);
    register(TreeMap.class, tmp);
    register(LinkedHashMap.class, tmp);
    register(Comparator.class, tmp);
    register(Comparable.class, tmp);
    register(Iterator.class, tmp);
    register(Optional.class, tmp);
    register(Date.class, tmp);
    register(Calendar.class, tmp);
    register(Collection.class, tmp);
    register(Collections.class, tmp);
    register(Arrays.class, tmp);
    register(ArrayDeque.class, tmp);
    register(UUID.class, tmp);
    register(Random.class, tmp);

    // java.io
    register(File.class, tmp);
    register(Closeable.class, tmp);
    register(BufferedInputStream.class, tmp);
    register(BufferedOutputStream.class, tmp);
    register(BufferedReader.class, tmp);
    register(BufferedWriter.class, tmp);
    register(ByteArrayInputStream.class, tmp);
    register(ByteArrayOutputStream.class, tmp);
    register(InputStreamReader.class, tmp);
    register(OutputStreamWriter.class, tmp);
    register(Serializable.class, tmp);
    register(Reader.class, tmp);
    register(Writer.class, tmp);
    register(InputStream.class, tmp);
    register(OutputStream.class, tmp);
    register(IOException.class, tmp);

    // java.net
    register(Socket.class, tmp);
    register(URL.class, tmp);
    register(URI.class, tmp);
    register(URLConnection.class, tmp);
    register(HttpURLConnection.class, tmp);
    register(Inet4Address.class, tmp);
    register(Inet6Address.class, tmp);

    // individual
    register(BigDecimal.class, tmp);
    register(BigInteger.class, tmp);

    TYPES = tmp;
  }

  /**
   * Imports given type if it is registered in the map.
   */
  public static void explicitlyImport(JType type) {
    if (type != null) {
      var jImport = TYPES.getOrDefault(type, TYPES.get(type.withoutGenerics()));
      if (jImport != null) {
        TransformationsQueue.instance().enqueueNewImports(jImport);
      }
    }
  }

  private GroovyImplicitImports() {
  }

  /**
   * For a given class register:
   * fqn -> fqn
   * fqn.segments.last -> fqn
   */
  private static void register(Class<?> c, Map<JType, JImport> map) {
    final var type = new JType(c);
    final var import0 = new JImport(type.fqn().asString(), false);
    map.put(type, import0);
    map.put(new JType(type.fqn().last().asString()), import0);
  }
}
