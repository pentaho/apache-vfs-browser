/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.vfs.ui;

import org.apache.commons.vfs2.FileObject;

public interface VfsBrowserListener {
  public void fireFileObjectDoubleClicked(FileObject selectedItem);
  public void fireFileObjectSelected(FileObject selectedItem);
}
