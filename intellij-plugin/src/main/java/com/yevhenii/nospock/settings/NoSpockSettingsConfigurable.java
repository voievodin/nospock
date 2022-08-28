package com.yevhenii.nospock.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class NoSpockSettingsConfigurable implements Configurable {

  private NoSpockSettingsComponent settingsComponent;

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return "NoSpock settings";
  }

  @Override
  public @Nullable JComponent createComponent() {
    settingsComponent = new NoSpockSettingsComponent();
    return settingsComponent.panel();
  }

  @Override
  public boolean isModified() {
    final NoSpockSettings settings = NoSpockSettings.getInstance();
    boolean modified = settings.assertionsUseStaticImports != settingsComponent.assertionsUseStaticImports();
    modified |= settings.mocksUseStaticImports != settingsComponent.mocksUseStaticImports();
    modified |= !Objects.equals(settings.renameTestClassPatterns, settingsComponent.renameTestClassPatterns());
    modified |= !Objects.equals(settings.engineProvider, settingsComponent.engineProvider());
    modified |= !Objects.equals(settings.assertionsProvider, settingsComponent.assertionsProvider());
    modified |= !Objects.equals(settings.mocksProvider, settingsComponent.mocksProvider());
    modified |= !Objects.equals(settings.executableJarPath, settingsComponent.executableJarPath());
    modified |= !Objects.equals(settings.removeSuperclasses, settingsComponent.removeSuperclasses());
    modified |= !Objects.equals(settings.spockLabelsPresenceMode, settingsComponent.spockLabelsPresenceMode());
    modified |= !Objects.equals(settings.textBlocksEnabled, settingsComponent.textBlocksEnabled());
    return modified;
  }

  @Override
  public void apply() {
    final NoSpockSettings settings = NoSpockSettings.getInstance();
    settings.assertionsUseStaticImports = settingsComponent.assertionsUseStaticImports();
    settings.mocksUseStaticImports = settingsComponent.mocksUseStaticImports();
    settings.renameTestClassPatterns = settingsComponent.renameTestClassPatterns();
    settings.engineProvider = settingsComponent.engineProvider();
    settings.assertionsProvider = settingsComponent.assertionsProvider();
    settings.mocksProvider = settingsComponent.mocksProvider();
    settings.executableJarPath = settingsComponent.executableJarPath();
    settings.removeSuperclasses = settingsComponent.removeSuperclasses();
    settings.spockLabelsPresenceMode = settingsComponent.spockLabelsPresenceMode();
  }

  @Override
  public void reset() {
    final NoSpockSettings settings = NoSpockSettings.getInstance();
    settingsComponent.assertionsUseStaticImports(settings.assertionsUseStaticImports);
    settingsComponent.mocksUseStaticImports(settings.mocksUseStaticImports);
    settingsComponent.renameTestClassPatterns(settings.renameTestClassPatterns);
    settingsComponent.engineProvider(List.of("junit"), settings.engineProvider);
    settingsComponent.assertionsProvider(List.of("junit"), settings.assertionsProvider);
    settingsComponent.mocksProvider(List.of("mockito"), settings.mocksProvider);
    settingsComponent.executableJarPath(settings.executableJarPath);
    settingsComponent.removeSuperclasses(settings.removeSuperclasses);
    settingsComponent.spockLabelsPresenceMode(
      List.of("MISSING", "PRESENT", "PRESENT_ONLY_WHEN_HAVE_COMMENTS"),
      settings.spockLabelsPresenceMode
    );
    settingsComponent.textBlocksEnabled(settings.textBlocksEnabled);
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }
}
