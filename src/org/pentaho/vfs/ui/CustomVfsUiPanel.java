package org.pentaho.vfs.ui;

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CustomVfsUiPanel extends Composite {

  private VfsFileChooserDialog vfsFileChooserDialog;
  private String vfsScheme;
  private String vfsSchemeDisplayText;

  public CustomVfsUiPanel(String vfsScheme, String vfsSchemeDisplayText, VfsFileChooserDialog vfsFileChooserDialog, int flags) {
    super(vfsFileChooserDialog.getCustomUIPanel(), flags);
    this.vfsFileChooserDialog = vfsFileChooserDialog;
    this.vfsScheme = vfsScheme;
    this.vfsSchemeDisplayText = vfsSchemeDisplayText;
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    setLayoutData(gridData);
    setLayout(new GridLayout(1, false));
  }

  public void activate() {
  }
  
  public VfsFileChooserDialog getVfsFileChooserDialog() {
    return vfsFileChooserDialog;
  }

  public void setVfsFileChooserDialog(VfsFileChooserDialog vfsFileChooserDialog) {
    this.vfsFileChooserDialog = vfsFileChooserDialog;
  }

  public void setVfsScheme(String vfsScheme) {
    this.vfsScheme = vfsScheme;
  }

  public String getVfsScheme() {
    return vfsScheme;
  }

  public String getVfsSchemeDisplayText() {
    return vfsSchemeDisplayText;
  }

  public void setVfsSchemeDisplayText(String vfsSchemeDisplayText) {
    this.vfsSchemeDisplayText = vfsSchemeDisplayText;
  }

  public FileObject resolveFile(String fileUri, FileSystemOptions opts) throws FileSystemException {
    FileSystem fs = null;
    if(vfsFileChooserDialog.rootFile != null) {
      fs = vfsFileChooserDialog.rootFile.getFileSystem();
    }
    if(fs != null) {
      if(opts == null) {
        return fs.getFileSystemManager().resolveFile(fileUri);
      } else {
        fs.getFileSystemManager().resolveFile(fileUri, opts);
      }
    }
    return null;
  }
  public FileObject resolveFile(String fileUri) throws FileSystemException {
    return resolveFile(fileUri, null);
  }

}
