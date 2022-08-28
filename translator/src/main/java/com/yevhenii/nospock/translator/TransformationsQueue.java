package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JFile;
import com.yevhenii.nospock.jast.JImport;

import java.util.*;

public class TransformationsQueue {

  public enum Target {
    FILE,
    CLASS,
    BLOCK
  }

  public enum Priority {
    IMMEDIATE,
    IN_THE_END
  }

  public interface Transformation {
    void transform(JAstNode jAstNode);
  }

  public static ThreadLocal<TransformationsQueue> INSTANCE = new ThreadLocal<>();

  public static TransformationsQueue instance() {
    if (INSTANCE.get() == null) {
      INSTANCE.set(new TransformationsQueue());
    }
    return INSTANCE.get();
  }

  public static void transform(Target target, CPath cPath, Priority priority, JAstNode node) {
    for (Transformation t : TransformationsQueue.instance().poll(target, cPath, priority)) {
      t.transform(node);
    }
  }

  public static void transformAll(Target target, CPath cPath, JAstNode node) {
    for (Priority priority : Priority.values()) {
      transform(target, cPath, priority, node);
    }
  }

  private final Map<QueueKey, List<Transformation>> transformations = new LinkedHashMap<>();

  public List<Transformation> poll(Target target, CPath cPath, Priority priority) {
    List<Transformation> result = transformations.remove(new QueueKey(target, cPath, priority));
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public void enqueue(Target target, CPath path, Priority priority, Transformation transformation) {
    transformations.compute(
      new QueueKey(target, path, priority),
      (k, queue) -> {
        if (queue == null) {
          queue = new ArrayList<>();
        }
        queue.add(transformation);
        return queue;
      }
    );
  }

  public void enqueueNewImports(List<JImport> imports) {
    if (imports != null && !imports.isEmpty()) {
      instance().enqueue(
        Target.FILE,
        CPath.ROOT,
        Priority.IN_THE_END,
        jAstNode -> {
          final var file = (JFile) jAstNode;

          // make it ok
          for (JImport anImport : imports) {
            if (!file.imports().contains(anImport)) {
              file.addImport(anImport);
            }
          }
        }
      );
    }
  }

  public void enqueueNewImports(JImport... imports) {
    enqueueNewImports(Arrays.asList(imports));
  }

  public void reset() {
    INSTANCE.set(new TransformationsQueue());
  }

  private static class QueueKey {
    public final Target target;
    public final CPath cPath;
    public final Priority priority;

    private QueueKey(Target target, CPath cPath, Priority priority) {
      this.target = Objects.requireNonNull(target);
      this.cPath = Objects.requireNonNull(cPath);
      this.priority = Objects.requireNonNull(priority);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      QueueKey queueKey = (QueueKey) o;
      return target == queueKey.target && Objects.equals(cPath, queueKey.cPath) && priority == queueKey.priority;
    }

    @Override
    public int hashCode() {
      return Objects.hash(target, cPath, priority);
    }
  }
}
