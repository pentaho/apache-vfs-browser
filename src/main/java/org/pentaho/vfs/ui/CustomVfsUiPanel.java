/*
* Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
* 
* This software was developed by Hitachi Vantara and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
