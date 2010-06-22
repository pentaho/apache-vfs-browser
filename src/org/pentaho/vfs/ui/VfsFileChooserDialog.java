/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @author Michael D'Amour
 */
package org.pentaho.vfs.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.vfs.messages.Messages;

public class VfsFileChooserDialog implements SelectionListener, VfsBrowserListener, IVfsFileChooser {

  public static final int VFS_DIALOG_OPEN_FILE = 0;

  public static final int VFS_DIALOG_OPEN_DIRECTORY = 1;

  public static final int VFS_DIALOG_OPEN_FILE_OR_DIRECTORY = 2;

  public static final int VFS_DIALOG_SAVEAS = 3;

  public FileObject rootFile;

  public FileObject initialFile;

  public Text fileNameText = null;

  public String enteredFileName = ""; //$NON-NLS-1$

  public Shell dialog = null;

  public boolean okPressed = false;

  public Button okButton = null;

  public Button cancelButton = null;

  // Button changeRootButton = null;

  public Button folderUpButton = null;

  public Button deleteFileButton = null;

  public Button newFolderButton = null;

  public Combo openFileCombo = null;

  public Combo fileFilterCombo = null;

  public int fileDialogMode = VFS_DIALOG_OPEN_FILE;

  public String[] fileFilters;

  public String[] fileFilterNames;

  public VfsBrowser vfsBrowser = null;

  public VfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
    this.rootFile = rootFile;
    this.initialFile = initialFile;
  }

  public FileObject open(Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode) {
    this.fileDialogMode = fileDialogMode;
    this.fileFilters = fileFilters;
    this.fileFilterNames = fileFilterNames;
    dialog = new Shell(applicationShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    if (fileDialogMode != VFS_DIALOG_SAVEAS) {
      dialog.setText(Messages.getString("VfsFileChooserDialog.openFile")); //$NON-NLS-1$
    } else {
      dialog.setText(Messages.getString("VfsFileChooserDialog.saveAs")); //$NON-NLS-1$
    }
    dialog.setLayout(new GridLayout());
    // create our file chooser tool bar, contains parent folder combo and various controls
    createToolbarPanel(dialog);
    // create our vfs browser component
    createVfsBrowser(dialog);
    if (fileDialogMode == VFS_DIALOG_SAVEAS) {
      createFileNamePanel(dialog, fileName);
    } else {
      // create file filter panel
      createFileFilterPanel(dialog);
    }
    // create our ok/cancel buttons
    createButtonPanel(dialog);

    // set the initial file selection
    try {
      vfsBrowser.selectTreeItemByFileObject(initialFile != null ? initialFile : rootFile, true);
      // vfsBrowser.setSelectedFileObject(initialFile);
      openFileCombo.setText(initialFile != null ? initialFile.getName().getFriendlyURI() : rootFile.getName().getFriendlyURI());
      updateParentFileCombo(initialFile != null ? initialFile : rootFile);
    } catch (FileSystemException e) {
      MessageBox box = new MessageBox(applicationShell);
      box.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
      box.setMessage(e.getMessage());
      box.open();
    }

    // set the size and show the dialog
    int height = 400;
    int width = 600;
    dialog.setSize(width, height);
    Rectangle bounds = dialog.getDisplay().getPrimaryMonitor().getClientArea();
    int x = (bounds.width - width) / 2;
    int y = (bounds.height - height) / 2;
    dialog.setLocation(x, y);
    dialog.open();

    if (rootFile != null && fileDialogMode == VFS_DIALOG_SAVEAS) {
      if (!rootFile.getFileSystem().hasCapability(Capability.WRITE_CONTENT)) {
        MessageBox messageDialog = new MessageBox(applicationShell, SWT.OK);
        messageDialog.setText(Messages.getString("VfsFileChooserDialog.warning")); //$NON-NLS-1$
        messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.noWriteSupport")); //$NON-NLS-1$
        messageDialog.open();
      }
    }

    vfsBrowser.fileSystemTree.forceFocus();
    while (!dialog.isDisposed()) {
      if (!dialog.getDisplay().readAndDispatch())
        dialog.getDisplay().sleep();
    }

    // we just woke up, we are probably disposed already..
    if (!dialog.isDisposed()) {
      dialog.dispose();
    }
    if (okPressed) {
      FileObject returnFile = vfsBrowser.getSelectedFileObject();
      if (returnFile != null && fileDialogMode == VFS_DIALOG_SAVEAS) {
        try {
          if (returnFile.getType().equals(FileType.FILE)) {
            returnFile = returnFile.getParent();
          }
          returnFile = returnFile.resolveFile(enteredFileName);
        } catch (FileSystemException e) {
          e.printStackTrace();
        }
      }
      return returnFile;
    } else {
      return null;
    }
  }

  public void createButtonPanel(Shell dialog) {
    Composite buttonPanel = new Composite(dialog, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    buttonPanel.setLayoutData(gridData);
    buttonPanel.setLayout(new GridLayout(4, false));

    String buttonAlign = System.getProperty("org.pentaho.di.buttonPosition", "right").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$

    if (!"left".equals(buttonAlign)) { //$NON-NLS-1$
      Label emptyLabel = new Label(buttonPanel, SWT.NONE);
      gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
      emptyLabel.setLayoutData(gridData);
    }
    okButton = new Button(buttonPanel, SWT.PUSH);
    okButton.setText(Messages.getString("VfsFileChooserDialog.ok")); //$NON-NLS-1$
    gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    gridData.widthHint = 90;
    okButton.setLayoutData(gridData);
    okButton.addSelectionListener(this);
    cancelButton = new Button(buttonPanel, SWT.PUSH);
    cancelButton.setText(Messages.getString("VfsFileChooserDialog.cancel")); //$NON-NLS-1$
    cancelButton.addSelectionListener(this);
    gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    gridData.widthHint = 90;
    cancelButton.setLayoutData(gridData);
    if ("center".equals(buttonAlign)) { //$NON-NLS-1$
      Label emptyLabel = new Label(buttonPanel, SWT.NONE);
      gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
      emptyLabel.setLayoutData(gridData);
    }
  }

  public void createFileFilterPanel(Shell dialog) {
    Composite filterPanel = new Composite(dialog, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    filterPanel.setLayoutData(gridData);
    filterPanel.setLayout(new GridLayout(3, false));
    // create filter label
    Label filterLabel = new Label(filterPanel, SWT.NONE);
    filterLabel.setText(Messages.getString("VfsFileChooserDialog.filter")); //$NON-NLS-1$
    gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
    filterLabel.setLayoutData(gridData);
    // create file filter combo
    fileFilterCombo = new Combo(filterPanel, SWT.READ_ONLY);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    fileFilterCombo.setLayoutData(gridData);
    fileFilterCombo.setItems(fileFilterNames);
    fileFilterCombo.addSelectionListener(this);
    fileFilterCombo.select(0);
  }

  public void createFileNamePanel(Shell dialog, String fileName) {
    Composite fileNamePanel = new Composite(dialog, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    fileNamePanel.setLayoutData(gridData);
    fileNamePanel.setLayout(new GridLayout(2, false));
    Label fileNameLabel = new Label(fileNamePanel, SWT.NONE);
    fileNameLabel.setText(Messages.getString("VfsFileChooserDialog.fileName")); //$NON-NLS-1$
    gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
    fileNameLabel.setLayoutData(gridData);
    fileNameText = new Text(fileNamePanel, SWT.BORDER);
    if (fileName != null) {
      fileNameText.setText(fileName);
    }
    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    fileNameText.setLayoutData(gridData);
    fileNameText.addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent arg0) {
      }

      public void keyReleased(KeyEvent event) {
        if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
          okPressed();
        }
      }

    });
  }

  public void createVfsBrowser(Shell dialog) {
    String defaultFilter = null;
    if (fileFilters != null && fileFilters.length > 0) {
      defaultFilter = fileFilters[0];
    }
    vfsBrowser = new VfsBrowser(dialog, SWT.NONE, rootFile, defaultFilter, fileDialogMode == VFS_DIALOG_SAVEAS ? true : false, false);
    // vfsBrowser.selectTreeItemByName(rootFile.getName().getFriendlyURI(), true);
    vfsBrowser.addVfsBrowserListener(this);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    vfsBrowser.setLayoutData(gridData);
  }

  public void createToolbarPanel(Shell dialog) {
    Composite chooserToolbarPanel = new Composite(dialog, SWT.NONE);
    chooserToolbarPanel.setLayout(new GridLayout(6, false));
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    chooserToolbarPanel.setLayoutData(gridData);

    // changeRootButton = new Button(chooserToolbarPanel, SWT.PUSH);
    // changeRootButton.setToolTipText(Messages.getString("VfsFileChooserDialog.changeVFSRoot")); //$NON-NLS-1$
    // changeRootButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream("/icons/network.gif"))); //$NON-NLS-1$
    // gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    // changeRootButton.setLayoutData(gridData);
    // changeRootButton.addSelectionListener(this);

    Label parentFoldersLabel = new Label(chooserToolbarPanel, SWT.NONE);
    if (fileDialogMode != VFS_DIALOG_SAVEAS) {
      parentFoldersLabel.setText(Messages.getString("VfsFileChooserDialog.openFromFolder")); //$NON-NLS-1$
    } else {
      parentFoldersLabel.setText(Messages.getString("VfsFileChooserDialog.saveInFolder")); //$NON-NLS-1$
    }
    gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
    parentFoldersLabel.setLayoutData(gridData);
    openFileCombo = new Combo(chooserToolbarPanel, SWT.BORDER);
    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    openFileCombo.setLayoutData(gridData);
    openFileCombo.addSelectionListener(this);
    openFileCombo.addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent event) {
    	// UP : 
    	//
        if ( (event.keyCode == SWT.ARROW_UP) && ((event.stateMask & SWT.CONTROL) == 0) && ((event.stateMask & SWT.ALT)==0) ) {
            resolveVfsBrowser();
            vfsBrowser.selectPreviousItem(); 
        }
          
        // DOWN:
        //
        if ( (event.keyCode == SWT.ARROW_DOWN) && ((event.stateMask & SWT.CONTROL) == 0) && ((event.stateMask & SWT.ALT)==0) ) {
            resolveVfsBrowser();
            vfsBrowser.selectNextItem();
        }
      }

      public void keyReleased(KeyEvent event) {
        if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
          try {
            FileObject newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(openFileCombo.getText());
            vfsBrowser.resetVfsRoot(newRoot);
          } catch (FileSystemException e) {
            MessageBox errorDialog = new MessageBox(vfsBrowser.getDisplay().getActiveShell(), SWT.OK);
            errorDialog.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
            errorDialog.setMessage(e.getMessage());
            errorDialog.open();
          }
        }
      }

    });
    folderUpButton = new Button(chooserToolbarPanel, SWT.PUSH);
    folderUpButton.setToolTipText(Messages.getString("VfsFileChooserDialog.upOneLevel")); //$NON-NLS-1$
    folderUpButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream("/icons/folderup.jpg"))); //$NON-NLS-1$
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    folderUpButton.setLayoutData(gridData);
    folderUpButton.addSelectionListener(this);
    deleteFileButton = new Button(chooserToolbarPanel, SWT.PUSH);
    deleteFileButton.setToolTipText(Messages.getString("VfsFileChooserDialog.deleteFile")); //$NON-NLS-1$
    deleteFileButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream("/icons/delete.jpg"))); //$NON-NLS-1$
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    deleteFileButton.setLayoutData(gridData);
    deleteFileButton.addSelectionListener(this);
    newFolderButton = new Button(chooserToolbarPanel, SWT.PUSH);
    newFolderButton.setToolTipText(Messages.getString("VfsFileChooserDialog.createNewFolder")); //$NON-NLS-1$
    newFolderButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream("/icons/newfolder.jpg"))); //$NON-NLS-1$
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    newFolderButton.setLayoutData(gridData);
    newFolderButton.addSelectionListener(this);
  }

  public void okPressed() {
    if (fileDialogMode == VFS_DIALOG_SAVEAS && "".equals(fileNameText.getText())) { //$NON-NLS-1$
      // do nothing, user did not enter a file name for saving
      MessageBox messageDialog = new MessageBox(dialog, SWT.OK);
      messageDialog.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
      messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.noFilenameEntered")); //$NON-NLS-1$
      messageDialog.open();
      return;
    }

    if (fileDialogMode == VFS_DIALOG_SAVEAS) {
      try {
        FileObject toBeSavedFile = vfsBrowser.getSelectedFileObject().resolveFile(fileNameText.getText());
        if (toBeSavedFile.exists()) {
          MessageBox messageDialog = new MessageBox(dialog, SWT.YES | SWT.NO);
          messageDialog.setText(Messages.getString("VfsFileChooserDialog.fileExists")); //$NON-NLS-1$
          messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.fileExistsOverwrite")); //$NON-NLS-1$
          int flag = messageDialog.open();
          if (flag == SWT.NO) {
            return;
          }
        }
      } catch (FileSystemException e) {
        e.printStackTrace();
      }
    }
    if (fileDialogMode == VFS_DIALOG_SAVEAS) {
      enteredFileName = fileNameText.getText();
    }

    try {
      if (fileDialogMode == VFS_DIALOG_OPEN_FILE && vfsBrowser.getSelectedFileObject().getType().equals(FileType.FOLDER)) {
        // try to open this node, it is a directory
        vfsBrowser.selectTreeItemByFileObject(vfsBrowser.getSelectedFileObject(), true);
        return;
      }
    } catch (FileSystemException e) {
    }

    okPressed = true;
    dialog.dispose();
  }

  public void widgetDefaultSelected(SelectionEvent se) {
  }

  public void widgetSelected(SelectionEvent se) {
    if (se.widget == openFileCombo) {
      // String filePath = parentFoldersCombo.getItem(parentFoldersCombo.getSelectionIndex());
      // vfsBrowser.selectTreeItemByName(filePath, true);

      try {
        FileObject newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(openFileCombo.getText());
        vfsBrowser.resetVfsRoot(newRoot);
      } catch (FileSystemException e) {
      }

    } else if (se.widget == okButton) {
      okPressed();
    } else if (se.widget == folderUpButton) {
      try {
        FileObject newRoot = vfsBrowser.getSelectedFileObject().getParent();
        if (newRoot != null) {
          vfsBrowser.resetVfsRoot(newRoot);
          vfsBrowser.setSelectedFileObject(newRoot);
          openFileCombo.setText(newRoot.getName().getFriendlyURI());
        }
      } catch (Exception e) {
        // top of root
      }
    } else if (se.widget == newFolderButton) {
      try {
        vfsBrowser.createFolder(Messages.getString("VfsFileChooserDialog.newFolder")); //$NON-NLS-1$
      } catch (FileSystemException e) {
        MessageBox mb = new MessageBox(newFolderButton.getShell());
        mb.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
        mb.setMessage(e.getMessage());
        mb.open();
      }
    } else if (se.widget == deleteFileButton) {
      MessageBox messageDialog = new MessageBox(se.widget.getDisplay().getActiveShell(), SWT.YES | SWT.NO);
      messageDialog.setText(Messages.getString("VfsFileChooserDialog.confirm")); //$NON-NLS-1$
      messageDialog.setMessage(Messages.getString("VfsFileChooserDialog.deleteFile") + vfsBrowser.getSelectedFileObject().getName().getFriendlyURI()); //$NON-NLS-1$
      int status = messageDialog.open();
      if (status == SWT.YES) {
        try {
          vfsBrowser.deleteSelectedItem();
        } catch (FileSystemException e) {
          MessageBox errorDialog = new MessageBox(se.widget.getDisplay().getActiveShell(), SWT.OK);
          errorDialog.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
          errorDialog.setMessage(e.getMessage() + "\n" + e.getCause().getMessage());
          errorDialog.open();
        }
      }
      // } else if (se.widget == changeRootButton) {
      // promptForNewVfsRoot();
    } else if (se.widget == fileFilterCombo) {

      Runnable r = new Runnable() {
        public void run() {
          String filter = fileFilters[fileFilterCombo.getSelectionIndex()];
          vfsBrowser.setFilter(filter);
          try {
            vfsBrowser.applyFilter();
          } catch (FileSystemException e) {
            MessageBox mb = new MessageBox(newFolderButton.getShell(), SWT.OK);
            mb.setText(Messages.getString("VfsFileChooserDialog.errorApplyFilter")); //$NON-NLS-1$
            mb.setMessage(e.getMessage());
            mb.open();
          }
        }
      };
      BusyIndicator.showWhile(fileFilterCombo.getDisplay(), r);
    } else {
      okPressed = false;
      dialog.dispose();
    }
  }

  public void promptForNewVfsRoot() {
    boolean done = false;
    String defaultText = vfsBrowser.rootFileObject.getName().getFriendlyURI();
    String text = defaultText;
    while (!done) {
      if (text == null) {
        text = defaultText;
      }
      File fileRoots[] = File.listRoots();
      String roots[] = new String[fileRoots.length];
      for (int i = 0; i < roots.length; i++) {
        try {
          roots[i] = fileRoots[i].toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
      ComboBoxInputDialog textDialog = new ComboBoxInputDialog(Messages.getString("VfsFileChooserDialog.enterNewVFSRoot"), text, roots, 650, 100); //$NON-NLS-1$
      text = textDialog.open();
      if (text != null && !"".equals(text)) { //$NON-NLS-1$
        try {
          vfsBrowser.resetVfsRoot(rootFile.getFileSystem().getFileSystemManager().resolveFile(text));
          done = true;
        } catch (FileSystemException e) {
          MessageBox errorDialog = new MessageBox(vfsBrowser.getDisplay().getActiveShell(), SWT.OK);
          errorDialog.setText(Messages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
          errorDialog.setMessage(e.getMessage());
          errorDialog.open();
        }
      } else {
        done = true;
      }
    }
  }

  public void updateParentFileCombo(FileObject selectedItem) {
    try {
      List parentChain = new ArrayList();
      // are we a directory?
      try {
        if (selectedItem.getChildren() != null) {
          // we have real children....
          parentChain.add(selectedItem);
        }
      } catch (Exception e) {
        // we are not a folder
      }
      FileObject parentFileObject;
      parentFileObject = selectedItem.getParent();
      while (parentFileObject != null) {
        parentChain.add(parentFileObject);
        parentFileObject = parentFileObject.getParent();
      }

      File roots[] = File.listRoots();
      for (int i = 0; i < roots.length; i++) {
        parentChain.add(selectedItem.getFileSystem().getFileSystemManager().resolveFile(roots[i].getAbsolutePath()));
      }

      String items[] = new String[parentChain.size()];
      int idx = 0;
      for (int i = parentChain.size() - 1; i >= 0; i--) {
        items[idx++] = ((FileObject) parentChain.get(i)).getName().getFriendlyURI();
      }
      openFileCombo.setItems(items);
      openFileCombo.select(items.length - 1);
    } catch (Exception e) {
      e.printStackTrace();
      // then let's not update the GUI
    }
  }

  public void fireFileObjectDoubleClicked(FileObject selectedItem) {
    if (fileDialogMode != VFS_DIALOG_SAVEAS) {
      // let's try drilling into the file as a new vfs root first

      String scheme = null;
      if (selectedItem.getName().getExtension().contains("jar")) {
        scheme = "jar:";
      } else if (selectedItem.getName().getExtension().contains("zip")) {
        scheme = "zip:";
      } else if (selectedItem.getName().getExtension().contains("gz")) {
        scheme = "gz:";
      } else if (selectedItem.getName().getExtension().contains("war")) {
        scheme = "war:";
      } else if (selectedItem.getName().getExtension().contains("ear")) {
        scheme = "ear:";
      } else if (selectedItem.getName().getExtension().contains("sar")) {
        scheme = "sar:";
      } else if (selectedItem.getName().getExtension().contains("tar")) {
        scheme = "tar:";
      } else if (selectedItem.getName().getExtension().contains("tbz2")) {
        scheme = "tbz2:";
      } else if (selectedItem.getName().getExtension().contains("tgz")) {
        scheme = "tgz:";
      } else if (selectedItem.getName().getExtension().contains("bz2")) {
        scheme = "bz2:";
      }

      if (scheme != null) {
        try {
          FileObject jarFileObject = selectedItem.getFileSystem().getFileSystemManager().resolveFile(scheme + selectedItem.getName().getFriendlyURI());
          vfsBrowser.resetVfsRoot(jarFileObject);
          updateParentFileCombo(jarFileObject);
          vfsBrowser.fileSystemTree.forceFocus();
        } catch (FileSystemException e) {
          e.printStackTrace();
          okPressed = true;
          dialog.dispose();
        }
      } else {
        okPressed = true;
        dialog.dispose();
      }

    } else {
      // anything?
    }
  }

  public void fireFileObjectSelected(FileObject selectedItem) {
    // something has just been selected, time to update the
    // parent file combo
    updateParentFileCombo(selectedItem);
    // openFileCombo.setText(selectedItem.getName().getFriendlyURI());
  }
  
  public void resolveVfsBrowser() {
      FileObject newRoot=null;
      try {
        newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(openFileCombo.getText());
      }
      catch(FileSystemException e) {
        // Ignore typo
      }
      if (newRoot!=null && !newRoot.equals(vfsBrowser.getRootFileObject())) {
        vfsBrowser.resetVfsRoot(newRoot);
      }
  }

}
