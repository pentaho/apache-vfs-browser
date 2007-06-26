package org.pentaho.vfs.ui;

import org.apache.commons.vfs.FileObject;

public interface VfsBrowserListener {
  public void fireFileObjectDoubleClicked(FileObject selectedItem);
  public void fireFileObjectSelected(FileObject selectedItem);
}
