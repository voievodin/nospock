package com.yevhenii.nospock.translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SpockSourceFile {

  private final Path path;

  public SpockSourceFile(Path path) {
    if (!path.getFileName().toString().toLowerCase().endsWith(".groovy")) {
      throw new IllegalArgumentException("Expected .groovy file extension");
    }
    this.path = path;
  }

  public Path path() {
    return path;
  }

  public String read() throws IOException {
    return Files.readString(path);
  }
}
