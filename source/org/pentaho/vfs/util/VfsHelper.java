package org.pentaho.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;


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
        throw new FileSystemException("File does not exist.");
      }
      IOUtils.copy(is, savedFile.getContent().getOutputStream());
      return savedFile;
    }
    throw new FileSystemException("Operation failed.");
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
    throw new FileSystemException("Operation failed.");    
  }
  
  public FileObject getFileObject(String uri) throws FileSystemException {
    if (fsManager != null) {
      FileObject file = fsManager.resolveFile(uri);
      if (!file.exists()) {
        throw new FileSystemException("File does not exist.");
      }
      return file;
    }
    throw new FileSystemException("Operation failed.");    
  }
  
  public byte[] getFileContentAsByteArray(FileObject fileObject) throws IOException {
    if (fileObject != null && fileObject.exists()) {
      IOUtils.toByteArray(fileObject.getContent().getInputStream());
    }
    throw new FileSystemException("Operation failed.");    
  }
  
  public byte[] getFileContentAsByteArray(String uri) throws IOException {
    if (fsManager != null) {
      return getFileContentAsByteArray(fsManager.resolveFile(uri));
    }
    throw new FileSystemException("Operation failed.");
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
