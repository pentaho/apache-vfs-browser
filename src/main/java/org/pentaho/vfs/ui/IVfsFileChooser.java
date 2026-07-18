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
import org.eclipse.swt.widgets.Shell;

public interface IVfsFileChooser {
	public FileObject open(Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode);
	public FileObject open(Shell applicationShell, FileObject defaultInitialFile, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode);
}
