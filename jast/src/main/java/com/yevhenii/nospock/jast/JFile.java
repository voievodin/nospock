package com.yevhenii.nospock.jast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JFile implements JAstNode {

  private String name;

  private final JPackage package0;
  private final List<JImport> imports = new ArrayList<>();
  private final List<JClass> classes = new ArrayList<>();

  public JFile(JPackage package0, String bane) {
    this.package0 = package0;
    this.name = bane;
  }

  public String name() {
    return name;
  }

  public void rename(String name) {
    for (JClass aClass : classes) {
      if (aClass.name().equals(this.name)) {
        aClass.name(name);
      }
    }
    this.name = Objects.requireNonNull(name);
  }

  public JClass mainClass() {
    for (JClass aClass : classes) {
      if (aClass.name().equals(this.name)) {
        return aClass;
      }
    }
    return null;
  }

  public List<JClass> classes() {
    return classes;
  }

  public void addImport(JImport jImport) {
    this.imports.add(jImport);
  }

  public List<JImport> imports() {
    return imports;
  }

  public void addClass(JClass jClass) {
    this.classes.add(jClass);
  }

  public void write(Path directory, CodeStyle style) throws IOException {
    if (!Files.exists(directory)) {
      Files.createDirectories(directory);
    }
    Files.writeString(directory.resolve(name + ".java"), asCode(style));
  }

  @Override
  public String asCode(CodeStyle style) {
    StringBuilder sb = new StringBuilder();

    // package
    if (!package0.isEmpty()) {
      sb.append(package0.asCode(style));
      CodeHelper.appendX(sb, style.nlSequence(), style.nlAfterPackage() + 1);
    }

    // imports
    if (!imports.isEmpty()) {
      List<JImport> nonStaticImports = imports.stream().filter(im -> !im.isStatic()).sorted().collect(Collectors.toList());
      if (!nonStaticImports.isEmpty()) {
        CodeHelper.appendEachNl(sb, nonStaticImports, style);
        CodeHelper.appendX(sb, style.nlSequence(), style.nlAfterNonStaticImports());
      }

      List<JImport> staticImports = imports.stream().filter(JImport::isStatic).sorted().collect(Collectors.toList());
      if (!staticImports.isEmpty()) {
        CodeHelper.appendEachNl(sb, staticImports, style);
        CodeHelper.appendX(sb, style.nlSequence(), style.nlAfterStaticImports());
      }
    }

    // classes
    sb.append(
      classes.stream()
        .map(c -> c.asCode(style))
        .collect(Collectors.joining(style.nlSequence()))
    );
    
    return AsCodePostProcessor.wrapUpProcessing(sb.toString(), style);
  }

  public JPackage getPackage() {
    return package0;
  }
}
