package com.yevhenii.nospock;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

class SingleFileConversionFlow {

  private static final Logger LOG = Logger.getLogger(SingleFileConversionFlow.class.getName());

  void execute(PsiFile psiFile) {
    final Conversion conversion;
    try {
      conversion = new Conversion(psiFile);
    } catch (ConversionException x) {
      LOG.info("Failed to convert psi file " + x.getMessage());
      NoSpockNotifications.conversionFailed(psiFile, x.getMessage());
      Messages.showErrorDialog(x.getMessage(), "Conversion Failed!");
      return;
    }

    final var confirmationDialog = SingleFileConversionConfirmation.create(conversion.outputDir());
    if (!confirmationDialog.showAndGet()) {
      return;
    }

    try {
      conversion.outputDir(new File(confirmationDialog.path.getText()).toPath());
      LOG.info("Command: " + conversion.commandStr());
      conversion.convert();
      NoSpockNotifications.conversionSuccessful(psiFile, conversion.outputDir());
    } catch (ConversionException conversionException) {
      NoSpockNotifications.conversionFailed(psiFile, conversionException.getMessage());
      Messages.showErrorDialog(conversionException.getMessage(), "Conversion Failed");
      return;
    }

    PostConversion.refreshAndNavigateTo(psiFile.getProject(), conversion.outputDir());
  }

  private static class SingleFileConversionConfirmation extends DialogWrapper {

    static SingleFileConversionConfirmation create(Path dir) {
      final var dialog = new SingleFileConversionConfirmation();
      dialog.path.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false, true, false, false, false, false)));
      dialog.path.setText(dir.toString());
      return dialog;
    }

    TextFieldWithBrowseButton path = new TextFieldWithBrowseButton();

    SingleFileConversionConfirmation() {
      super(true); // use current window as parent
      setTitle("Conversion Confirmation");
      init();
    }

    @Override
    protected JComponent createCenterPanel() {
      return FormBuilder.createFormBuilder()
        .addLabeledComponent(new JBLabel("Moving to package (will be created if does not exist):"), path, 1, true)
//        .addComponent(new JBCheckBox("Delete converted file", false))
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();
    }
  }
}
