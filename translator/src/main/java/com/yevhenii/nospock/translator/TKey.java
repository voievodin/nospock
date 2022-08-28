package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.JAnnotationUsage;
import com.yevhenii.nospock.jast.JAstNode;
import com.yevhenii.nospock.jast.JClass;
import com.yevhenii.nospock.jast.JConstructor;
import com.yevhenii.nospock.jast.JField;
import com.yevhenii.nospock.jast.JImport;
import com.yevhenii.nospock.jast.JMethod;
import com.yevhenii.nospock.jast.JPackage;
import com.yevhenii.nospock.jast.JParameter;
import com.yevhenii.nospock.jast.JType;
import com.yevhenii.nospock.jast.exp.JArrayExpression;
import com.yevhenii.nospock.jast.exp.JBinaryExpression;
import com.yevhenii.nospock.jast.exp.JCastExpression;
import com.yevhenii.nospock.jast.exp.JClassLiteralExpression;
import com.yevhenii.nospock.jast.exp.JConstantExpression;
import com.yevhenii.nospock.jast.exp.JConstructorCallExpression;
import com.yevhenii.nospock.jast.exp.JDeclarationExpression;
import com.yevhenii.nospock.jast.exp.JExpression;
import com.yevhenii.nospock.jast.exp.JFieldAccessExpression;
import com.yevhenii.nospock.jast.exp.JLambdaExpression;
import com.yevhenii.nospock.jast.exp.JMethodReferenceExpression;
import com.yevhenii.nospock.jast.exp.JNotExpression;
import com.yevhenii.nospock.jast.exp.JPostfixExpression;
import com.yevhenii.nospock.jast.exp.JPrefixExpression;
import com.yevhenii.nospock.jast.exp.JTernaryExpression;
import com.yevhenii.nospock.jast.exp.JVariableExpression;
import com.yevhenii.nospock.jast.stmt.JBlockStatement;
import com.yevhenii.nospock.jast.stmt.JBreakStatement;
import com.yevhenii.nospock.jast.stmt.JContinueStatement;
import com.yevhenii.nospock.jast.stmt.JEmptyStatement;
import com.yevhenii.nospock.jast.stmt.JExpressionStatement;
import com.yevhenii.nospock.jast.stmt.JIfStatement;
import com.yevhenii.nospock.jast.stmt.JReturnStatement;
import com.yevhenii.nospock.jast.stmt.JStatement;
import com.yevhenii.nospock.jast.stmt.JSwitchStatement;
import com.yevhenii.nospock.jast.stmt.JThrowStatement;
import com.yevhenii.nospock.jast.stmt.JTryCatchStatement;
import com.yevhenii.nospock.jast.stmt.JWhileStatement;
import com.yevhenii.nospock.translator.spock.JForeignStatement;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import java.util.Objects;

/**
 * Points to a concrete translator within {@link Translators}.
 * Primarily used to manipulate/lookup translators in the registry.
 */
public final class TKey<F extends ASTNode, T extends JAstNode> {

  public static final TKey<ClassNode, JClass> CLASS = new TKey<>(ClassNode.class, JClass.class);
  public static final TKey<AnnotationNode, JAnnotationUsage> ANNOTATION_USAGE = new TKey<>(AnnotationNode.class, JAnnotationUsage.class);
  public static final TKey<ClassNode, JType> TYPE = new TKey<>(ClassNode.class, JType.class);
  public static final TKey<FieldNode, JField> FIELD = new TKey<>(FieldNode.class, JField.class);
  public static final TKey<ConstructorNode, JConstructor> CONSTRUCTOR = new TKey<>(ConstructorNode.class, JConstructor.class);
  public static final TKey<BlockStatement, JBlockStatement> ST_BLOCK = new TKey<>(BlockStatement.class, JBlockStatement.class);
  public static final TKey<MethodNode, JMethod> METHOD = new TKey<>(MethodNode.class, JMethod.class);
  public static final TKey<MethodNode, JMethod> TEST_METHOD = new TKey<>(MethodNode.class, JMethod.class, "test");
  public static final TKey<MethodNode, JMethod> LIFECYCLE_TEST_METHOD = new TKey<>(MethodNode.class, JMethod.class, "lifecycle");
  public static final TKey<Parameter, JParameter> PARAMETER = new TKey<>(Parameter.class, JParameter.class);
  public static final TKey<ImportNode, JImport> IMPORT = new TKey<>(ImportNode.class, JImport.class);
  public static final TKey<PackageNode, JPackage> PACKAGE = new TKey<>(PackageNode.class, JPackage.class);
  public static final TKey<BinaryExpression, JExpression> EX_BINARY = new TKey<>(BinaryExpression.class, JExpression.class);
  public static final TKey<ClosureExpression, JLambdaExpression> EX_CLOSURE = new TKey<>(ClosureExpression.class, JLambdaExpression.class);
  public static final TKey<ConstantExpression, JConstantExpression> EX_CONSTANT = new TKey<>(ConstantExpression.class, JConstantExpression.class);
  public static final TKey<ConstructorCallExpression, JConstructorCallExpression> EX_CONSTRUCTOR_CALL = new TKey<>(ConstructorCallExpression.class, JConstructorCallExpression.class);
  public static final TKey<DeclarationExpression, JDeclarationExpression> EX_DECLARATION = new TKey<>(DeclarationExpression.class, JDeclarationExpression.class);
  public static final TKey<FieldExpression, JFieldAccessExpression> EX_FIELD_ACCESS = new TKey<>(FieldExpression.class, JFieldAccessExpression.class);
  public static final TKey<MethodCallExpression, JExpression> EX_METHOD_CALL = new TKey<>(MethodCallExpression.class, JExpression.class);
  public static final TKey<PostfixExpression, JPostfixExpression> EX_POSTFIX = new TKey<>(PostfixExpression.class, JPostfixExpression.class);
  public static final TKey<PropertyExpression, JExpression> EX_PROPERTY = new TKey<>(PropertyExpression.class, JExpression.class);
  public static final TKey<VariableExpression, JVariableExpression> EX_VARIABLE = new TKey<>(VariableExpression.class, JVariableExpression.class);
  public static final TKey<BooleanExpression, JExpression> EX_BOOLEAN = new TKey<>(BooleanExpression.class, JExpression.class);
  public static final TKey<CastExpression, JCastExpression> EX_CAST = new TKey<>(CastExpression.class, JCastExpression.class);
  public static final TKey<ListExpression, JExpression> EX_LIST = new TKey<>(ListExpression.class, JExpression.class);
  public static final TKey<NotExpression, JNotExpression> EX_NOT = new TKey<>(NotExpression.class, JNotExpression.class);
  public static final TKey<ClassExpression, JClassLiteralExpression> EX_CLASS_LITERAL = new TKey<>(ClassExpression.class, JClassLiteralExpression.class);
  public static final TKey<BreakStatement, JBreakStatement> ST_BREAK = new TKey<>(BreakStatement.class, JBreakStatement.class);
  public static final TKey<ContinueStatement, JContinueStatement> ST_CONTINUE = new TKey<>(ContinueStatement.class, JContinueStatement.class);
  public static final TKey<EmptyStatement, JEmptyStatement> ST_EMPTY = new TKey<>(EmptyStatement.class, JEmptyStatement.class);
  public static final TKey<ExpressionStatement, JExpressionStatement> ST_EXPRESSION = new TKey<>(ExpressionStatement.class, JExpressionStatement.class);
  public static final TKey<ForStatement, JStatement> ST_FOR = new TKey<>(ForStatement.class, JStatement.class);
  public static final TKey<IfStatement, JIfStatement> ST_IF = new TKey<>(IfStatement.class, JIfStatement.class);
  public static final TKey<ReturnStatement, JReturnStatement> ST_RETURN = new TKey<>(ReturnStatement.class, JReturnStatement.class);
  public static final TKey<SwitchStatement, JSwitchStatement> ST_SWITCH = new TKey<>(SwitchStatement.class, JSwitchStatement.class);
  public static final TKey<ThrowStatement, JThrowStatement> ST_THROW = new TKey<>(ThrowStatement.class, JThrowStatement.class);
  public static final TKey<TryCatchStatement, JTryCatchStatement> ST_TRY_CATCH = new TKey<>(TryCatchStatement.class, JTryCatchStatement.class);
  public static final TKey<WhileStatement, JWhileStatement> ST_WHILE = new TKey<>(WhileStatement.class, JWhileStatement.class);
  public static final TKey<AssertStatement, JForeignStatement> ST_ASSERT = new TKey<>(AssertStatement.class, JForeignStatement.class);
  public static final TKey<PrefixExpression, JPrefixExpression> EX_PREFIX = new TKey<>(PrefixExpression.class, JPrefixExpression.class);
  public static final TKey<ArrayExpression, JArrayExpression> EX_ARRAY = new TKey<>(ArrayExpression.class, JArrayExpression.class);
  public static final TKey<MapExpression, JExpression> EX_MAP = new TKey<>(MapExpression.class, JExpression.class);
  public static final TKey<SpreadExpression, JExpression> EX_SPREAD = new TKey<>(SpreadExpression.class, JExpression.class);
  public static final TKey<TernaryExpression, JTernaryExpression> EX_TERNARY = new TKey<>(TernaryExpression.class, JTernaryExpression.class);
  public static final TKey<MethodPointerExpression, JMethodReferenceExpression> EX_METHOD_POINTER = new TKey<>(MethodPointerExpression.class, JMethodReferenceExpression.class);
  public static final TKey<GStringExpression, JBinaryExpression> EX_GSTRING = new TKey<>(GStringExpression.class, JBinaryExpression.class);
  public static final TKey<ElvisOperatorExpression, JTernaryExpression> EX_ELVIS = new TKey<>(ElvisOperatorExpression.class, JTernaryExpression.class);

  private final Class<F> f;
  private final Class<T> t;
  private final String qualifier;

  private TKey(Class<F> f, Class<T> t, String qualifier) {
    this.f = Objects.requireNonNull(f);
    this.t = Objects.requireNonNull(t);
    this.qualifier = qualifier;
  }

  private TKey(Class<F> f, Class<T> t) {
    this(f, t, null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TKey<?, ?> tKey = (TKey<?, ?>) o;
    return Objects.equals(f, tKey.f) && Objects.equals(t, tKey.t) && Objects.equals(qualifier, tKey.qualifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(f, t, qualifier);
  }
}
