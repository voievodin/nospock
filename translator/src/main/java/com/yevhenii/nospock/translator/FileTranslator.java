package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JPackage;
import com.yevhenii.nospock.translator.spi.TranslationException;
import com.yevhenii.nospock.jast.JFile;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileTranslator {

  private final Translator<PackageNode, JPackage> packageTranslator;
  private final Translator<ImportNode, JImport> importTranslator;
  private final Translator<ClassNode, JClass> classTranslator;

  public FileTranslator(
    Translator<PackageNode, JPackage> packageTranslator,
    Translator<ImportNode, JImport> importTranslator,
    Translator<ClassNode, JClass> classTranslator
  ) {
    this.packageTranslator = Objects.requireNonNull(packageTranslator);
    this.importTranslator = Objects.requireNonNull(importTranslator);
    this.classTranslator = Objects.requireNonNull(classTranslator);
  }

  public JFile translate(SpockSourceFile file) throws TranslationException {
    final List<ASTNode> nodes;
    try {
      nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, true, file.read());
    } catch (IOException ioEx) {
      throw new TranslationException(ioEx);
    }

    // Having only class nodes seems enough to get the rest of information in the file
    // well at least this is true for the simple tests I have in mind for now
    final List<ClassNode> classNodes = nodes.stream()
      .filter(node -> node.getClass() == ClassNode.class) // intentionally skip inner classes and other members
      .map(node -> (ClassNode) node)
      .collect(Collectors.toList());

    // not necessary to have, but for now not clear what's the purpose of such file anyway
    if (classNodes.isEmpty()) {
      throw new TranslationException("At least one class expected in the source file");
    }

    // non class related data
    final ClassNode class0 = classNodes.get(0);
    final ModuleNode module = class0.getModule();

    final JFile jFile = new JFile(
      packageTranslator.translate(module.getPackage(), null),
      class0.getNameWithoutPackage()
    );

    // imports
    for (ImportNode anImport : module.getImports()) {
      jFile.addImport(importTranslator.translate(anImport, null));
    }
    for (ImportNode anImport : module.getStarImports()) {
      jFile.addImport(importTranslator.translate(anImport, null));
    }
    for (ImportNode anImport : module.getStaticImports().values()) {
      jFile.addImport(importTranslator.translate(anImport, null));
    }
    for (ImportNode anImport : module.getStaticStarImports().values()) {
      jFile.addImport(importTranslator.translate(anImport, null));
    }

    // classes
    for (ClassNode classNode : classNodes) {
      TContext context = new TContext(
        jFile.getPackage(),
        jFile.imports(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        CPath.ROOT
      );
      final JClass translated = classTranslator.translate(classNode, context);
      translated.modifiers(translated.modifiers() & ~Modifier.PUBLIC);
      jFile.addClass(translated);
    }

    // make ok
    jFile.imports().removeIf(anImport -> anImport.fqn().asString().startsWith("spock.lang"));

    TransformationsQueue.transformAll(TransformationsQueue.Target.FILE, CPath.ROOT, jFile);

    return jFile;
  }
}
