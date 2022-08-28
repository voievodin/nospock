package com.yevhenii.nospock;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.nio.file.Files;
import java.nio.file.Path;

class PostConversion {

  static void refreshAndNavigateTo(Project project, Path outputDir) {
    if (Files.exists(outputDir)) {
      final VirtualFile virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(outputDir);
      if (virtualFile != null) {
        virtualFile.refresh(false, true);
        new OpenFileDescriptor(project, virtualFile).navigate(true);
      }
    }
  }
}
