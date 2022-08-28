package com.yevhenii.nospock.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "com.yevhenii.nospock.settings.NoSpockSettings",
  storages = @Storage("NoSpockSettingsPlugin.xml")
)
public class NoSpockSettings implements PersistentStateComponent<NoSpockSettings> {

  public String renameTestClassPatterns = "Spec -> Test, Specification -> Test";
  public String assertionsProvider = "junit";
  public String mocksProvider = "mockito";
  public String engineProvider = "junit";
  public String removeSuperclasses = "";
  public String spockLabelsPresenceMode = "PRESENT";
  public boolean assertionsUseStaticImports = true;
  public boolean mocksUseStaticImports = true;
  public String executableJarPath = "";
  public boolean textBlocksEnabled = true;

  public static NoSpockSettings getInstance() {
    return ApplicationManager.getApplication().getService(NoSpockSettings.class);
  }

  @Override
  public @Nullable NoSpockSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull NoSpockSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
