package com.yevhenii.nospock.jast;

public class CodeStyle {

  private int nlAfterPackage = 1;
  private int nlAfterNonStaticImports = 1;
  private int nlAfterStaticImports = 1;
  private String nlSequence = "\n";
  private int nlBeforeFieldsGroup = 1;
  private int nlBeforeMethod = 1;
  private int nlBeforeArbitraryAnonymousClassBlock = 0;
  private int multilineArgumentsThreshold = 3;
  private String indent = "  ";

  public String indent() {
    return indent;
  }

  public String nlSequence() {
    return nlSequence;
  }

  public int nlAfterPackage() {
    return nlAfterPackage;
  }

  public int nlAfterNonStaticImports() {
    return nlAfterNonStaticImports;
  }

  public int nlAfterStaticImports() {
    return nlAfterStaticImports;
  }

  public int nlBeforeFieldsGroup() {
    return nlBeforeFieldsGroup;
  }

  public int nlBeforeMethod() {
    return nlBeforeMethod;
  }

  public int nlBeforeArbitraryAnonymousClassBlock() {
    return nlBeforeArbitraryAnonymousClassBlock;
  }

  public CodeStyle setNlAfterPackage(int nlAfterPackage) {
    this.nlAfterPackage = nlAfterPackage;
    return this;
  }

  public CodeStyle setNlAfterNonStaticImports(int nlAfterNonStaticImports) {
    this.nlAfterNonStaticImports = nlAfterNonStaticImports;
    return this;
  }

  public CodeStyle setNlAfterStaticImports(int nlAfterStaticImports) {
    this.nlAfterStaticImports = nlAfterStaticImports;
    return this;
  }

  public CodeStyle setNlBeforeFieldsGroup(int nlBeforeFieldsGroup) {
    this.nlBeforeFieldsGroup = nlBeforeFieldsGroup;
    return this;
  }

  public CodeStyle setNlBeforeMethod(int nlBeforeMethod) {
    this.nlBeforeMethod = nlBeforeMethod;
    return this;
  }

  public CodeStyle setNlBeforeArbitraryAnonymousClassBlock(int nlBeforeArbitraryAnonymousClassBlock) {
    this.nlBeforeArbitraryAnonymousClassBlock = nlBeforeArbitraryAnonymousClassBlock;
    return this;
  }

  public CodeStyle setNlSequence(String nlSequence) {
    this.nlSequence = nlSequence;
    return this;
  }

  public CodeStyle setIndent(String indent) {
    this.indent = indent;
    return this;
  }

  public CodeStyle setMultilineArgumentsThreshold(int multilineArgumentsThreshold) {
    this.multilineArgumentsThreshold = multilineArgumentsThreshold;
    return this;
  }

  public int multilineArgumentsThreshold() {
    return multilineArgumentsThreshold;
  }
}
