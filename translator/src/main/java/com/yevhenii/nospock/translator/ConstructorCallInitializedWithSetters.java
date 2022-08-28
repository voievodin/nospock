package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JMethodCallExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;

import java.util.List;

/**
 * In some cases the anonymous type can be replaced with
 * simpler and inlined structure. For example, when initialization block
 * of a class is used to preset some values. However, in some cases
 * that initialization can follow the actual constructor call.
 * So {@code new X() { { setValue(1); }}} becomes {@code X x = new X(); x.setValue(1); }.
 * Note that post construction statements should reflect behaviour defined in anonymous type,
 * as depending on the context either of those will be used.
 */
public class ConstructorCallInitializedWithSetters extends JConstructorCallExpression {

  private final List<JMethodCallExpression> setterCalls; 
  
  public ConstructorCallInitializedWithSetters(JType type, List<JMethodCallExpression> setterCalls) {
    super(type, asAnonymousTypeWithInitialization(type, setterCalls));
    this.setterCalls = List.copyOf(setterCalls);
  }
  
  public List<JMethodCallExpression> setterCallsForVariable(JVariableExpression variable) {
    for (JMethodCallExpression setterCall : setterCalls) {
      setterCall.object(variable);
    }
    return setterCalls;
  }
  
  private static JClass asAnonymousTypeWithInitialization(JType type, List<JMethodCallExpression> setterCalls) {
    final var init = new JBlockStatement();
    for (JMethodCallExpression setterCall : setterCalls) {
      init.statements().add(new JExpressionStatement(setterCall));
    }
    final var jClass = new JClass(type.name());
    jClass.setAnonymous(true);
    jClass.initBlock(init);
    return jClass;
  }
}
