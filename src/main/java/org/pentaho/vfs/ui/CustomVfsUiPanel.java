/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.vfs.ui;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class CustomVfsUiPanel extends Composite {

  private VfsFileChooserDialog vfsFileChooserDialog;
  private String vfsScheme;
  private String vfsSchemeDisplayText;

  public CustomVfsUiPanel( String vfsScheme, String vfsSchemeDisplayText, VfsFileChooserDialog vfsFileChooserDialog,
                           int flags ) {
    super( vfsFileChooserDialog.getCustomUIPanel(), flags );
    this.vfsFileChooserDialog = vfsFileChooserDialog;
    this.vfsScheme = vfsScheme;
    this.vfsSchemeDisplayText = vfsSchemeDisplayText;
    GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    setLayoutData( gridData );
    setLayout( new GridLayout( 1, false ) );
  }

  public void activate() {
  }

  public VfsFileChooserDialog getVfsFileChooserDialog() {
    return vfsFileChooserDialog;
  }

  public void setVfsFileChooserDialog( VfsFileChooserDialog vfsFileChooserDialog ) {
    this.vfsFileChooserDialog = vfsFileChooserDialog;
  }

  public void setVfsScheme( String vfsScheme ) {
    this.vfsScheme = vfsScheme;
  }

  public String getVfsScheme() {
    return vfsScheme;
  }

  public String getVfsSchemeDisplayText() {
    return vfsSchemeDisplayText;
  }

  public void setVfsSchemeDisplayText( String vfsSchemeDisplayText ) {
    this.vfsSchemeDisplayText = vfsSchemeDisplayText;
  }

  public FileObject resolveFile( String fileUri, FileSystemOptions opts ) throws FileSystemException {
    return ( vfsFileChooserDialog == null ) ? null : vfsFileChooserDialog.resolver.resolveFile( fileUri, opts );
  }

  public FileObject resolveFile( String fileUri ) throws FileSystemException {
    return resolveFile( fileUri, null );
  }

}
