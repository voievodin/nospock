package com.yevhenii.nospock.jast;

import com.yevhenii.nospock.jast.stmt.JBlockStatement;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JClass implements JAstNode {

  private String name;

  private final List<JField> fields = new ArrayList<>();
  private final List<JAnnotationUsage> annotations = new ArrayList<>();
  private final List<JType> generics = new ArrayList<>();
  private final List<JType> interfaces = new ArrayList<>();
  private final List<JConstructor> constructors = new ArrayList<>();
  private final List<JMethod> methods = new ArrayList<>();
  private final List<JClass> innerClasses = new ArrayList<>();

  private int modifiers;
  private JType superclass;
  private JClassType classType = JClassType.CLASS;
  private JBlockStatement initBlock;

  // means that only class internals will be generated
  // basically instead of having 'class X { void x(); }' we will
  // have '{ void x(); }'.
  private boolean anonymous;

  public JClass(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public String name() {
    return name;
  }

  public JClassType classType() {
    return classType;
  }

  public void name(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public void modifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  public int modifiers() {
    return modifiers;
  }

  public void superclass(JType superclass) {
    this.superclass = superclass;
  }

  public JType superclass() {
    return superclass;
  }

  public void addAnnotation(JAnnotationUsage annotation) {
    this.annotations.add(annotation);
  }

  public void addInterfaces(JType interface0) {
    this.interfaces.add(interface0);
  }

  public void addGeneric(JType generic) {
    this.generics.add(generic);
  }

  public void addConstructor(JConstructor constructor) {
    this.constructors.add(constructor);
  }

  public void addField(JField field) {
    this.fields.add(field);
  }

  public void addMethod(JMethod method) {
    this.methods.add(method);
  }

  public void addInnerClass(JClass jClass) {
    innerClasses.add(jClass);
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public void classType(JClassType classType) {
    this.classType = classType;
  }

  public void initBlock(JBlockStatement initBlock) {
    this.initBlock = initBlock;
  }

  @Override
  public String asCode(CodeStyle style) {
    var sb = new StringBuilder();

    // annotations
    CodeHelper.appendEachNl(sb, annotations, style);

    // modifiers
    CodeHelper.appendModifiers(sb, modifiersAsCode());

    // definition
    sb.append(classType.asCode(style)).append(" ").append(name);
    if (!generics.isEmpty()) {
      sb.append('<');
      Iterator<JType> iterator = generics.iterator();
      while (iterator.hasNext()) {
        sb.append(iterator.next().asCode(style));
        if (iterator.hasNext()) {
          sb.append(", ");
        }
      }
      sb.append('>');
    }

    // superclass
    if (superclass != null && classType != JClassType.ENUM) {
      sb.append(" extends ").append(superclass.asCode(style));
    }

    // interfaces
    if (!interfaces.isEmpty() && classType != JClassType.ANNOTATION) {
      if (classType == JClassType.INTERFACE) {
        sb.append(" extends ");
      }
      {
        sb.append(" implements ");
      }
      sb.append(
        interfaces.stream()
          .map(jcu -> jcu.asCode(style))
          .collect(Collectors.joining(", "))
      );
    }

    sb.append(" {").append(style.nlSequence());

    // TODO: avoid unnecessary work in case of anonymous or have smarter hierarchy
    if (anonymous) {
      sb = new StringBuilder("{").append(style.nlSequence());
    }

    // init blocks
    if (initBlock != null) {
      CodeHelper.appendX(sb, style.nlSequence(), anonymous ? style.nlBeforeArbitraryAnonymousClassBlock() : style.nlBeforeMethod());
      sb.append(
        CodeHelper.indent(
          initBlock.asCode(style),
          style.nlSequence(),
          style.indent()
        )
      ).append(style.nlSequence());
    }

    // fields
    final List<JField> enumValueFields = fields.stream().filter(JField::isEnumValue).collect(Collectors.toList());
    final List<JField> nonEnumValueFields = fields.stream().filter(f -> !f.isEnumValue()).collect(Collectors.toList());

    // special case for enum values (which are represented as fields)
    if (classType == JClassType.ENUM && !enumValueFields.isEmpty()) {
      sb.append(style.indent()).append(
        enumValueFields.stream()
          .map(JField::name)
          .collect(Collectors.joining("," + style.nlSequence() + style.indent()))
      ).append(";").append(style.nlSequence());
    }

    appendFields(
      sb,
      nonEnumValueFields.stream().filter(JField::isStatic).collect(Collectors.toList()),
      style,
      style.nlBeforeFieldsGroup()
    );

    appendFields(
      sb,
      nonEnumValueFields.stream().filter(jf -> !jf.isStatic()).collect(Collectors.toList()),
      style,
      style.nlBeforeFieldsGroup()
    );

    // constructors
    for (JConstructor constructor : constructors) {
      CodeHelper.appendX(sb, style.nlSequence(), style.nlBeforeMethod());
      sb.append(
          CodeHelper.indent(
            constructor.asCode(style),
            style.nlSequence(),
            style.indent()
          )
        )
        .append(style.nlSequence());
    }

    // methods
    for (JMethod method : methods) {

      if (classType == JClassType.INTERFACE || classType == JClassType.ANNOTATION) {
        method.setNoAbstractModifier();
        method.setNoAccessModifier();
      }

      CodeHelper.appendX(sb, style.nlSequence(), style.nlBeforeMethod());
      sb.append(
          CodeHelper.indent(
            method.asCode(style),
            style.nlSequence(),
            style.indent()
          )
        )
        .append(style.nlSequence());
    }

    // inner classes
    for (JClass innerClass : innerClasses) {
      CodeHelper.appendX(sb, style.nlSequence(), style.nlBeforeMethod());
      sb.append(
        CodeHelper.indent(
          innerClass.asCode(style),
          style.nlSequence(),
          style.indent()
        )
      ).append(style.nlSequence());
    }

    return sb.append("}").append(style.nlSequence()).toString();
  }

  private static void appendFields(StringBuilder sb, List<JField> fields, CodeStyle style, int nlBefore) {
    if (!fields.isEmpty()) {
      CodeHelper.appendX(sb, style.nlSequence(), nlBefore);
    }
    for (JField jf : fields) {
      sb.append(
          CodeHelper.indent(
            jf.asCode(style) + ';',
            style.nlSequence(),
            style.indent()
          )
        )
        .append(style.nlSequence());
    }
  }

  private int modifiersAsCode() {
    int tmpMod = modifiers;
    if (classType != JClassType.CLASS) {
      tmpMod = tmpMod & ~(Modifier.ABSTRACT | Modifier.FINAL);
    }
    return tmpMod;
  }
}
