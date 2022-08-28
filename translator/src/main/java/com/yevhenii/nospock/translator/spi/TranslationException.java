package com.yevhenii.nospock.translator.spi;

/**
 * The root exception for lib errors.
 */
public class TranslationException extends RuntimeException {
  public TranslationException(String message) {
    super(message);
  }

  public TranslationException(Exception cause) {
    super(cause);
  }
}
