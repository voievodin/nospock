package com.yevhenii.nospock.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.util.List;

public class NoSpockSettingsComponent {

  private final JPanel panel;
  private final JBTextField testClassRenamePatterns = new JBTextField();
  private final JBTextField removeSuperclasses = new JBTextField();
  private final JComboBox<String> engineProvider = new JComboBox<>();
  private final JComboBox<String> assertionsProvider = new JComboBox<>();
  private final JComboBox<String> mocksProvider = new JComboBox<>();
  private final JComboBox<String> spockLabelsPresenceMode = new JComboBox<>();
  private final TextFieldWithBrowseButton executableJarPath = new TextFieldWithBrowseButton();
  private final JBCheckBox staticImportsForAssertions = new JBCheckBox("Use static imports for assertions");
  private final JBCheckBox staticImportsForMocks = new JBCheckBox("Use static imports for mocks");
  private final JBCheckBox textBlocksEnabled = new JBCheckBox("Whether to convert long multiline strings to text blocks \"\"\"...\"\"\"");

  public NoSpockSettingsComponent() {
    executableJarPath.addBrowseFolderListener(
      new TextBrowseFolderListener(new FileChooserDescriptor(false, false, true, true, false, false)));
    executableJarPath.setText("");
    panel = FormBuilder.createFormBuilder()
      .addLabeledComponent(new JBLabel("Executable jar path"), executableJarPath)
      .addLabeledComponent(new JBLabel("Test class rename patterns: "), testClassRenamePatterns, 1, false)
      .addLabeledComponent(new JBLabel("Remove superclasses: "), removeSuperclasses, 1, false)
      .addLabeledComponent(new JBLabel("Engine provider used"), engineProvider)
      .addLabeledComponent(new JBLabel("Assertions provider used"), assertionsProvider)
      .addLabeledComponent(new JBLabel("Mocks provider"), mocksProvider)
      .addLabeledComponent(new JBLabel("Spock labels presence mode"), spockLabelsPresenceMode)
      .addComponent(staticImportsForAssertions, 1)
      .addComponent(staticImportsForMocks, 1)
      .addComponent(textBlocksEnabled, 1)
      .addComponentFillVertically(new JPanel(), 0)
      .getPanel();
  }

  public JPanel panel() {
    return panel;
  }

  public boolean assertionsUseStaticImports() {
    return staticImportsForAssertions.isSelected();
  }

  public void assertionsUseStaticImports(boolean value) {
    staticImportsForAssertions.setSelected(value);
  }

  public boolean mocksUseStaticImports() {
    return staticImportsForMocks.isSelected();
  }

  public void mocksUseStaticImports(boolean value) {
    staticImportsForMocks.setSelected(value);
  }

  public String engineProvider() {
    return engineProvider.getSelectedItem().toString();
  }

  public void engineProvider(List<String> providers, String selected) {
    for (String provider : providers) {
      engineProvider.addItem(provider);
    }
    engineProvider.setSelectedItem(selected);
  }

  public String assertionsProvider() {
    return assertionsProvider.getSelectedItem().toString();
  }

  public void assertionsProvider(List<String> providers, String selected) {
    for (String provider : providers) {
      assertionsProvider.addItem(provider);
    }
    engineProvider.setSelectedItem(selected);
  }

  public String mocksProvider() {
    return mocksProvider.getSelectedItem().toString();
  }

  public void mocksProvider(List<String> providers, String selected) {
    for (String provider : providers) {
      mocksProvider.addItem(provider);
    }
    engineProvider.setSelectedItem(selected);
  }

  public String renameTestClassPatterns() {
    return testClassRenamePatterns.getText();
  }

  public void renameTestClassPatterns(String patterns) {
    testClassRenamePatterns.setText(patterns);
  }

  public String executableJarPath() {
    return executableJarPath.getText();
  }

  public void executableJarPath(String path) {
    executableJarPath.setText(path);
  }

  public void removeSuperclasses(String removeSuperclasses) {
    this.removeSuperclasses.setText(removeSuperclasses);
  }

  public String removeSuperclasses() {
    return removeSuperclasses.getText();
  }

  public void spockLabelsPresenceMode(List<String> modes, String selectedMode) {
    for (String possibleMode : modes) {
      spockLabelsPresenceMode.addItem(possibleMode);
    }
    this.spockLabelsPresenceMode.setSelectedItem(selectedMode);
  }

  public String spockLabelsPresenceMode() {
    return (String) spockLabelsPresenceMode.getSelectedItem();
  }
  
  public boolean textBlocksEnabled() {
    return textBlocksEnabled.isSelected();
  }
  
  public void textBlocksEnabled(boolean value) {
    textBlocksEnabled.setSelected(value);
  }
}
