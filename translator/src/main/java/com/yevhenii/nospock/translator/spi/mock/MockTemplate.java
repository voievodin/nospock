package com.yevhenii.nospock.translator.spi.mock;

import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JLambdaExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.translator.spock.JForeignExpression;
import com.yevhenii.nospock.translator.spock.JForeignStatement;
import com.yevhenii.nospock.translator.spock.mock.MockType;

import java.util.List;
import java.util.Objects;

public interface MockTemplate {

  JForeignExpression initialization(
    MockType mockType,
    JExpression classReference,
    JLambdaExpression answer
  );

  JForeignStatement mockReturnValue(
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers,
    JExpression thenReturn
  );

  JForeignStatement verifyTimesCalled(
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers,
    Times times
  );

  JForeignStatement doAnswer(
    ArgumentsAssignment assignment,
    JBlockStatement block,
    JExpression object,
    String methodName,
    List<ArgumentMatcher> matchers
  );

  class ArgumentMatcher {

    public enum Rule {
      ANY,
      ANY_BYTE,
      ANY_SHORT,
      ANY_INT,
      ANY_LONG,
      ANY_CHAR,
      ANY_BOOLEAN,
      ANY_FLOAT,
      ANY_DOUBLE,
      ANY_OF_TYPE,
      EQ,
      ARG_THAT,
    }

    public final Rule rule;
    public final JExpression value;

    public ArgumentMatcher(Rule rule, JExpression value) {
      this.rule = rule;
      this.value = value;
    }
  }

  class ArgumentsAssignment {
    public JVariableExpression argsVariable;
    public List<JVariableExpression> parameterVariables;

    public ArgumentsAssignment(JVariableExpression argsVariable) {
      this.argsVariable = argsVariable;
    }

    public ArgumentsAssignment(List<JVariableExpression> parameterVariables) {
      this.parameterVariables = parameterVariables;
    }
  }
  
  class Times {

    public enum Type {
      ONCE,
      NEVER,
      EXACTLY,
      AT_LEAST
    }
    
    public final Type type;
    public final JExpression expression;

    public Times(Type type, JExpression expression) {
      this.type = Objects.requireNonNull(type);
      this.expression = expression;
    }

    @Override
    public String toString() {
      return "Times{" +
             "type=" + type +
             ", expression=" + expression +
             '}';
    }
  }
}
