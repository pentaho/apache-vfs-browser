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
