package com.yevhenii.nospock.translator.spi.assertion.impl.junit;

import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.exp.*;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.translator.spock.JForeignStatement;
import com.yevhenii.nospock.translator.spi.assertion.AssertionsTemplate;

import java.util.List;

public class JUnitAssertionsTemplate implements AssertionsTemplate {

  private final boolean useStaticImports;

  public JUnitAssertionsTemplate(boolean useStaticImports) {
    this.useStaticImports = useStaticImports;
  }

  @Override
  public JForeignStatement assertEquals(JAstNode expected, JAstNode actual) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertEquals",
          List.of(expected, actual)
        )
      ),
      List.of(importFor("assertEquals"))
    );
  }

  @Override
  public JForeignStatement assertThrows(JExpression declaration, JFieldAccessExpression exClass, JBlockStatement execution) {
    final var assertThrows = new JMethodCallExpression(
      assertionsOrThis(),
      "assertThrows",
      List.of(
        exClass,
        new JLambdaExpression(List.of(), execution)
      )
    );

    return new JForeignStatement(
      new JExpressionStatement(
        declaration == null ? assertThrows : new JDeclarationExpression(declaration, assertThrows)
      ),
      List.of(importFor("assertThrows"))
    );
  }

  @Override
  public JForeignStatement assertTrue(JExpression expression) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertTrue",
          List.of(expression)
        )
      ),
      List.of(importFor("assertTrue"))
    );
  }

  @Override
  public JForeignStatement assertNotNull(JExpression expression) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertNotNull",
          List.of(expression)
        )
      ),
      List.of(importFor("assertNotNull"))
    );
  }

  @Override
  public JForeignStatement assertNotEquals(JExpression v1, JExpression v2) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertNotEquals",
          List.of(v1, v2)
        )
      ),
      List.of(importFor("assertNotEquals"))
    );
  }

  @Override
  public JForeignStatement assertNull(JExpression expression) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertNull",
          List.of(expression)
        )
      ),
      List.of(importFor("assertNull"))
    );
  }

  @Override
  public JForeignStatement assertFalse(JExpression expression) {
    return new JForeignStatement(
      new JExpressionStatement(
        new JMethodCallExpression(
          assertionsOrThis(),
          "assertFalse",
          List.of(expression)
        )
      ),
      List.of(importFor("assertFalse"))
    );
  }

  private JAstNode assertionsOrThis() {
    return useStaticImports ? JVariableExpression.this0() : new JType("Assertions");
  }

  private JImport importFor(String method) {
    final var fqn = "org.junit.jupiter.api.Assertions";
    if (useStaticImports) {
      return new JImport(fqn + '.' + method, true);
    } else {
      return new JImport(fqn, false);
    }
  }
}
