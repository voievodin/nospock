package com.yevhenii.nospock;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;

import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

class DirConversionFlow {

  private static final Logger LOG = Logger.getLogger(DirConversionFlow.class.getName());

  void execute(PsiDirectory psiDir) {
    ProgressManager.getInstance().run(new Task.Backgroundable(psiDir.getProject(), "Directory conversion") {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        executeInABackgroundThread(psiDir, indicator);
      }
    });
  }

  private void executeInABackgroundThread(PsiDirectory psiDir, ProgressIndicator indicator) {
    LOG.info("Converting groovy files in dir tree: " + psiDir.getVirtualFile().toNioPath());

    indicator.setIndeterminate(false);
    indicator.setFraction(0);

    final List<PsiFile> selectedFiles = confirmSelectedFiles(psiDir);
    if (selectedFiles.isEmpty()) {
      return;
    }

    final var stats = new ConversionStats();
    Path firstSuccessfullyConvertedOutDir = null;
    for (int i = 0; i < selectedFiles.size(); i++) {
      final PsiFile file = selectedFiles.get(i);
      indicator.setText("Converting file: " + file.getName());
      LOG.info("Converting file: " + file.getName());

      try {
        final var conversion = new Conversion(file);
        conversion.convert();

        NoSpockNotifications.conversionSuccessful(file, conversion.outputDir());
        stats.successful++;
        if (firstSuccessfullyConvertedOutDir == null) {
          firstSuccessfullyConvertedOutDir = conversion.outputDir();
        }
      } catch (ConversionException x) {
        NoSpockNotifications.conversionFailed(file, x.getMessage());
        stats.failed++;
      }

      indicator.setFraction(((double) i + 1) / selectedFiles.size());
    }

    indicator.stop();

    if (stats.failed == 0) {
      ApplicationManager.getApplication().invokeLater(
        () -> Messages.showInfoMessage(
          "All files (" + stats.successful + ") successfully converted", 
          "Conversion Successful"
        )
      );
    } else if (stats.successful == 0) {
      ApplicationManager.getApplication().invokeLater(
        () -> Messages.showErrorDialog(
          "Failed to convert all selected files (" + stats.failed + "), check event log for details", 
          "Conversion Result"
        )
      );
    } else {
      ApplicationManager.getApplication().invokeLater(
        () -> Messages.showWarningDialog(
          String.format(
            "Failed to convert some of selected files. Check event log for details (successful: %d, failed: %d)",
            stats.successful,
            stats.failed
          ), 
          "Conversion Result"
        )
      );
    }

    if (firstSuccessfullyConvertedOutDir != null) {
      final Path navigateTo = firstSuccessfullyConvertedOutDir;
      ApplicationManager.getApplication().invokeLater(
        () -> PostConversion.refreshAndNavigateTo(psiDir.getProject(), navigateTo)
      );
    }
  }

  private static List<PsiFile> confirmSelectedFiles(PsiDirectory psiDir) {
    final AtomicReference<List<PsiFile>> selectedFiles = new AtomicReference<>();
    final CountDownLatch selectedFilesLatch = new CountDownLatch(1);

    ApplicationManager.getApplication().invokeLater(() -> {
      final DirConversionConfirmationDialog dialog = new DirConversionConfirmationDialog(psiDir);
      if (dialog.showAndGet()) {
        selectedFiles.set(dialog.selected());
      } else {
        selectedFiles.set(List.of());
      }
      selectedFilesLatch.countDown();
    });


    try {
      selectedFilesLatch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return List.of();
    }

    return selectedFiles.get();
  }

  // obviously could look nicer
  private static class DirConversionConfirmationDialog extends DialogWrapper {

    final CheckedFileMap fileMap;
    final JPanel panel;

    DirConversionConfirmationDialog(PsiDirectory baseDir) {
      super(true);
      setTitle("Conversion Confirmation");
      this.fileMap = new CheckedFileMap(baseDir);
      this.panel = new JPanel();
      this.panel.add(new JBScrollPane(new CheckboxTree(fileMap)));
      init();
    }

    List<PsiFile> selected() {
      return fileMap.selected();
    }

    @Override
    protected JComponent createCenterPanel() {
      return panel;
    }
  }

  private static class ConversionStats {
    int successful = 0;
    int failed = 0;
  }
}
