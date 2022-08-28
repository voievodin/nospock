package com.yevhenii.nospock;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.psi.PsiFile;

import java.nio.file.Path;

class NoSpockNotifications {

  private static final String GROUP = "nospock";

  static void conversionSuccessful(PsiFile psiFile, Path outputDir) {
    NotificationGroupManager.getInstance()
      .getNotificationGroup(GROUP)
      .createNotification(
        String.format(
          "Conversion Successful!\nFile '%s'\nOut Dir: '%s'",
          psiFile.getVirtualFile().toNioPath(),
          outputDir
        ),
        NotificationType.INFORMATION
      )
      .notify(psiFile.getProject());
  }

  static void conversionFailed(PsiFile psiFile, String reason) {
    NotificationGroupManager.getInstance()
      .getNotificationGroup(GROUP)
      .createNotification("Conversion Failed!\nFile: " + psiFile.getVirtualFile().toNioPath() + "\nReason: " + reason, NotificationType.ERROR)
      .notify(psiFile.getProject());
  }
}
