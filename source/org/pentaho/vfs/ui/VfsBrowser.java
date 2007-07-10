package org.pentaho.vfs.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class VfsBrowser extends Composite {
  Tree fileSystemTree = null;

  FileObject rootFileObject = null;

  FileObject selectedFileObject = null;

  List eventListenerList = new ArrayList();

  boolean showFoldersOnly = false;

  boolean allowDoubleClickOpenFolder = false;

  String fileFilter = null;

  HashMap fileObjectChildrenMap = new HashMap();

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
    column1.setText("Name");
    column1.setWidth(260);
    final TreeColumn column2 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column2.setText("Type");
    column2.setWidth(120);
    final TreeColumn column3 = new TreeColumn(fileSystemTree, SWT.LEFT);
    column3.setText("Modified");
    column3.setWidth(120);

    parent.getShell().addControlListener(new ControlListener() {
      public void controlMoved(ControlEvent arg0) {
      }

      public void controlResized(ControlEvent arg0) {
        int treeWidth = fileSystemTree.getBounds().width;
        int remainderWidth = treeWidth - (column1.getWidth() + column2.getWidth() + column3.getWidth());
        column1.setWidth(column1.getWidth() + remainderWidth - 10);
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
          mb.setText("Error");
          mb.setMessage(e.getMessage());
          mb.open();
        }
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
        try {
          deleteItem(fileSystemTree.getSelection()[0]);
        } catch (FileSystemException e) {
          e.printStackTrace();
          MessageBox errorDialog = new MessageBox(fileSystemTree.getDisplay().getActiveShell(), SWT.YES | SWT.NO);
          errorDialog.setText("Error");
          errorDialog.setMessage(e.getMessage());
          errorDialog.open();
        }
      }
    });

    MenuItem renameFileItem = new MenuItem(popupMenu, SWT.PUSH);
    renameFileItem.setText("Rename File");
    renameFileItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        promptForRenameFile();
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
          mb.setText("Error:  Cannot select object");
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
        selectedFileObject = (FileObject) (ti.getData());
        if (ti.getData("isLoaded") == null || !((Boolean) ti.getData("isLoaded")).booleanValue()) {
          ti.removeAll();
          populateFileSystemTree(selectedFileObject, fileSystemTree, ti);
        }
        // if (!ti.getExpanded()) {
        // ti.setExpanded(true);
        // fireFileObjectSelected();
        // }
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
    if (fileSystemTree.getItemCount() > 0) {
      fileSystemTree.setSelection(fileSystemTree.getItem(0));
      fileSystemTree.getItem(0).setExpanded(true);
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
      TextInputDialog textDialog = new TextInputDialog("Enter new filename", text, 800, 100);
      text = textDialog.open();
      if (text != null && !"".equals(text)) {
        try {
          renameItem(fileSystemTree.getSelection()[0], text);
          done = true;
        } catch (FileSystemException e) {
          MessageBox errorDialog = new MessageBox(fileSystemTree.getDisplay().getActiveShell(), SWT.OK);
          errorDialog.setText("Error");
          errorDialog.setMessage(e.getMessage());
          errorDialog.open();
        }
      } else {
        done = true;
      }
    }
  }

  public boolean createFolder(String folderName) throws FileSystemException {
    FileObject newFolder = getSelectedFileObject().resolveFile(folderName);
    newFolder.createFolder();
    TreeItem newFolderTreeItem = new TreeItem(fileSystemTree.getSelection()[0], SWT.NONE);
    newFolderTreeItem.setData(newFolder);
    newFolderTreeItem.setData("isLoaded", Boolean.TRUE);
    newFolderTreeItem.setImage(new Image(newFolderTreeItem.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
    populateTreeItemText(newFolderTreeItem, newFolder);
    fileSystemTree.setSelection(newFolderTreeItem);
    return true;
  }

  public boolean deleteSelectedItem() throws FileSystemException {
    return deleteItem(fileSystemTree.getSelection()[0]);
  }

  public boolean deleteItem(TreeItem ti) throws FileSystemException {
    FileObject file = (FileObject) ti.getData();
    if (file.delete()) {
      ti.dispose();
      return true;
    }
    return false;
  }

  public boolean renameItem(TreeItem ti, String newName) throws FileSystemException {
    FileObject file = (FileObject) ti.getData();
    FileObject newFileObject = file.getParent().resolveFile(newName);
    if (!newFileObject.exists()) {
      newFileObject.createFile();
    } else {
      return false;
    }
    file.moveTo(newFileObject);
    ti.setText(newName);
    return true;
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
  }

  public void setFilter(String filter) {
    if (filter != null) {
      if (!filter.startsWith("*")) {
        filter = "*" + filter;
      }
      // we need to turn the filter into a proper regex
      // for example *.txt would be .*\.txt
      // and *.* would be .*\..*
      filter = filter.replaceAll("\\.", "\\.").replaceAll("\\*", ".*");
    }
    this.fileFilter = filter;
  }

  public void applyFilter() throws FileSystemException {
    // need to apply filter to entire tree (deletes nodes)
    FileObject selectedFileObject = (FileObject) fileSystemTree.getSelection()[0].getData();
    fileSystemTree.removeAll();
    populateFileSystemTree(rootFileObject, fileSystemTree, null);
    selectTreeItemByFileObject(selectedFileObject, true);
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
    TreeItem treeItem = fileSystemTree.getSelection()[0];
    treeItem.setExpanded(true);
    fileSystemTree.setSelection(treeItem);
    setSelectedFileObject(selectedFileObject);
    for (int i = selectedFileObjectParentList.size() - 1; i >= 0; i--) {
      FileObject obj = (FileObject) selectedFileObjectParentList.get(i);
      treeItem = findTreeItemByName(treeItem, obj.getName().getBaseName());
      if (treeItem != null) {
        if (treeItem.getData() == null || treeItem.getData("isLoaded") == null || !((Boolean) treeItem.getData("isLoaded")).booleanValue()) {
          treeItem.removeAll();
          populateFileSystemTree(obj, fileSystemTree, treeItem);
        }
        treeItem.setExpanded(expandSelection);
        fileSystemTree.setSelection(treeItem);
        setSelectedFileObject(obj);
        fireFileObjectSelected();
      }
    }
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
  }

  public void populateFileSystemTree(final FileObject inputFile, final Tree tree, TreeItem item) {
    if (inputFile == null) {
      return;
    }
    if (item == null) {
      item = new TreeItem(tree, SWT.NONE);
      item.setText(inputFile.getName().toString());
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
          if (children == null && inputFile.getType().hasChildren()) {
            children = inputFile.getChildren();
            fileObjectChildrenMap.put(inputFile.getName().getFriendlyURI(), children);
          }
        } catch (FileSystemException e) {
          e.printStackTrace();
        }
        myItem.setData("isLoaded", Boolean.TRUE);
        if (children != null) {
          myItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
        } else if (showFoldersOnly) {
          myItem.removeAll();
          myItem.dispose();
          return;
        }
        for (int i = 0; children != null && i < children.length; i++) {
          FileObject fileObj = children[i];
          TreeItem childTreeItem = new TreeItem(myItem, SWT.NONE);
          populateTreeItemText(childTreeItem, fileObj);
          childTreeItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/file.png")));
          childTreeItem.setData(fileObj);
          childTreeItem.setData("isLoaded", Boolean.FALSE);
          // try {
          FileObject[] myChildren = (FileObject[]) fileObjectChildrenMap.get(fileObj.getName().getFriendlyURI());
          try {
            if (myChildren == null && fileObj.getType().hasChildren()) {
              myChildren = fileObj.getChildren();
              fileObjectChildrenMap.put(fileObj.getName().getFriendlyURI(), myChildren);
              if (myChildren != null) {
                childTreeItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
                TreeItem tmpItem = new TreeItem(childTreeItem, SWT.NONE);
                populateTreeItemText(tmpItem, fileObj);
              } else if (showFoldersOnly) {
                childTreeItem.removeAll();
                childTreeItem.dispose();
              }
            } else if (fileObj.getType().equals(FileType.FOLDER)) {
              childTreeItem.setImage(new Image(tree.getDisplay(), getClass().getResourceAsStream("/icons/folder.gif")));
              TreeItem tmpItem = new TreeItem(childTreeItem, SWT.NONE);
              populateTreeItemText(tmpItem, fileObj);
            } else if (!fileObj.getType().equals(FileType.FOLDER) && !isAcceptedByFilter(childTreeItem)) {
              childTreeItem.removeAll();
              childTreeItem.dispose();
            }
          } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          // } catch (FileSystemException e) {
          // e.printStackTrace();
          // if (showFoldersOnly) {
          // childTreeItem.removeAll();
          // childTreeItem.dispose();
          // } else {
          // // well we know we found a real file, let's apply the filters
          // if (!isAcceptedByFilter(childTreeItem)) {
          // childTreeItem.removeAll();
          // childTreeItem.dispose();
          // }
          // }
          // }
        }
      }
    };
    BusyIndicator.showWhile(tree.getDisplay(), r);
  }

  public boolean isAcceptedByFilter(TreeItem treeItem) {
    if (fileFilter != null && !"".equals(fileFilter)) {
      StringTokenizer st = new StringTokenizer(fileFilter, ";");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (((FileObject) treeItem.getData()).getName().getFriendlyURI().matches(token)) {
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
}
