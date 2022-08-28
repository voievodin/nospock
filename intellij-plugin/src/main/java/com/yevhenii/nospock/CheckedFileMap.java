package com.yevhenii.nospock;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBCheckBox;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

class CheckedFileMap {
  final PsiDirectory root;
  final IdentityHashMap<PsiElement, JBCheckBox> el2box = new IdentityHashMap<>();
  final IdentityHashMap<PsiElement, PsiElement[]> el2children = new IdentityHashMap<>();

  CheckedFileMap(PsiDirectory psiDirectory) {
    this.root = psiDirectory;
    map(psiDirectory);
  }

  List<PsiFile> selected() {
    final List<PsiFile> files = new ArrayList<>();
    for (Map.Entry<PsiElement, JBCheckBox> entry : el2box.entrySet()) {
      if (entry.getKey() instanceof PsiFile && entry.getValue().isSelected()) {
        files.add((PsiFile) entry.getKey());
      }
    }
    return files;
  }

  void checkOrUncheck(PsiElement element) {
    JBCheckBox box = el2box.get(element);
    setSelected(element, !box.isSelected());
  }

  private void setSelected(PsiElement psiElement, boolean selected) {
    el2box.get(psiElement).setSelected(selected);
    PsiElement[] psiElements = el2children.get(psiElement);
    if (psiElements != null) {
      for (PsiElement element : psiElements) {
        setSelected(element, selected);
      }
    }
  }

  private void map(PsiDirectory psiDirectory) {
    el2box.put(psiDirectory, new JBCheckBox(psiDirectory.getName(), true));
    el2children.put(psiDirectory, psiDirectory.getChildren());
    for (PsiElement child : psiDirectory.getChildren()) {
      if (child instanceof PsiDirectory) {
        map((PsiDirectory) child);
      } else if (child instanceof PsiFile) {
        final PsiFile psiFile = (PsiFile) child;
        if ("groovy".equals(psiFile.getVirtualFile().getExtension())) {
          el2box.put(child, new JBCheckBox(psiFile.getName(), true));
        }
      }
    }
  }
}
