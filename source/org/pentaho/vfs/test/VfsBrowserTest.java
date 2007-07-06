package org.pentaho.vfs.test;

import java.io.File;
import java.io.IOException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
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
        File f = new File(".");
        try {
          ((DefaultFileSystemManager) fsManager).setBaseFile(f.getCanonicalFile());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // rootFile = fsManager.resolveFile("jar:lib/jdom.jar2");
      // rootFile = fsManager.resolveFile("file:/home/mdamour/workspace/apache-vfs-browser");
      rootFile = fsManager.resolveFile("file:/");
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
