package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.Fqn;
import com.yevhenii.nospock.jast.JImport;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class TypeLoader {

  private static final Logger LOG = Logger.getLogger(TypeLoader.class.getName());
  private static final Map<String, Class<?>> PRELOADED_TYPE_CACHE;

  static {
    final var cache = new HashMap<String, Class<?>>();
    preload(byte.class, cache);
    preload(short.class, cache);
    preload(int.class, cache);
    preload(long.class, cache);
    preload(float.class, cache);
    preload(double.class, cache);
    preload(boolean.class, cache);
    preload(char.class, cache);
    preload(void.class, cache);
    preload(Byte.class, cache);
    preload(Short.class, cache);
    preload(Integer.class, cache);
    preload(Long.class, cache);
    preload(Float.class, cache);
    preload(Double.class, cache);
    preload(Boolean.class, cache);
    preload(Character.class, cache);
    preload(Void.class, cache);
    preload(String.class, cache);
    preload(BigDecimal.class, cache);
    preload(BigInteger.class, cache);
    preload(List.class, cache);
    preload(ArrayList.class, cache);
    preload(Map.class, cache);
    preload(HashMap.class, cache);
    PRELOADED_TYPE_CACHE = Map.copyOf(cache);
  }

  private final ClassLoader classLoader;
  private final ConcurrentMap<ContextCacheKey, Class<?>> contextCache;

  public TypeLoader(ClassLoader classLoader) {
    this.classLoader = Objects.requireNonNull(classLoader);
    contextCache = new ConcurrentHashMap<>();
  }

  public Class<?> tryLoad(Fqn fqn, TContext context) {
    return tryLoad(fqn.asString(), context);
  }

  public Class<?> tryLoad(String cName, TContext context) {
    if ("this".equals(cName)) {
      return null;
    }
    if (PRELOADED_TYPE_CACHE.containsKey(cName)) {
      return PRELOADED_TYPE_CACHE.get(cName);
    }
    final var ccKey = new ContextCacheKey(context.path(), cName);
    final Class<?> cached = contextCache.get(ccKey);
    if (cached != null) {
      return cached;
    }
    final Class<?> loaded = tryLoadUsingDifferentSources(cName, context);
    if (loaded == null) {
      LOG.finest(String.format("Failed to load class for cName: '%s', cpath: '%s'", cName, context.path()));
      return null;
    }
    LOG.finest(String.format("Loaded: '%s' for cName: '%s', cpath: '%s'", loaded, cName, context.path()));
    contextCache.put(ccKey, loaded);
    return loaded;
  }

  // cName = Thread -> java.lang.Thread (default package lookup)
  // cName = Thread.State, imports = [static java.lang.Thread.State] -> java.lang.Thread.State (
  private Class<?> tryLoadUsingDifferentSources(String cName, TContext context) {
    if (cName.contains(".")) {
      final Class<?> c = tryLoadForName(cName);
      if (c != null) {
        return c;
      } else {
        return doTryLoadInnerClassName(cName, context);
      }
    }

    // java.math.BigDecimal, cName = BigDecimal
    for (JImport anImport : context.imports()) {
      if (!anImport.isStatic() && anImport.fqn().last().asString().equals(cName)) {
        return tryLoadForName(anImport.fqn().asString());
      }
    }

    // java.math.BigInteger.ONE, cName = ONE
    // java.lang.Thread.State.RUNNING, cName = RUNNING
    // java.lang.Thread.State, cName = State
    for (JImport anImport : context.imports()) {
      if (!anImport.isStatic()) {
        continue;
      }

      if (!anImport.fqn().last().asString().equals(cName)) {
        continue;
      }

      // Since it's statically imported it's a member of the class
      // that contains it, hence no reason to attempt loading the full fqn. 
      // Though loading inner classes means replacing fqn '.' with '$'.
      // here we attempt loading fqn parts one by one. E.g. for java.lang.Thread.State
      // 1. java.lang.Thread.State
      // 2. java.lang.Thread$State (succeeds)
      // 3. java.lang$Thread$State
      Class<?> c = doTryLoadInnerClass(anImport.fqn().withoutLastOrEmpty(), anImport.fqn().last().asString());
      if (c != null) {
        return c;
      }
    }

    // In case haven't found class by imports the guess is that it might 
    // be the class from the set of imports that groovy implicitly imports
    // e.g. java.lang.String
    for (String defaultGroovyImport : GroovyImplicitImports.PACKAGES) {
      final var c = tryLoadForName(defaultGroovyImport + '.' + cName);
      if (c != null) {
        return c;
      }
    }

    // Assuming the class is in the same package as the most outer class
    // e.g. if outer context class is in 'x.y' package will try to load 'x.y.cName' 
    if (context.package0() != null) {
      final var c = tryLoadForName(context.package0().fqn() + '.' + cName);
      if (c != null) {
        return c;
      }
    }

    // class can be also declared within this class, hence trying to load inner class in this way
    // Note that inner classes in such case can only have capitalized first symbol
    if (context.path().isWithinClass() && Character.isUpperCase(cName.charAt(0))) {
      return tryLoadForName(context.path().containingClass().last().name() + '$' + cName);
    }

    return null;
  }

  private Class<?> doTryLoadInnerClassName(String cName, TContext context) {
    // might be the case that cannot be loaded because it's inner class
    final var cNameFqn = new Fqn(cName);
    final Class<?> innerC = doTryLoadInnerClass(cNameFqn.withoutLastOrEmpty(), cNameFqn.last().asString());
    if (innerC != null) {
      return innerC;
    }

    // might be inner class that needs to be combined with import
    // x.y.z.Outer.Inner for cName=Outer.Inner
    final Fqn first = cNameFqn.first();
    for (JImport anImport : context.imports()) {
      if (anImport.fqn().last().equals(first)) {
        // x.y.z.Outer -> x.y.z.Outer.Inner
        final Fqn innerFqn = anImport.fqn().withoutLastOrEmpty().add(cNameFqn);
        return doTryLoadInnerClass(innerFqn.withoutLastOrEmpty(), innerFqn.last().asString());
      }
    }

    // might be inner class within default package, e.g. Thread.State
    // when used as Thread.State
    final String cInnerName = cName.replaceAll("\\.", "\\$");
    for (String defaultGroovyImport : GroovyImplicitImports.PACKAGES) {
      final var dc = tryLoadForName(defaultGroovyImport + '.' + cInnerName);
      if (dc != null) {
        return dc;
      }
    }

    // might be inner class within this package
    if (context.package0() != null) {
      return tryLoadForName(context.package0().fqn() + '.' + cInnerName);
    }

    return null;
  }

  private Class<?> doTryLoadInnerClass(Fqn fqn, String suffix) {
    if (fqn.isEmpty()) {
      return null;
    }
    // java.lang.Thread$State
    final Class<?> c = tryLoadForName(fqn.asString() + '$' + suffix);
    if (c != null) {
      return c;
    } else {
      return doTryLoadInnerClass(fqn.withoutLastOrEmpty(), fqn.last().asString() + '$' + suffix);
    }
  }

  private Class<?> tryLoadForName(String fqn) {
    try {
      return Class.forName(fqn, true, classLoader);
    } catch (ClassNotFoundException ignored) {
      LOG.finest(String.format("Class '%s' not found", fqn));
    } catch (UnsupportedClassVersionError unsupportedClassVersionError) {
      LOG.warning(
        String.format(
          "Couldn't load class '%s' due to unsupported class version: '%s'",
          fqn,
          unsupportedClassVersionError
        )
      );
    } catch (NoClassDefFoundError def404) {
      // we might try to load some ridiculous names, hence it's fine
      LOG.finest(
        String.format(
          "Couldn't load class '%s' as class definition isn't found: '%s'",
          fqn,
          def404.getMessage()
        )
      );
    }
    return null;
  }

  private static void preload(Class<?> c, Map<String, Class<?>> cache) {
    cache.put(c.getSimpleName(), c);
    cache.put(c.getName(), c);
  }

  private static class ContextCacheKey {

    private final CPath cPath;
    private final String cName;

    private ContextCacheKey(CPath cPath, String cName) {
      this.cPath = Objects.requireNonNull(cPath);
      this.cName = Objects.requireNonNull(cName);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof ContextCacheKey)) {
        return false;
      }
      final ContextCacheKey that = (ContextCacheKey) obj;
      return Objects.equals(cPath, that.cPath)
             && Objects.equals(cName, that.cName);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Objects.hashCode(cPath);
      hash = 31 * hash + Objects.hashCode(cName);
      return hash;
    }
  }
}
