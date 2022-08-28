package com.yevhenii.nospock;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.yevhenii.nospock.settings.NoSpockSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Conversion {

  private final Path inputFile;
  private final Path javaBinaryPath;
  private final Cp cp;
  private Path outputDir;

  public Conversion(PsiFile psiFile) throws ConversionException {
    final NoSpockSettings settings = NoSpockSettings.getInstance();
    this.inputFile = psiFile.getVirtualFile().toNioPath().toAbsolutePath();
    this.javaBinaryPath = projectSdkHome(psiFile.getProject()).resolve("bin").resolve("java");
    this.cp = Cp.forFileInModule(psiFile);
    if (!new File(settings.executableJarPath).exists()) {
      throw new ConversionException("Configured executable jar doesn't exist: " + settings.executableJarPath);
    }
    this.cp.paths.add(new File(settings.executableJarPath).toPath().toAbsolutePath());
    this.outputDir = defaultOutputDirForFile(psiFile);
  }

  public Path outputDir() {
    return outputDir;
  }

  public void outputDir(Path outputDir) {
    this.outputDir = Objects.requireNonNull(outputDir).toAbsolutePath();
  }

  public void convert() throws ConversionException {
    final List<String> command = command();
    final Process process;
    try {
      process = new ProcessBuilder(command)
        .redirectErrorStream(true)
        .start();
    } catch (IOException ioEx) {
      throw new ConversionException("Failed to create process, command: " + command + ". Reason: " + ioEx.getMessage());
    }

    String output;
    try {
      output = readOutput(process);
    } catch (IOException ioEx) {
      throw new ConversionException("Failed to read process output: " + ioEx.getMessage());
    }

    final int exitCode;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new ConversionException("Interrupted while waiting for process to finish");
    }

    if (exitCode != 0) {
      throw new ConversionException("Failed to execute conversion: " + output);
    }
  }

  public List<String> command() {
    final NoSpockSettings settings = NoSpockSettings.getInstance();
    final List<String> command = new ArrayList<>();
    command.add(javaBinaryPath.toAbsolutePath().toString());
    command.add("-cp");
    command.add(cp.asCmdOption());
    command.add("com.yevhenii.nospock.NoSpock");
    command.add("s2j");
    command.add("-i"); 
    command.add(inputFile.toString());
    command.add("-o");
    command.add(outputDir.toString());
    command.add("-rename-pattern");
    command.add(settings.renameTestClassPatterns);
    command.add("-assertions-provider");
    command.add(settings.assertionsProvider);
    command.add("-mocks-provider");
    command.add(settings.mocksProvider);
    command.add("-spock-labels-presence-mode");
    command.add(settings.spockLabelsPresenceMode);
    if (settings.assertionsUseStaticImports) {
      command.add("--static-imports-assertions");
    }
    if (settings.mocksUseStaticImports) {
      command.add("--static-imports-mocks");
    }
    if (settings.removeSuperclasses != null && !settings.removeSuperclasses.isBlank()) {
      command.add("-remove-superclasses");
      command.add(settings.removeSuperclasses);
    }
    if (settings.textBlocksEnabled) {
      command.add("--text-blocks-enabled");
    }
    return command;
  }

  public String commandStr() {
    return String.join(" ", command());
  }

  private static String readOutput(Process process) throws IOException {
    final var sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String l;
      while ((l = reader.readLine()) != null) {
        sb.append(l).append('\n');
      }
    }
    return sb.toString();
  }

  private static Path projectSdkHome(Project project) throws ConversionException {
    final Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
    if (projectSdk == null) {
      throw new ConversionException("Project SDK must be selected");
    }
    if (projectSdk.getHomePath() == null) {
      throw new ConversionException(
        String.format(
          "Project SDK '%s' doesn't have home directory set",
          projectSdk.getName()
        )
      );
    }
    return new File(projectSdk.getHomePath()).toPath().toAbsolutePath();
  }

  // moves to identical package replacing groovy -> java in path
  // e.g. src/test/groovy/x/y/X.groovy -> src/test/java/x/y/
  private static Path defaultOutputDirForFile(PsiFile file) {
    final var path = file.getVirtualFile().toNioPath().toAbsolutePath();
    final var segments = new String[path.getNameCount()];
    for (int i = 0; i < path.getNameCount(); i++) {
      final var name = path.getName(i);
      if (name.toString().equals("groovy")) {
        segments[i] = "java";
      } else {
        segments[i] = name.toString();
      }
    }
    var result = path.getRoot();
    for (String segment : segments) {
      result = result.resolve(Path.of(segment));
    }
    return result.getParent();
  }

  private static class Cp {

    static Cp forFileInModule(PsiFile psiFile) throws ConversionException {
      final Module module = ModuleUtil.findModuleForPsiElement(psiFile);
      if (module == null) {
        throw new ConversionException(
          "Cannot locate project module for psi file: " + psiFile + ". Required to get correct classpath"
        );
      }
      final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
      final List<Path> paths = new ArrayList<>();
      for (String path : rootManager.orderEntries().classes().getPathsList().getPathList()) {
        paths.add(new File(FileUtil.toSystemIndependentName(path)).toPath());
      }
      return new Cp(paths);
    }

    final List<Path> paths;

    Cp(List<Path> paths) {
      this.paths = paths
        .stream()
        .filter(p -> !p.toString().contains("org.codehaus.groovy")) // keep the one bundled with the lib, questionable though
        .filter(p -> !p.toString().contains("awaitility")) // keep the one bundled with the lib, questionable though
        .collect(Collectors.toList());
    }

    String asCmdOption() {
      return paths.stream()
        .map(p -> p.toAbsolutePath().toString())
        .collect(Collectors.joining(":"));
    }
  }
}
