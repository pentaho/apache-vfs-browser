/*
* Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
* 
* This software was developed by Pentaho Corporation and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
