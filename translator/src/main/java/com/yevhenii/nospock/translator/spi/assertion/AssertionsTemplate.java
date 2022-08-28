package com.yevhenii.nospock.translator.spi.assertion;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.translator.spock.JForeignStatement;

public interface AssertionsTemplate {
  JForeignStatement assertEquals(JAstNode expected, JAstNode actual);

  JForeignStatement assertThrows(JExpression declaration, JFieldAccessExpression exClass, JBlockStatement execution);

  JForeignStatement assertTrue(JExpression expression);

  JForeignStatement assertNotNull(JExpression expression);

  JForeignStatement assertNotEquals(JExpression v1, JExpression v2);

  JForeignStatement assertNull(JExpression expression);

  JForeignStatement assertFalse(JExpression translate);
}
