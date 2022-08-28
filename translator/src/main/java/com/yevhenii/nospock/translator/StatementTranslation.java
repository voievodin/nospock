package com.yevhenii.nospock.translator;

import com.yevhenii.nospock.jast.stmt.JStatement;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Component that configures full support for statement translation.
 */
public class StatementTranslation<G extends Statement, J extends JStatement> extends NodeTranslation<G, J> {
  public StatementTranslation(TKey<G, J> key, Translator<G, J> translator) {
    super(key, translator);
  }
}
