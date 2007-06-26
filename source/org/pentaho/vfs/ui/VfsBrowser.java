package org.pentaho.vfs.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class VfsBrowser extends Composite {
  Tree fileSystemTree = null;

  FileObject rootFileObject = null;

  FileObject selectedFileObject = null;

  List eventListenerList = new ArrayList();

  boolean showFoldersOnly = false;

  String fileFilter = null;
  
  public VfsBrowser(final Composite parent, int style, final FileObject rootFileObject, String fileFilter, boolean showFoldersOnly) {
    super(parent, style);
    this.showFoldersOnly = showFoldersOnly;
    setFilter(fileFilter);
    setLayout(new FillLayout());
    this.rootFileObject = rootFileObject;
    fileSystemTree = new Tree(this, SWT.BORDER | SWT.SINGLE);
    fileSystemTree.setHeaderVisible(true);
    TreeColumn column1 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column1.setText("Name");
    column1.setWidth(260);
    TreeColumn column2 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column2.setText("Type");
    column2.setWidth(120);
    TreeColumn column3 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column3.setText("Modified");
    column3.setWidth(120);
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
        moveItem(fileSystemTree.getSelection()[0], (TreeItem) event.item);
      }
    });
    populateFileSystemTree(rootFileObject, fileSystemTree, null);
    final Menu popupMenu = new Menu(parent.getShell(), SWT.POP_UP);
    MenuItem deleteFileItem = new MenuItem(popupMenu, SWT.PUSH);
    deleteFileItem.setText("Delete File");
    deleteFileItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        deleteItem(fileSystemTree.getSelection()[0]);
      }
    });
    MenuItem renameFileItem = new MenuItem(popupMenu, SWT.PUSH);
    renameFileItem.setText("Rename File");
    renameFileItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        renameItem(fileSystemTree.getSelection()[0], "renamedfile");
      }
    });
    fileSystemTree.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        selectedFileObject = (FileObject) fileSystemTree.getSelection()[0].getData();
        fireFileObjectDoubleClicked();
      }

      public void mouseDown(MouseEvent arg0) {
        if (arg0.button == 3) {
          popupMenu.setVisible(true);
        }
      }

      public void mouseUp(MouseEvent arg0) {
        // TODO Auto-generated method stub
      }
    });
    fileSystemTree.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent e) {
        TreeItem ti = (TreeItem) e.item;
        selectedFileObject = (FileObject) (ti.getData());
        if (ti.getData("isLoaded") == null || !((Boolean) ti.getData("isLoaded")).booleanValue()) {
          ti.removeAll();
          populateFileSystemTree(selectedFileObject, fileSystemTree, ti);
        }
        ti.setExpanded(!ti.getExpanded());
        fireFileObjectSelected();
      }
    });
    fileSystemTree.addTreeListener(new TreeListener() {
      public void treeExpanded(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(new Image(parent.getDisplay(), getClass().getResourceAsStream("/icons/folderopen.gif")));
        if (ti.getData("isLoaded") == null || !((Boolean) ti.getData("isLoaded")).booleanValue()) {
          ti.removeAll();
          populateFileSystemTree((FileObject) ti.getData(), fileSystemTree, ti);
        }
      }

      public void treeCollapsed(TreeEvent e) {
        TreeItem ti = (TreeItem) e.item;
        ti.setImage(new Image(parent.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
      }
    });
    fileSystemTree.setSelection(fileSystemTree.getItem(0));
    fileSystemTree.getItem(0).setExpanded(true);
  }

  public boolean createFolder(String folderName) {
    try {
      FileObject newFolder = getSelectedFileObject().resolveFile(folderName);
      newFolder.createFolder();
      TreeItem newFolderTreeItem = new TreeItem(fileSystemTree.getSelection()[0], SWT.NONE);
      newFolderTreeItem.setData(newFolder);
      newFolderTreeItem.setData("isLoaded", Boolean.TRUE);
      newFolderTreeItem.setImage(new Image(newFolderTreeItem.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
      populateTreeItemText(newFolderTreeItem, newFolder);
      fileSystemTree.setSelection(newFolderTreeItem);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
  public boolean deleteSelectedItem() {
    return deleteItem(fileSystemTree.getSelection()[0]);
  }  
  
  public boolean deleteItem(TreeItem ti) {
    FileObject file = (FileObject) ti.getData();
    try {
      if (file.delete()) {
        ti.dispose();
        return true;
      }
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean renameItem(TreeItem ti, String newName) {
    FileObject file = (FileObject) ti.getData();
    try {
      FileObject newFileObject = file.getParent().resolveFile(newName);
      if (!newFileObject.exists()) {
        newFileObject.createFile();
      } else {
        return false;
      }
      file.moveTo(newFileObject);
      ti.setText(newName);
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean moveItem(TreeItem source, TreeItem destination) {
    try {
      FileObject file = (FileObject) source.getData();
      FileObject destFile = (FileObject) destination.getData();
      if (!file.exists() && !destFile.exists()) {
        return false;
      }
      try {
        if (destFile.getChildren() != null) {
          destFile = destFile.resolveFile(source.getText());
          if (!destFile.exists()) {
            destFile.createFile();
          }
        }
      } catch (Exception e) {
        destFile = destFile.getParent().resolveFile(source.getText());
        destination = destination.getParentItem();
        if (!destFile.exists()) {
          destFile.createFile();
        }
      }
      if (!file.getParent().equals(destFile.getParent())) {
        file.moveTo(destFile);
        TreeItem destTreeItem = new TreeItem(destination, SWT.NONE);
        destTreeItem.setImage(new Image(source.getDisplay(), getClass().getResourceAsStream("/icons/file.png")));
        destTreeItem.setData(destFile);
        destTreeItem.setData("isLoaded", Boolean.FALSE);
        populateTreeItemText(destTreeItem, destFile);
        source.dispose();
      }
      return true;
    } catch (FileSystemException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void setFilter(String filter) {
    if (filter != null) {
      if (filter.equals("*.*") || filter.equals("*")) {
        filter = "";
      }
    }
    this.fileFilter = filter;    
  }
  
  public void applyFilter() {
    // need to apply filter to entire tree (deletes nodes)
    fileSystemTree.removeAll();
    populateFileSystemTree(rootFileObject, fileSystemTree, null);
  }
  
  public void populateTreeItemText(TreeItem ti, FileObject fileObject) {
    try {
      String contentType = fileObject.getContent().getContentInfo().getContentType();
      DateFormat df = SimpleDateFormat.getDateTimeInstance();
      Date date = new Date(fileObject.getContent().getLastModifiedTime());
      if (contentType == null) {
        contentType = "";
      }
      ti.setText(new String[] { fileObject.getName().getBaseName(), contentType, df.format(date) });
    } catch (Throwable t) {
//      t.printStackTrace();
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

  public void populateFileSystemTree(FileObject inputFile, Tree tree, TreeItem item) {
    FileObject[] children = null;
    try {
      children = inputFile.getChildren();
    } catch (FileSystemException e) {
      // e.printStackTrace();
    }
    if (item == null) {
      item = new TreeItem(tree, SWT.NONE);
      item.setText(inputFile.getName().toString());
      item.setData(inputFile);
    } else {
      item.setData(inputFile);
    }
    item.setData("isLoaded", Boolean.TRUE);
    if (children != null) {
      item.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
    } else if (showFoldersOnly) {
      item.removeAll();
      item.dispose();
      return;
    }
    for (int i = 0; children != null && i < children.length; i++) {
      FileObject fileObj = children[i];
      TreeItem childTreeItem = new TreeItem(item, SWT.NONE);
      populateTreeItemText(childTreeItem, fileObj);
      childTreeItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/file.png")));
      childTreeItem.setData(fileObj);
      childTreeItem.setData("isLoaded", Boolean.FALSE);
      try {
        if (fileObj.getChildren() != null) {
          childTreeItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
          TreeItem tmpItem = new TreeItem(childTreeItem, SWT.NONE);
          populateTreeItemText(tmpItem, fileObj);
        } else if (showFoldersOnly) {
          childTreeItem.removeAll();
          childTreeItem.dispose();
        }
      } catch (FileSystemException e) {
        if (showFoldersOnly) {
          childTreeItem.removeAll();
          childTreeItem.dispose();
        } else {
          // well we know we found a real file, let's apply the filters
          if (!isAcceptedByFilter(childTreeItem)) {
            childTreeItem.removeAll();
            childTreeItem.dispose();
          }
        }
      }
    }
  }

  public boolean isAcceptedByFilter(TreeItem treeItem) {
    System.out.println("filter = " + fileFilter);
    if (fileFilter != null && !"".equals(fileFilter)) {
      StringTokenizer st = new StringTokenizer(fileFilter, ";");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (((FileObject)treeItem.getData()).getName().getFriendlyURI().endsWith(token)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }
  
  public TreeItem findTreeItemByName(TreeItem treeItem, String itemName) {
    if (treeItem.getData() != null && ((FileObject) treeItem.getData()).getName().getFriendlyURI().equals(itemName)) {
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
        setSelectedFileObject((FileObject)foundItem.getData());
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

}
