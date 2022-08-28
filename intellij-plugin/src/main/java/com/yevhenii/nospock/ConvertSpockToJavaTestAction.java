package com.yevhenii.nospock;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;

public class ConvertSpockToJavaTestAction extends AnAction {

  private final SingleFileConversionFlow singleFileConversionFlow = new SingleFileConversionFlow();
  private final DirConversionFlow dirConversionFlow = new DirConversionFlow();

  @Override
  public void update(@NotNull AnActionEvent event) {
    final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
    event.getPresentation().setEnabledAndVisible(
      event.getProject() != null
      && event.getData(CommonDataKeys.PSI_FILE) != null || psiElement instanceof PsiDirectory
    );
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
    final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
    if (psiFile != null) {
      singleFileConversionFlow.execute(psiFile);
    } else if (psiElement instanceof PsiDirectory) {
      dirConversionFlow.execute(((PsiDirectory) psiElement));
    } else {
      throw new IllegalStateException("NoSpock translation for not supported psi element: " + psiElement);
    }
  }
}
