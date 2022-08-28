package com.yevhenii.nospock.jast;

/**
 * This component exists due to the pure interface that {@link JAstNode#asCode} provides. 
 * There is no good way to represent multiline strings atm. Once conversion is switched to 
 * more structure format this component might not be used.
 */
public class AsCodePostProcessor {
  
  public static final String NL_PLACEHOLDER = "$$$$$nl$$$$$";
  
  public static String wrapUpProcessing(String source, CodeStyle style) {
    final var result = new StringBuilder();
    for (String line : source.split(style.nlSequence())) {
      result.append(line.stripTrailing().replace(NL_PLACEHOLDER, "")).append(style.nlSequence());
    }
    return result.toString();
  } 
}
