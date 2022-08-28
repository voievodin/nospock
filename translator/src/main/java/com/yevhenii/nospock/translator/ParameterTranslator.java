package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;

import java.util.Objects;

public class ParameterTranslator implements Translator<Parameter, JParameter> {

  private final Translator<ClassNode, JType> typeTranslator;

  public ParameterTranslator(Translator<ClassNode, JType> typeTranslator) {
    this.typeTranslator = Objects.requireNonNull(typeTranslator);
  }

  @Override
  public JParameter translate(Parameter parameter, TContext context) {
    return new JParameter(
      typeTranslator.translate(parameter.getType(), context),
      parameter.getName()
    );
  }
}
