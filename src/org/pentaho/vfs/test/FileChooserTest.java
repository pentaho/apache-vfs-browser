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

package org.pentaho.vfs.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.vfs.messages.Messages;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class FileChooserTest {
  public static void main(String args[]) {
    FileSystemManager fsManager = null;
    FileObject maybeRootFile = null;
    try {
      fsManager = VFS.getManager();
      if (fsManager instanceof DefaultFileSystemManager) {
        File f = new File("."); //$NON-NLS-1$
        try {
          ((DefaultFileSystemManager) fsManager).setBaseFile(f.getCanonicalFile());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //maybeRootFile = fsManager.resolveFile("jar:lib/mail-1.3.2.jar"); //$NON-NLS-1$
      // rootFile = fsManager.resolveFile("file:/home/mdamour/workspace/apache-vfs-browser");
       maybeRootFile = fsManager.resolveFile("file:///c:/");
      // maybeRootFile = fsManager.resolveFile("jar:lib/mail.jar");
      // maybeRootFile = fsManager.resolveFile("ftp://ftpgolden.pentaho.org/");

      // maybeRootFile.getFileSystem().getParentLayer().

      // maybeRootFile.getFileSystem().getFileSystemManager().gets

    } catch (Exception e) {
      e.printStackTrace();
    }
    final FileObject rootFile = maybeRootFile;
    final Shell applicationShell = new Shell(SWT.SHELL_TRIM | SWT.CLOSE | SWT.MIN | SWT.MAX);
    applicationShell.setLayout(new FillLayout());
    applicationShell.setText(Messages.getString("FileChooserTest.application")); //$NON-NLS-1$
    applicationShell.setSize(640, 400);
    Menu bar = new Menu(applicationShell, SWT.BAR);
    applicationShell.setMenuBar(bar);
    MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
    fileItem.setText(Messages.getString("FileChooserTest.file")); //$NON-NLS-1$
    fileItem.setAccelerator(SWT.CTRL + 'F');
    Menu fileSubMenu = new Menu(applicationShell, SWT.DROP_DOWN);
    fileItem.setMenu(fileSubMenu);
    MenuItem fileOpenItem = new MenuItem(fileSubMenu, SWT.CASCADE);
    fileOpenItem.setText(Messages.getString("FileChooserTest.open")); //$NON-NLS-1$
    fileOpenItem.setAccelerator(SWT.CTRL + 'O');
    final String filters[] = new String[] { "*.*", "*.xml;*.XML;", "*.class", "*.map" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    final String filterNames[] = new String[] {
        Messages.getString("FileChooserTest.allFiles"), Messages.getString("FileChooserTest.xmlFiles"), Messages.getString("FileChooserTest.javaFiles"), Messages.getString("FileChooserTest.mapFiles") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    fileOpenItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        FileObject initialFile = rootFile;
        // try {
        // // initialFile = rootFile.resolveFile("/home/mdamour");
        // } catch (FileSystemException e) {
        // e.printStackTrace();
        // }
        try {
          VfsFileChooserDialog fileOpenDialog = new VfsFileChooserDialog(applicationShell, VFS.getManager(), rootFile, initialFile);
          fileOpenDialog.addVFSUIPanel(buildHDFSPanel("HDFS", fileOpenDialog));
          fileOpenDialog.addVFSUIPanel(buildHDFSPanel("S3", fileOpenDialog));
          fileOpenDialog.addVFSUIPanel(buildHDFSPanel("file", fileOpenDialog));
          FileObject selectedFile = fileOpenDialog.open(applicationShell, null, filters, filterNames, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
          if (selectedFile != null) {
            System.out.println(Messages.getString("FileChooserTest.selectedFileEquals") + selectedFile.getName()); //$NON-NLS-1$
          } else {
            System.out.println(Messages.getString("FileChooserTest.noFileSelected")); //$NON-NLS-1$
          }
        } catch (FileSystemException ex) {
          ex.printStackTrace();
        }
      }
    });
    MenuItem saveAsOpenItem = new MenuItem(fileSubMenu, SWT.CASCADE);
    saveAsOpenItem.setText(Messages.getString("FileChooserTest.saveAs")); //$NON-NLS-1$
    saveAsOpenItem.setAccelerator(SWT.CTRL + 'A');
    saveAsOpenItem.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
      }

      public void widgetSelected(SelectionEvent arg0) {
        FileObject initialFile = null;
        try {
          initialFile = rootFile.resolveFile("/home/mdamour"); //$NON-NLS-1$
        } catch (FileSystemException e) {
          e.printStackTrace();
        }
        try {
          VfsFileChooserDialog fileOpenDialog = new VfsFileChooserDialog(applicationShell, VFS.getManager(), rootFile, initialFile);
          FileObject selectedFile = fileOpenDialog.open(applicationShell, 
              Messages.getString("FileChooserTest.untitled"), filters, filterNames, VfsFileChooserDialog.VFS_DIALOG_SAVEAS); //$NON-NLS-1$
          if (selectedFile != null) {
            System.out.println(Messages.getString("FileChooserTest.selectedFileEquals") + selectedFile.getName()); //$NON-NLS-1$
          } else {
            System.out.println(Messages.getString("FileChooserTest.noFileSelected")); //$NON-NLS-1$
          }
        } catch (FileSystemException ex) {
          ex.printStackTrace();
        }
      }
    });
    applicationShell.open();
    while (!applicationShell.isDisposed()) {
      if (!applicationShell.getDisplay().readAndDispatch())
        applicationShell.getDisplay().sleep();
    }
  }

  public static CustomVfsUiPanel buildHDFSPanel(final String scheme, final VfsFileChooserDialog parent) {
    CustomVfsUiPanel hdfsPanel = new CustomVfsUiPanel(scheme, scheme, parent, SWT.BORDER);
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    hdfsPanel.setLayoutData(gridData);
    hdfsPanel.setLayout(new GridLayout(1, false));

    Label fileNameLabel = new Label(hdfsPanel, SWT.NONE);
    fileNameLabel.setText("Some label: " + scheme); //$NON-NLS-1$
    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    fileNameLabel.setLayoutData(gridData);

    final Text fileName = new Text(hdfsPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fileName.setText(scheme + "://"); //$NON-NLS-1$
    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    fileName.setLayoutData(gridData);
    
    Button connectButton = new Button(hdfsPanel, SWT.PUSH);
    connectButton.setText("Connect"); //$NON-NLS-1$
    gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    gridData.widthHint = 90;
    connectButton.setLayoutData(gridData);
    connectButton.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent event) {
        parent.openFileCombo.setText(fileName.getText());
        parent.resolveVfsBrowser();
        //parent.vfsBrowser.resetVfsRoot(null);
      }

      public void widgetDefaultSelected(SelectionEvent event) {
      }
    });

    return hdfsPanel;
  }

}
