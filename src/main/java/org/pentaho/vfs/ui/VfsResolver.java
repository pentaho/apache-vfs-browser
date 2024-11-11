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

/**
 * @author Andrey Khayrutdinov
 */
public interface VfsResolver {

  FileObject resolveFile( String vfsUrl ) throws FileSystemException;

  FileObject resolveFile( String vfsUrl, FileSystemOptions fsOptions ) throws FileSystemException;
}
