package org.pentaho.vfs.ui;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class VfsFileChooserDialog implements SelectionListener, VfsBrowserListener {
  public static final int VFS_DIALOG_OPEN = 0;

  public static final int VFS_DIALOG_SAVEAS = 1;

  FileObject rootFile;

  FileObject initialFile;

  Text fileNameText = null;

  String enteredFileName = "";

  Shell dialog = null;

  boolean okPressed = true;

  Button okButton = null;

  Button cancelButton = null;

  Button folderUpButton = null;

  Button deleteFileButton = null;

  Button newFolderButton = null;

  Combo parentFoldersCombo = null;

  Combo fileFilterCombo = null;

  int fileDialogMode = VFS_DIALOG_OPEN;

  String[] fileFilters;

  String[] fileFilterNames;

  VfsBrowser vfsBrowser = null;

  public VfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
    this.rootFile = rootFile;
    this.initialFile = initialFile;
  }

  public FileObject open(Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames,
      int fileDialogMode) {
    this.fileDialogMode = fileDialogMode;
    this.fileFilters = fileFilters;
    this.fileFilterNames = fileFilterNames;
    dialog = new Shell(applicationShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    if (fileDialogMode == VFS_DIALOG_OPEN) {
      dialog.setText("Open File");
    } else {
      dialog.setText("Save As");
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
    vfsBrowser.selectTreeItemByFileObject(initialFile, true);
    
    
    
    // set the size and show the dialog
    int height = 400;
    int width = 600;
    dialog.setSize(width, height);
    int x = (dialog.getDisplay().getBounds().width - width) / 2;
    int y = (dialog.getDisplay().getBounds().height - height) / 2;
    dialog.setLocation(x, y);
    dialog.open();
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
    buttonPanel.setLayout(new GridLayout(3, false));
    Label emptyLabel = new Label(buttonPanel, SWT.NONE);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    emptyLabel.setLayoutData(gridData);
    okButton = new Button(buttonPanel, SWT.PUSH);
    okButton.setText("Ok");
    gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    okButton.setLayoutData(gridData);
    okButton.addSelectionListener(this);
    cancelButton = new Button(buttonPanel, SWT.PUSH);
    cancelButton.setText("Cancel");
    cancelButton.addSelectionListener(this);
    gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    cancelButton.setLayoutData(gridData);
  }

  public void createFileFilterPanel(Shell dialog) {
    Composite filterPanel = new Composite(dialog, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    filterPanel.setLayoutData(gridData);
    filterPanel.setLayout(new GridLayout(3, false));
    // create filter label
    Label filterLabel = new Label(filterPanel, SWT.NONE);
    filterLabel.setText("Filter: ");
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
    fileNameLabel.setText("Filename:");
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
    vfsBrowser = new VfsBrowser(dialog, SWT.NONE, rootFile, defaultFilter, fileDialogMode == VFS_DIALOG_SAVEAS ? true
        : false);
    parentFoldersCombo.addSelectionListener(this);
    //vfsBrowser.selectTreeItemByName(rootFile.getName().getFriendlyURI(), true);
    updateParentFileCombo(rootFile);
    vfsBrowser.addVfsBrowserListener(this);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    vfsBrowser.setLayoutData(gridData);
  }

  public void createToolbarPanel(Shell dialog) {
    Composite chooserToolbarPanel = new Composite(dialog, SWT.NONE);
    chooserToolbarPanel.setLayout(new GridLayout(5, false));
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    chooserToolbarPanel.setLayoutData(gridData);
    Label parentFoldersLabel = new Label(chooserToolbarPanel, SWT.NONE);
    parentFoldersLabel.setText("Save in Folder:");
    gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
    parentFoldersLabel.setLayoutData(gridData);
    parentFoldersCombo = new Combo(chooserToolbarPanel, SWT.BORDER | SWT.READ_ONLY);
    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    parentFoldersCombo.setLayoutData(gridData);
    folderUpButton = new Button(chooserToolbarPanel, SWT.PUSH);
    folderUpButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream(
        "/icons/folderup.jpg")));
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    folderUpButton.setLayoutData(gridData);
    folderUpButton.addSelectionListener(this);
    deleteFileButton = new Button(chooserToolbarPanel, SWT.PUSH);
    deleteFileButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream(
        "/icons/delete.jpg")));
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    deleteFileButton.setLayoutData(gridData);
    deleteFileButton.addSelectionListener(this);
    newFolderButton = new Button(chooserToolbarPanel, SWT.PUSH);
    newFolderButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream(
        "/icons/newfolder.jpg")));
    gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    newFolderButton.setLayoutData(gridData);
    newFolderButton.addSelectionListener(this);
  }

  public void okPressed() {
    if (fileDialogMode == VFS_DIALOG_SAVEAS && "".equals(fileNameText.getText())) {
      // do nothing, user did not enter a file name for saving
      MessageBox messageDialog = new MessageBox(dialog, SWT.OK);
      messageDialog.setText("Error");
      messageDialog.setMessage("No filename was entered.");
      messageDialog.open();
      return;
    }
    
    if (fileDialogMode == VFS_DIALOG_SAVEAS) {
      try {
        FileObject toBeSavedFile = vfsBrowser.getSelectedFileObject().resolveFile(fileNameText.getText());
        if (toBeSavedFile.exists()) {
          MessageBox messageDialog = new MessageBox(dialog, SWT.YES | SWT.NO);
          messageDialog.setText("File Exists");
          messageDialog.setMessage("File exists, do you wish to overwrite?");
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
    okPressed = true;
    dialog.dispose();    
  }
  
  public void widgetDefaultSelected(SelectionEvent se) {
  }

  public void widgetSelected(SelectionEvent se) {
    if (se.widget == parentFoldersCombo) {
      String filePath = parentFoldersCombo.getItem(parentFoldersCombo.getSelectionIndex());
      vfsBrowser.selectTreeItemByName(filePath, true);
    } else if (se.widget == okButton) {
      okPressed();
    } else if (se.widget == folderUpButton) {
      if (parentFoldersCombo.getSelectionIndex() > 0) {
        String parentFolderText = parentFoldersCombo.getItem(parentFoldersCombo.getSelectionIndex() - 1);
        vfsBrowser.selectTreeItemByName(parentFolderText, true);
        parentFoldersCombo.select(parentFoldersCombo.getSelectionIndex() - 1);
      }
    } else if (se.widget == newFolderButton) {
      vfsBrowser.createFolder("New Folder");
    } else if (se.widget == deleteFileButton) {
      MessageBox messageDialog = new MessageBox(se.widget.getDisplay().getActiveShell(), SWT.YES | SWT.NO);
      messageDialog.setText("Confirm");
      messageDialog.setMessage("Delete file " + vfsBrowser.getSelectedFileObject().getName().getFriendlyURI());
      int status = messageDialog.open();
      if (status == SWT.YES) {
        System.out.println(vfsBrowser.deleteSelectedItem());
      }
    } else if (se.widget == fileFilterCombo) {
      Runnable r = new Runnable() {
        public void run() {
          String filter = fileFilters[fileFilterCombo.getSelectionIndex()];
          vfsBrowser.setFilter(filter);
          vfsBrowser.applyFilter();
        }
      };
      BusyIndicator.showWhile(fileFilterCombo.getDisplay(), r);
    } else {
      okPressed = false;
      dialog.dispose();
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
      String items[] = new String[parentChain.size()];
      int idx = 0;
      for (int i = parentChain.size() - 1; i >= 0; i--) {
        items[idx++] = ((FileObject) parentChain.get(i)).getName().getFriendlyURI();
      }
      parentFoldersCombo.setItems(items);
      parentFoldersCombo.select(items.length - 1);
    } catch (Exception e) {
      // then let's not update the GUI
    }
  }

  public void fireFileObjectDoubleClicked(FileObject selectedItem) {
    if (fileDialogMode == VFS_DIALOG_OPEN) {
      okPressed = true;
      dialog.dispose();
    } else {
      // anything?
    }
  }

  public void fireFileObjectSelected(FileObject selectedItem) {
    // something has just been selected, time to update the
    // parent file combo
    updateParentFileCombo(selectedItem);
  }
}
