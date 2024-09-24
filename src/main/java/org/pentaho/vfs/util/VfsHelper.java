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

package org.pentaho.vfs.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.pentaho.vfs.messages.Messages;


public class VfsHelper {

  private FileSystemManager fsManager = null;
  private FileObject rootFile = null;
  
  public VfsHelper(FileSystemManager fsManager, String root) throws FileSystemException {
    this.fsManager = fsManager;
    rootFile = fsManager.resolveFile( root );
  }

  public FileObject saveFile(String uri, InputStream is) throws IOException {
    if (fsManager != null) {
      FileObject savedFile = fsManager.resolveFile(uri);
      if (!savedFile.exists()) {
        throw new FileSystemException(Messages.getString("VfsHelper.fileDoesNotExist")); //$NON-NLS-1$
      }
      IOUtils.copy(is, savedFile.getContent().getOutputStream());
      return savedFile;
    }
    throw new FileSystemException(Messages.getString("VfsHelper.operationFailed")); //$NON-NLS-1$
  }
  
  public FileObject saveFileAs(String uri, InputStream is) throws FileSystemException, IOException {
    if (fsManager != null) {
      FileObject savedFile = fsManager.resolveFile(uri);
      if (!savedFile.exists()) {
        savedFile.createFile();
      }
      IOUtils.copy(is, savedFile.getContent().getOutputStream());
      return savedFile;
    }
    throw new FileSystemException(Messages.getString("VfsHelper.operationFailed"));     //$NON-NLS-1$
  }
  
  public FileObject getFileObject(String uri) throws FileSystemException {
    if (fsManager != null) {
      FileObject file = fsManager.resolveFile(uri);
      if (!file.exists()) {
        throw new FileSystemException(Messages.getString("VfsHelper.fileDoesNotExist")); //$NON-NLS-1$
      }
      return file;
    }
    throw new FileSystemException(Messages.getString("VfsHelper.operationFailed"));     //$NON-NLS-1$
  }
  
  public byte[] getFileContentAsByteArray(FileObject fileObject) throws IOException {
    if (fileObject != null && fileObject.exists()) {
      IOUtils.toByteArray(fileObject.getContent().getInputStream());
    }
    throw new FileSystemException(Messages.getString("VfsHelper.operationFailed"));     //$NON-NLS-1$
  }
  
  public byte[] getFileContentAsByteArray(String uri) throws IOException {
    if (fsManager != null) {
      return getFileContentAsByteArray(fsManager.resolveFile(uri));
    }
    throw new FileSystemException(Messages.getString("VfsHelper.operationFailed")); //$NON-NLS-1$
  }
  
  public FileSystemManager getFsManager() {
    return fsManager;
  }

  
  public void setFsManager(FileSystemManager fsManager) {
    this.fsManager = fsManager;
  }

  
  public FileObject getRootFile() {
    return rootFile;
  }

  
  public void setRootFile(FileObject rootFile) {
    this.rootFile = rootFile;
  }

  
}
