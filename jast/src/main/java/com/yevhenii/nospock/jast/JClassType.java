package com.yevhenii.nospock.jast;

public enum JClassType implements JAstNode {

  CLASS {
    @Override
    public String asCode(CodeStyle style) {
      return "class";
    }
  },

  ENUM {
    @Override
    public String asCode(CodeStyle style) {
      return "enum";
    }
  },

  INTERFACE {
    @Override
    public String asCode(CodeStyle style) {
      return "interface";
    }
  },

  ANNOTATION {
    @Override
    public String asCode(CodeStyle style) {
      return "@interface";
    }
  }
}
