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


package org.pentaho.vfs.ui;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * @author Andrey Khayrutdinov
 */
public class DelegatingResolver implements VfsResolver {

  private final FileSystemManager fsm;

  public DelegatingResolver( FileSystemManager fsm ) {
    if ( fsm == null ) {
      throw new NullPointerException( "A FileSystemManager is required" );
    }
    this.fsm = fsm;
  }

  @Override
  public FileObject resolveFile( String vfsUrl ) throws FileSystemException {
    return fsm.resolveFile( vfsUrl );
  }

  @Override
  public FileObject resolveFile( String vfsUrl, FileSystemOptions fsOptions ) throws FileSystemException {
    return fsm.resolveFile( vfsUrl, fsOptions );
  }
}
