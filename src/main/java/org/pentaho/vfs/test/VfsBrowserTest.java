/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.vfs.test;

import java.io.File;
import java.io.IOException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.vfs.ui.VfsBrowser;

public class VfsBrowserTest {
  public static void main(String args[]) {
    FileSystemManager fsManager = null;
    FileObject rootFile = null;
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
      // rootFile = fsManager.resolveFile("jar:lib/jdom.jar2");
      // rootFile = fsManager.resolveFile("file:/home/mdamour/workspace/apache-vfs-browser");
      rootFile = fsManager.resolveFile("file:///"); //$NON-NLS-1$
    } catch (Exception e) {
      e.printStackTrace();
    }
    Shell s = new Shell();
    s.setLayout(new FillLayout());
    VfsBrowser browser = new VfsBrowser(s, SWT.MIN | SWT.MAX | SWT.CLOSE | SWT.RESIZE, rootFile, null, false, false);
    s.setVisible(true);
    while (!s.isDisposed()) {
      try {
        if (!s.getDisplay().readAndDispatch())
          s.getDisplay().sleep();
      } catch (SWTException e) {
      }
    }
  }
}
