package org.pentaho.vfs.ui;

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

}
