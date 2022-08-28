package com.yevhenii.nospock.translator;

import java.util.Objects;

/**
 * Configuration that can affect translation process. This configuration does not contain
 * configuration of specific spi providers, those providers shall be configured on their own.
 */
public class TranslationConfig {

  public enum SpockLabelPresenceMode {

    /**
     * Labels won't be present in generated code.
     */
    MISSING,

    /**
     * Every label will be present in generated code.
     */
    PRESENT,

    /**
     * Every label will be present only if at least one label has comment.
     */
    PRESENT_ONLY_WHEN_HAVE_COMMENTS
  }

  private SpockLabelPresenceMode spockLabelsPresenceMode = SpockLabelPresenceMode.MISSING;
  private boolean enableTextBlocks = true;

  public SpockLabelPresenceMode spockLabelsPresenceMode() {
    return spockLabelsPresenceMode;
  }

  public TranslationConfig spockLabelsPresenceMode(SpockLabelPresenceMode spockLabelsPresenceMode) {
    this.spockLabelsPresenceMode = Objects.requireNonNull(spockLabelsPresenceMode);
    return this;
  }

  public boolean enableTextBlocks() {
    return enableTextBlocks;
  }

  public TranslationConfig enableTextBlocks(boolean enableTextBlocks) {
    this.enableTextBlocks = enableTextBlocks;
    return this;
  }
}
