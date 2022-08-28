package com.yevhenii.nospock;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CheckboxTree extends Tree {

  final CheckedFileMap fileMap;

  CheckboxTree(CheckedFileMap fileMap) {
    super(new CheckedDirTreeModel(fileMap));
    setCellRenderer(new CheckedDirCellTreeRenderer(fileMap));
    for (int i = 0; i < getRowCount(); i++) {
      expandRow(i);
    }
    this.fileMap = fileMap;
    addMouseListener(new MouseClickedListener());
  }

  private static class CheckedDirTreeModel implements TreeModel {

    final CheckedFileMap fileMap;

    private CheckedDirTreeModel(CheckedFileMap fileMap) {
      this.fileMap = fileMap;
    }

    @Override
    public Object getRoot() {
      return fileMap.root;
    }

    @Override
    public Object getChild(Object parent, int index) {
      return fileMap.el2children.get(parent)[index];
    }

    @Override
    public int getChildCount(Object parent) {
      if (parent instanceof PsiDirectory) {
        return fileMap.el2children.get(parent).length;
      }
      return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
      return node instanceof PsiFile;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
      return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
  }

  private static class CheckedDirCellTreeRenderer implements TreeCellRenderer {

    final CheckedFileMap fileMap;

    CheckedDirCellTreeRenderer(CheckedFileMap fileMap) {
      this.fileMap = fileMap;
    }

    @Override
    public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean selected,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus
    ) {
      return fileMap.el2box.get(value);
    }
  }

  private class MouseClickedListener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
      final Rectangle pathBounds = getPathBounds(getSelectionPath());
      if (pathBounds == null) {
        return;
      }

      // assuming this is roughly checkbox size
      final Rectangle bounds = pathBounds.getBounds();
      bounds.setSize(20, pathBounds.height);
      if (bounds.contains(e.getPoint())) {
        fileMap.checkOrUncheck((PsiElement) getLastSelectedPathComponent());
        updateUI();
      }
    }
  }
}
