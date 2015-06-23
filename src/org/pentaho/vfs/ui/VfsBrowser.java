/*
* Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
* 
* This software was developed by Pentaho Corporation and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package org.pentaho.vfs.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.AllFileSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.vfs.messages.Messages;

public class VfsBrowser extends Composite {
  private Image imgFolderOpen;
  private Image imgFolder;
  private Image imgFile;
  
  public Tree fileSystemTree = null;

  protected FileObject rootFileObject = null;

  protected FileObject selectedFileObject = null;

  protected List eventListenerList = new ArrayList();

  protected boolean showFoldersOnly = false;

  protected boolean allowDoubleClickOpenFolder = false;

  protected String fileFilter = null;

  protected HashMap fileObjectChildrenMap = new HashMap();

  public VfsBrowser(final Composite parent, int style, final FileObject rootFileObject, String fileFilter, final boolean showFoldersOnly,
      final boolean allowDoubleClickOpenFolder) {
    super(parent, style);
    this.showFoldersOnly = showFoldersOnly;
    this.allowDoubleClickOpenFolder = allowDoubleClickOpenFolder;
    setFilter(fileFilter);

    setLayout(new FillLayout());
    this.rootFileObject = rootFileObject;
    fileSystemTree = new Tree(this, SWT.BORDER | SWT.SINGLE);
    fileSystemTree.setHeaderVisible(true);

    final TreeColumn column1 = new TreeColumn(fileSystemTree, SWT.LEFT | SWT.RESIZE);
    column1.setText(Messages.getString("VfsBrowser.name")); //$NON-NLS-1$
    column1.setWidth(260);
    final TreeColumn column2 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column2.setText(Messages.getString("VfsBrowser.type")); //$NON-NLS-1$
    column2.setWidth(120);
    final TreeColumn column3 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column3.setText(Messages.getString("VfsBrowser.modified")); //$NON-NLS-1$
    column3.setWidth(180);

    parent.getShell().addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent arg0) {
      }

      public void controlResized(ControlEvent arg0) {
        int treeWidth = fileSystemTree.getBounds().width;
        if (treeWidth > column1.getWidth()) {
          int remainderWidth = treeWidth - (column1.getWidth() + column2.getWidth() + column3.getWidth());
          column1.setWidth(column1.getWidth() + remainderWidth - 10);
        }
      }
    });

    Transfer[] types = new Transfer[] { TextTransfer.getInstance(), FileTransfer.getInstance() };
    // Create the drag source on the tree
    DragSource ds = new DragSource(fileSystemTree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    ds.setTransfer(types);
    ds.addDragListener(new DragSourceAdapter() {
      public void dragSetData(DragSourceEvent event) {
        // Set the data to be the first selected item's text
        event.data = fileSystemTree.getSelection()[0].getText();
      }
    });
    DropTarget dt = new DropTarget(fileSystemTree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    dt.setTransfer(types);
    dt.addDropListener(new DropTargetAdapter() {
      public void drop(DropTargetEvent event) {
        try {
          moveItem(fileSystemTree.getSelection()[0], (TreeItem) event.item);
        } catch (FileSystemException e) {
          MessageBox mb = new MessageBox(parent.getShell());
          mb.setText(Messages.getString("VfsBrowser.error")); //$NON-NLS-1$
          mb.setMessage(e.getMessage());
          mb.open();
        }
      }
    });
    populateFileSystemTree(rootFileObject, fileSystemTree, null);
    final Menu popupMenu = new Menu(parent.getShell(), SWT.POP_UP);
    MenuItem deleteFileItem = new MenuItem(popupMenu, SWT.PUSH);
    deleteFileItem.setText(Messages.getString("VfsBrowser.deleteFile")); //$NON-NLS-1$
    deleteFileItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        try {
          MessageBox messageDialog = new MessageBox(getDisplay().getActiveShell(), SWT.YES | SWT.NO);
          messageDialog.setText(Messages.getString("VfsFileChooserDialog.confirm")); //$NON-NLS-1$
          messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.deleteFile")); //$NON-NLS-1$
          int status = messageDialog.open();
          if (status == SWT.YES) {
            deleteItem(fileSystemTree.getSelection()[0]);
          }
        } catch (FileSystemException e) {
          MessageBox errorDialog = new MessageBox(fileSystemTree.getDisplay().getActiveShell(), SWT.OK);
          errorDialog.setText(Messages.getString("VfsBrowser.error")); //$NON-NLS-1$
          errorDialog.setMessage(e.getMessage());
          errorDialog.open();
        }
      }
    });

    MenuItem renameFileItem = new MenuItem(popupMenu, SWT.PUSH);
    renameFileItem.setText(Messages.getString("VfsBrowser.renameFile")); //$NON-NLS-1$
    renameFileItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        promptForRenameFile();
      }
    });

    MenuItem separatorItem = new MenuItem(popupMenu, SWT.SEPARATOR);

    MenuItem refreshItem = new MenuItem(popupMenu, SWT.PUSH);
    refreshItem.setText(Messages.getString("VfsBrowser.refresh")); //$NON-NLS-1$
    refreshItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        fileObjectChildrenMap.clear();
        try {
          selectedFileObject.refresh();
          applyFilter();
        } catch (FileSystemException e) {
          e.printStackTrace();
        }
      }
    });

    fileSystemTree.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        TreeItem ti = fileSystemTree.getSelection()[0];
        selectedFileObject = (FileObject) ti.getData();
        try {
          if (allowDoubleClickOpenFolder || selectedFileObject.getType().equals(FileType.FILE)) {
            fireFileObjectDoubleClicked();
          } else {
            ti.setExpanded(!ti.getExpanded());
            fireFileObjectSelected();
          }
        } catch (FileSystemException ex) {
          // this simply means that we don't know if the selected file was a file or a folder, likely, we don't have permission
          MessageBox mb = new MessageBox(parent.getShell());
          mb.setText(Messages.getString("VfsBrowser.cannotSelectObject")); //$NON-NLS-1$
          mb.setMessage(ex.getMessage());
          mb.open();
        }
      }

      public void mouseDown(MouseEvent arg0) {
        if (arg0.button == 3) {
          popupMenu.setVisible(true);
        } else {
        }
      }

      public void mouseUp(MouseEvent arg0) {
      }
    });
    fileSystemTree.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent e) {
        // TreeItem ti = fileSystemTree.getSelection()[0];
        TreeItem ti = (TreeItem) e.item;
        if(ti != null) {
          selectedFileObject = (FileObject) (ti.getData());
          if (ti.getData("isLoaded") == null || !((Boolean) ti.getData("isLoaded")).booleanValue()) { //$NON-NLS-1$ //$NON-NLS-2$
            ti.removeAll();
            populateFileSystemTree(selectedFileObject, fileSystemTree, ti);
          }
          // if (!ti.getExpanded()) {
          // ti.setExpanded(true);
          fireFileObjectSelected();
        }
        // }
      }
    });
    fileSystemTree.addTreeListener(new TreeListener() {
      public void treeExpanded(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(getFolderOpenImage(parent.getDisplay()));
        if (ti.getData("isLoaded") == null || !((Boolean) ti.getData("isLoaded")).booleanValue()) { //$NON-NLS-1$ //$NON-NLS-2$
          ti.removeAll();
          populateFileSystemTree((FileObject) ti.getData(), fileSystemTree, ti);
        }
        fireFileObjectSelected();
      }

      public void treeCollapsed(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(getFolderImage(parent.getDisplay()));
      }
    });
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent arg0) {
        disposeImages();
      }
    });
  }

  protected void disposeImages() {
    if (imgFolderOpen != null && !imgFolderOpen.isDisposed()) {
      imgFolderOpen.dispose();
    }
    if (imgFolder != null && !imgFolder.isDisposed()) {
      imgFolder.dispose();
    }
    if (imgFile != null && !imgFile.isDisposed()) {
      imgFile.dispose();
    }
  }

  public void promptForRenameFile() {
    boolean done = false;
    String defaultText = fileSystemTree.getSelection()[0].getText();
    String text = defaultText;
    while (!done) {
      if (text == null) {
        text = defaultText;
      }
      TextInputDialog textDialog = new TextInputDialog(Messages.getString("VfsBrowser.enterNewFilename"), text, 500, 100); //$NON-NLS-1$
      text = textDialog.open();
      if (text != null && !"".equals(text)) { //$NON-NLS-1$
        try {
          done = renameItem(fileSystemTree.getSelection()[0], text);
          if (!done) {
            MessageBox errorDialog = new MessageBox(fileSystemTree.getDisplay().getActiveShell(), SWT.OK);
            errorDialog.setText(Messages.getString("VfsBrowser.error")); //$NON-NLS-1$
            errorDialog.setMessage("Could not rename selection, target exists or operation not supported.");
            errorDialog.open();
          }
        } catch (FileSystemException e) {
          MessageBox errorDialog = new MessageBox(fileSystemTree.getDisplay().getActiveShell(), SWT.OK);
          errorDialog.setText(Messages.getString("VfsBrowser.error")); //$NON-NLS-1$
          if (e.getCause() != null) {
            errorDialog.setMessage(e.getCause().getMessage());
          } else {
            errorDialog.setMessage(e.getMessage());
          }
          errorDialog.open();
        }
      } else {
        done = true;
      }
    }
  }

  public boolean createFolder(String folderName) throws FileSystemException {
    FileObject newFolder = getSelectedFileObject().resolveFile(folderName);
    if (newFolder.exists()) {
      throw new FileSystemException("vfs.provider/create-folder.error", folderName);
    }
    newFolder.createFolder();
    TreeItem newFolderTreeItem = new TreeItem(fileSystemTree.getSelection()[0], SWT.NONE);
    newFolderTreeItem.setData(newFolder);
    newFolderTreeItem.setData("isLoaded", Boolean.TRUE); //$NON-NLS-1$
    newFolderTreeItem.setImage(getFolderImage(newFolderTreeItem.getDisplay()));
    populateTreeItemText(newFolderTreeItem, newFolder);
    fileSystemTree.setSelection(newFolderTreeItem);
    setSelectedFileObject(newFolder);
    fireFileObjectSelected();
    return true;
  }

  public boolean deleteSelectedItem() throws FileSystemException {
    return deleteItem(fileSystemTree.getSelection()[0]);
  }

  public boolean deleteItem(TreeItem ti) throws FileSystemException {
    FileObject file = (FileObject) ti.getData();
    if (file.getName().getPath().equals("/")) return false; // If the root folder is attempted to delete, do nothing.
    if (file.delete()) {
      ti.dispose();
      return true;
    }
    // If deleting a file object failed and no exception was kicked in, the selected object is a non-empty folder.
    // Show a second Confirm message, and perform a recursive delete if OK is pressed.
    MessageBox messageDialog = new MessageBox(fileSystemTree.getShell(), SWT.YES | SWT.NO);
    messageDialog.setText(Messages.getString("VfsFileChooserDialog.confirm")); //$NON-NLS-1$
    messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.deleteFolderWithContents")); //$NON-NLS-1$
    int status = messageDialog.open();
    if (status == SWT.YES) {
      if (file.delete(new AllFileSelector()) != 0){
        ti.dispose();
        return true;
      }
    }
    return false;
  }

  public boolean renameItem(TreeItem ti, String newName) throws FileSystemException {
    FileObject file = (FileObject) ti.getData();
    FileObject newFileObject = file.getParent().resolveFile(newName);

    if (file.canRenameTo(newFileObject)) {
      if (!newFileObject.exists()) {
        newFileObject.createFile();
      } else {
        return false;
      }
      file.moveTo(newFileObject);
      ti.setText(newName);
      ti.setData(newFileObject);
      return true;
    } else {
      return false;
    }

  }

  public boolean moveItem(TreeItem source, TreeItem destination) throws FileSystemException {
    FileObject file = (FileObject) source.getData();
    FileObject destFile = (FileObject) destination.getData();
    if (!file.exists() && !destFile.exists()) {
      return false;
    }
    try {
      if (destFile.getChildren() != null) {
        destFile = destFile.resolveFile(source.getText());
      }
    } catch (Exception e) {
      destFile = destFile.getParent().resolveFile(source.getText());
      destination = destination.getParentItem();
    }
    if (!file.getParent().equals(destFile.getParent())) {
      file.moveTo(destFile);
      TreeItem destTreeItem = new TreeItem(destination, SWT.NONE);
      destTreeItem.setImage(getFileImage(source.getDisplay()));
      destTreeItem.setData(destFile);
      destTreeItem.setData("isLoaded", Boolean.FALSE); //$NON-NLS-1$
      populateTreeItemText(destTreeItem, destFile);
      source.dispose();
    }
    return true;
  }

  public void setFilter(String filter) {
    if (filter != null) {
      if (!filter.startsWith("*")) { //$NON-NLS-1$
        filter = "*" + filter; //$NON-NLS-1$
      }
      // we need to turn the filter into a proper regex
      // for example *.txt would be .*\.txt
      // and *.* would be .*\..*
      filter = filter.replaceAll("\\.", "\\.").replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    this.fileFilter = filter;
  }

  public void applyFilter() throws FileSystemException {
    // need to apply filter to entire tree (deletes nodes)
    if (fileSystemTree.getSelection() != null && fileSystemTree.getSelection().length > 0) {
      FileObject selectedFileObject = (FileObject) fileSystemTree.getSelection()[0].getData();
      fileSystemTree.removeAll();
      populateFileSystemTree(rootFileObject, fileSystemTree, null);
      selectTreeItemByFileObject(selectedFileObject, true);
    }
  }

  public void selectTreeItemByFileObject(FileObject selectedFileObject, boolean expandSelection) throws FileSystemException {
    // note that this method WILL cause the tree to load files from VFS
    // go through selectedFileObject's parent elements until we hit the root
    if (selectedFileObject == null) {
      return;
    }
    List selectedFileObjectParentList = new ArrayList();
    selectedFileObjectParentList.add(selectedFileObject);
    FileObject parent = selectedFileObject.getParent();
    while (parent != null && !parent.equals(rootFileObject)) {
      selectedFileObjectParentList.add(parent);
      parent = parent.getParent();
    }

    if (fileSystemTree.getSelection().length > 0) {
      TreeItem treeItem = fileSystemTree.getSelection()[0];
      treeItem.setExpanded(true);
      fileSystemTree.setSelection(treeItem);
      setSelectedFileObject(selectedFileObject);
      for (int i = selectedFileObjectParentList.size() - 1; i >= 0; i--) {
        FileObject obj = (FileObject) selectedFileObjectParentList.get(i);
        treeItem = findTreeItemByName(treeItem, obj.getName().getBaseName());
        if (treeItem != null && !treeItem.isDisposed()) {
          if (treeItem.getData() == null || treeItem.getData("isLoaded") == null || !((Boolean) treeItem.getData("isLoaded")).booleanValue()) { //$NON-NLS-1$ //$NON-NLS-2$
            treeItem.removeAll();
            populateFileSystemTree(obj, fileSystemTree, treeItem);
          }
        }
        if (treeItem != null && !treeItem.isDisposed()) {
          fileSystemTree.setSelection(treeItem);
          treeItem.setExpanded(expandSelection);
        }
      }
    }
  }

  public void populateTreeItemText(TreeItem ti, FileObject fileObject) {
    try {
      String contentType = fileObject.getContent().getContentInfo().getContentType();
      DateFormat df = SimpleDateFormat.getDateTimeInstance();
      Date date = new Date(fileObject.getContent().getLastModifiedTime());
      if (contentType == null) {
        contentType = ""; //$NON-NLS-1$
      }
      ti.setText(new String[] { fileObject.getName().getBaseName(), contentType, df.format(date) });
    } catch (Throwable t) {
      // t.printStackTrace();
      ti.setText(fileObject.getName().getBaseName());
    }
  }

  public boolean setContent(TreeItem ti, byte[] data) {
    FileObject file = (FileObject) ti.getData();
    try {
      OutputStream os = file.getContent().getOutputStream();
      os.write(data);
      os.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void resetVfsRoot(final FileObject newRoot) {
    rootFileObject = newRoot;
    fileSystemTree.removeAll();
    populateFileSystemTree(newRoot, fileSystemTree, null);
    try {
      selectTreeItemByFileObject(newRoot, true);
    } catch (FileSystemException e) {
    }
    if (fileSystemTree.getItemCount() > 0 && fileSystemTree.getSelection().length == 0) {
      fileSystemTree.setSelection(fileSystemTree.getItem(0));
      fileSystemTree.getItem(0).setExpanded(true);
    }
  }

  public void populateFileSystemTree(final FileObject inputFile, final Tree tree, TreeItem item) {
    if (inputFile == null) {
      return;
    }
    if (item == null) {
      item = new TreeItem(tree, SWT.NONE);
      String rootName = inputFile.getName().getBaseName();
      if (rootName == null || "".equals(rootName)) {
        rootName = "/";
      }
      item.setText(rootName);
      item.setData(inputFile);
      item.setExpanded(true);
      tree.setSelection(item);
    } else {
      item.setData(inputFile);
    }
    final TreeItem myItem = item;
    Runnable r = new Runnable() {
      public void run() {
        FileObject[] children = null;
        try {
          children = (FileObject[]) fileObjectChildrenMap.get(inputFile.getName().getFriendlyURI());
          if (children == null && inputFile.getType().equals(FileType.FOLDER)) {
            children = inputFile.getChildren();
            if (children == null) {
              children = new FileObject[0];
            }
            Arrays.sort(children, new Comparator<FileObject>() {
              public int compare(FileObject o1, FileObject o2) {
                try {
                  if (o1.getType().equals(o2.getType())) {
                    return o1.getName().getBaseName().compareTo(o2.getName().getBaseName());
                  }
                  if (o1.getType().equals(FileType.FOLDER)) {
                    return -1;
                  }
                  if (o1.getType().equals(FileType.FILE)) {
                    return 1;
                  }
                } catch (Exception e) {
                }
                return 0;
              }

              public boolean equals(Object obj) {
                return super.equals(obj);
              }
            });
            fileObjectChildrenMap.put(inputFile.getName().getFriendlyURI(), children);
          }
        } catch (FileSystemException e) {
          e.printStackTrace();
        }
        myItem.setData("isLoaded", Boolean.TRUE); //$NON-NLS-1$
        if (children != null) {
          myItem.setImage(getFolderImage(tree.getDisplay()));
        } else if (showFoldersOnly) {
          myItem.removeAll();
          myItem.dispose();
          return;
        }
        for (int i = 0; children != null && i < children.length; i++) {
          FileObject fileObj = children[i];
          try {
            if (fileObj.getType().hasChildren()) {
              TreeItem childTreeItem = new TreeItem(myItem, SWT.NONE);
              populateTreeItemText(childTreeItem, fileObj);
              childTreeItem.setImage(getFileImage(tree.getDisplay()));
              childTreeItem.setData(fileObj);
              childTreeItem.setData("isLoaded", Boolean.FALSE); //$NON-NLS-1$
              childTreeItem.setImage(getFolderImage(tree.getDisplay()));
              TreeItem tmpItem = new TreeItem(childTreeItem, SWT.NONE);
              populateTreeItemText(tmpItem, fileObj);
            } else if (fileObj.getType().equals(FileType.FOLDER)) {
              TreeItem childTreeItem = new TreeItem(myItem, SWT.NONE);
              populateTreeItemText(childTreeItem, fileObj);
              childTreeItem.setImage(getFileImage(tree.getDisplay()));
              childTreeItem.setData(fileObj);
              childTreeItem.setData("isLoaded", Boolean.FALSE); //$NON-NLS-1$
              childTreeItem.setImage(getFolderImage(tree.getDisplay()));
              TreeItem tmpItem = new TreeItem(childTreeItem, SWT.NONE);
              populateTreeItemText(tmpItem, fileObj);
            } else if (!fileObj.getType().equals(FileType.FOLDER) && !showFoldersOnly) {
              if (isAcceptedByFilter(fileObj.getName())) {
                TreeItem childTreeItem = new TreeItem(myItem, SWT.NONE);
                populateTreeItemText(childTreeItem, fileObj);
                childTreeItem.setImage(getFileImage(tree.getDisplay()));
                childTreeItem.setData(fileObj);
                childTreeItem.setData("isLoaded", Boolean.FALSE); //$NON-NLS-1$
              }
            }
          } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        if (fileSystemTree.getItemCount() > 0 && fileSystemTree.getSelection().length == 0) {
          fileSystemTree.setSelection(fileSystemTree.getItem(0));
          fileSystemTree.getItem(0).setExpanded(true);
        }
      }
    };
    BusyIndicator.showWhile(tree.getDisplay(), r);
  }

  public boolean isAcceptedByFilter(TreeItem treeItem) {
    return isAcceptedByFilter(((FileObject) treeItem.getData()).getName());
  }

  public boolean isAcceptedByFilter(FileName fileName) {
    if (fileFilter != null && !"".equals(fileFilter)) { //$NON-NLS-1$
      StringTokenizer st = new StringTokenizer(fileFilter, ";"); //$NON-NLS-1$
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (fileName.getFriendlyURI().matches(token)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  public TreeItem findTreeItemByName(TreeItem treeItem, String itemName) {
    if (treeItem == null
        || (treeItem.getData() != null && (((FileObject) treeItem.getData()).getName().getBaseName().equals(itemName) || ((FileObject) treeItem.getData())
            .getName().getFriendlyURI().equals(itemName)))) {
      return treeItem;
    }
    TreeItem children[] = treeItem.getItems();
    for (int i = 0; children != null && i < children.length; i++) {
      TreeItem foundItem = findTreeItemByName(children[i], itemName);
      if (foundItem != null) {
        return foundItem;
      }
    }
    return null;
  }

  public void selectTreeItemByName(String itemName, boolean expandSelectedItem) {
    // search only the tree as we know it, do NOT load anything, as
    // this can result in a huge performance hit
    // the idea here is to allow someone to select (from history) a node
    // that has already been loaded
    TreeItem children[] = fileSystemTree.getItems();
    for (int i = 0; children != null && i < children.length; i++) {
      TreeItem foundItem = findTreeItemByName(children[i], itemName);
      if (foundItem != null) {
        // ok we found it
        // select it, and return, we're done
        // expand our parents
        TreeItem parent = foundItem.getParentItem();
        while (parent != null) {
          parent.setExpanded(true);
          parent = parent.getParentItem();
        }
        foundItem.setExpanded(expandSelectedItem);
        setSelectedFileObject((FileObject) foundItem.getData());
        fileSystemTree.setSelection(foundItem);
        return;
      }
    }
  }

  public FileObject getSelectedFileObject() {
    return selectedFileObject;
  }

  public void setSelectedFileObject(FileObject selectedFileObject) {
    this.selectedFileObject = selectedFileObject;
  }

  public void addVfsBrowserListener(VfsBrowserListener listener) {
    eventListenerList.add(listener);
  }

  public void removeVfsBrowserListener(VfsBrowserListener listener) {
    eventListenerList.remove(listener);
  }

  public void fireFileObjectDoubleClicked() {
    for (int i = 0; i < eventListenerList.size(); i++) {
      VfsBrowserListener listener = (VfsBrowserListener) eventListenerList.get(i);
      listener.fireFileObjectDoubleClicked(getSelectedFileObject());
    }
  }

  public void fireFileObjectSelected() {
    for (int i = 0; i < eventListenerList.size(); i++) {
      VfsBrowserListener listener = (VfsBrowserListener) eventListenerList.get(i);
      listener.fireFileObjectSelected(getSelectedFileObject());
    }
  }

  public void selectNextItem() {
    fileSystemTree.setFocus();
    // TODO: move one down
  }

  public void selectPreviousItem() {
    fileSystemTree.setFocus();
    // TODO: move one up;
  }

  public FileObject getRootFileObject() {
    return rootFileObject;
  }
  
  private Image getFolderOpenImage(Display display) {
    if (imgFolderOpen == null || imgFolderOpen.isDisposed()) {
      imgFolderOpen = new Image(display, getClass().getResourceAsStream("/icons/folderopen.gif"));
    }
    return imgFolderOpen;
  }
  
  private Image getFolderImage(Display display) {
    if (imgFolder == null || imgFolder.isDisposed()) {
      imgFolder = new Image(display, getClass().getResourceAsStream("/icons/folder.gif"));
    }
    return imgFolder;
  }
  
  private Image getFileImage(Display display) {
    if (imgFile == null || imgFile.isDisposed()) {
      imgFile = new Image(display, getClass().getResourceAsStream("/icons/file.png"));
    }
    return imgFile;
  }
}
