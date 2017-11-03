/*
* Copyright 2002 - 2018 Hitachi Vantara.  All rights reserved.
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

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.URLFileName;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.vfs.messages.Messages;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class VfsFileChooserDialog implements SelectionListener, MouseListener, VfsBrowserListener, IVfsFileChooser {

  private Image imgFolderUp;
  private Image imgDelete;
  private Image imgNewFolder;

  public static final int VFS_DIALOG_OPEN_FILE = 0;

  public static final int VFS_DIALOG_OPEN_DIRECTORY = 1;

  public static final int VFS_DIALOG_OPEN_FILE_OR_DIRECTORY = 2;

  public static final int VFS_DIALOG_SAVEAS = 3;

  private static final String  FILE_SCHEME = "file";

  private static final String MAPRFS_SCHEME = "maprfs";

  public FileObject rootFile;

  public FileObject initialFile;

  private FileObject selectedFile;
  private CustomVfsUiPanel currentPanel;

  public Text fileNameText = null;

  public String enteredFileName = ""; //$NON-NLS-1$

  public Shell dialog = null;
  Shell applicationShell = null;

  public boolean okPressed = false;

  public Button okButton = null;

  public Button cancelButton = null;

  // Button changeRootButton = null;

  public Label folderUpButton = null;

  public Label deleteFileButton = null;

  public Label newFolderButton = null;

  public Combo openFileCombo = null;

  public Combo fileFilterCombo = null;

  public int fileDialogMode = VFS_DIALOG_OPEN_FILE;

  public String[] fileFilters;

  public String[] fileFilterNames;

  public VfsBrowser vfsBrowser = null;

  public FileObject defaultInitialFile = null;

  public Composite customUIPanel;

  Map<Integer, CustomVfsUiPanel> customUIPanelsOrderedMap = new TreeMap<Integer, CustomVfsUiPanel>();

  Composite comboPanel;
  Combo customUIPicker;
  Shell fakeShell = new Shell();
  String initialScheme = FILE_SCHEME;
  String[] schemeRestrictions = null;
  boolean showFileScheme = true;
  boolean showLocation;
  boolean showCustomUI = true;

  final VfsResolver resolver;

  public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
    new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME,
      UserAuthenticationData.PASSWORD };

  /**
   * It will add the panel to ordered list started reverse from Integer.MAX_VALUE
   * 
   * please check the {@link #addVFSUIPanelOrdered(Integer, CustomVfsUiPanel)}
   */
  public void addVFSUIPanel( CustomVfsUiPanel panel ) {
    int orderForNewPanel = Integer.MAX_VALUE;
    while ( customUIPanelsOrderedMap.get( orderForNewPanel ) != null ) {
      orderForNewPanel--;
    }
    addVFSUIPanel( orderForNewPanel, panel );
  }

  /**
   * It will add the panel with order to use order for get name of file system
   * 
   * <b>Important note</b> if we already have the panel with order which you are trying  to add it will be replaced new panel!
   */
  public void addVFSUIPanel( Integer order,  CustomVfsUiPanel panel ) {
    customUIPanelsOrderedMap.put( order, panel );
  }

  public List<CustomVfsUiPanel> getCustomVfsUiPanels() {
    return customUIPanelsOrderedMap.values().stream().collect( Collectors.toList() );
  }

  public void createCustomUIPanel( final Shell dialog ) {
    customUIPanel = new Composite( dialog, SWT.NONE );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
    customUIPanel.setLayoutData( gridData );
    customUIPanel.setLayout( new GridLayout( 1, false ) );

    comboPanel = new Composite( customUIPanel, SWT.NONE );
    comboPanel.setLayoutData( gridData );
    comboPanel.setLayout( new GridLayout( 2, false ) );
    comboPanel.setData( "donotremove" );

    Label lookInLabel = new Label( comboPanel, SWT.NONE );
    lookInLabel.setText( Messages.getString( "VfsFileChooserDialog.LookIn" ) );
    gridData = new GridData( SWT.LEFT, SWT.CENTER, false, false );
    lookInLabel.setLayoutData( gridData );

    customUIPicker = new Combo( comboPanel, SWT.READ_ONLY );
    gridData = new GridData( SWT.LEFT, SWT.CENTER, true, false );
    customUIPicker.setLayoutData( gridData );

    if ( !showLocation ) {
      comboPanel.setParent( fakeShell );
    }

    if ( !showCustomUI ) {
      customUIPanel.setParent( fakeShell );
    }

    customUIPicker.addSelectionListener( new SelectionListener() {

      public void widgetSelected( SelectionEvent event ) {
        selectCustomUI();
      }

      public void widgetDefaultSelected( SelectionEvent event ) {
        selectCustomUI();
      }
    } );
    customUIPicker.addKeyListener( new KeyListener() {

      public void keyReleased( KeyEvent arg0 ) {
        selectCustomUI();
      }

      public void keyPressed( KeyEvent arg0 ) {
        selectCustomUI();
      }
    } );

    boolean createdLocal = false;
    for ( CustomVfsUiPanel panel : getCustomVfsUiPanels() ) {
      if ( panel.getVfsScheme().equals( FILE_SCHEME ) ) {
        createdLocal = true;
      }
    }

    if ( !createdLocal ) {
      CustomVfsUiPanel localPanel = new CustomVfsUiPanel( FILE_SCHEME, "Local", this, SWT.None ) {
        public void activate() {
          try {
            File startFile = new File( getKettleUserDataDirectory() );
            if ( startFile == null || !startFile.exists() ) {
              startFile = File.listRoots()[ 0 ];
            }
            FileObject dot = resolver.resolveFile( startFile.toURI().toURL().toExternalForm() );
            rootFile = resolver.resolveFile( getKettleUserDataDirectory() );
            selectedFile = rootFile;
            setInitialFile( selectedFile );
            openFileCombo.setText( selectedFile.getName().getURI() );
            resolveVfsBrowser();
          } catch ( Throwable t ) {
            t.printStackTrace();
          }
        }

        /*
         * Copied from kettle-core to break the dependency on the patched kettle-core
         * so that this project can be built using the official kettle-core
         */
        private String getKettleUserDataDirectory() {
          String path = Const.getKettleDirectory();
          String user = getUser();
          if ( user != null ) {
            path += Const.FILE_SEPARATOR + "users" + Const.FILE_SEPARATOR + user;
          }
          return path + Const.FILE_SEPARATOR + "data";
        }

        private String getUser() {
          try {
            return RWT.getRequest().getRemoteUser();
          } catch ( Exception e ) { // when accessed from background threads (e.g., when the webSpoon server is starting)
            return null;
          }
        }
      };
      addVFSUIPanel( 100, localPanel );
    }
  }

  private void selectCustomUI() {
    hideCustomPanelChildren();
    String desiredScheme = customUIPicker.getText();
    for ( CustomVfsUiPanel panel : getCustomVfsUiPanels() ) {
      if ( desiredScheme.equals( panel.getVfsSchemeDisplayText() ) ) {
        panel.setParent( customUIPanel );
        panel.activate();
        currentPanel = panel;
      }
    }
    if ( currentPanel == null ) {
      currentPanel = getCustomVfsUiPanels().get( 0 );
      currentPanel.setParent( customUIPanel );
      currentPanel.activate();
    }
    customUIPanel.pack();
    dialog.layout();
  }

  private void hideCustomPanelChildren() {
    Control[] children = customUIPanel.getChildren();
    for ( Control child : children ) {
      if ( !( child instanceof Composite ) || !"donotremove".equals( ( child ).getData() ) ) {
        child.setParent( fakeShell );
      }
    }
    customUIPanel.pack();
  }

  public void populateCustomUIPanel( Shell dialog ) {
    ArrayList<String> customNames = new ArrayList<String>();
    for ( int i = 0; i < getCustomVfsUiPanels().size(); i++ ) {
      CustomVfsUiPanel panel = getCustomVfsUiPanels().get( i );
      if ( panel.getVfsScheme().equalsIgnoreCase( FILE_SCHEME ) || schemeRestrictions == null
        || isRestrictedTo( panel.getVfsScheme() ) ) {
        if ( panel.getVfsScheme().equalsIgnoreCase( FILE_SCHEME ) && !showFileScheme ) {
          continue;
        }
        if ( panel.getVfsScheme().equalsIgnoreCase( MAPRFS_SCHEME ) ) {
          continue;
        }
        customNames.add( panel.getVfsSchemeDisplayText() );
      }
    }

    customUIPicker.setItems( customNames.toArray( new String[] {} ) );
    hideCustomPanelChildren();

    // hide entire panel if no customizations
    if ( customNames.size() == 0 ) {
      customUIPanel.setParent( fakeShell );
    } else {
      if ( customNames.size() == 1 && FILE_SCHEME.equals( customNames.get( 0 ) ) ) {
        customUIPanel.setParent( fakeShell );
      } else {
        String initialSchemeDisplayText = initialScheme;
        for ( int i = 0; i < getCustomVfsUiPanels().size(); i++ ) {
          if ( getCustomVfsUiPanels().get( i ).getVfsScheme().equalsIgnoreCase( initialScheme ) ) {
            initialSchemeDisplayText = getCustomVfsUiPanels().get( i ).getVfsSchemeDisplayText();
            break;
          }
        }
        for ( int i = 0; i < customUIPicker.getItemCount(); i++ ) {
          if ( customUIPicker.getItem( i ).equalsIgnoreCase( initialSchemeDisplayText ) ) {
            customUIPicker.select( i );
            customUIPicker.notifyListeners( SWT.Selection, null );
          }
        }
      }
    }
  }

  private boolean isRestrictedTo( String schema ) {
    boolean isRestrictedTo = false;
    if ( schemeRestrictions != null && schema != null ) {
      for ( String restriction : schemeRestrictions ) {
        if ( schema.equalsIgnoreCase( restriction ) ) {
          isRestrictedTo = true;
          break;
        }
      }
    }
    return isRestrictedTo;
  }

  public VfsFileChooserDialog( Shell applicationShell, FileSystemManager fsm, FileObject rootFile,
                               FileObject initialFile ) {
    this( applicationShell, new DelegatingResolver( fsm ), rootFile, initialFile );
  }

  public VfsFileChooserDialog( Shell applicationShell, VfsResolver resolver, FileObject rootFile,
                               FileObject initialFile ) {
    this.rootFile = rootFile;
    this.initialFile = initialFile;
    this.applicationShell = applicationShell;
    this.resolver = resolver;
    createDialog( applicationShell );
  }

  private void createDialog( Shell applicationShell ) {
    if ( dialog == null || dialog.isDisposed() ) {
      dialog = new Shell( applicationShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL );

      if ( applicationShell.getImage() != null ) {
        dialog.setImage( applicationShell.getImage() );
      }

      dialog.addListener( SWT.Close, new Listener() {
        public void handleEvent( Event event ) {
          hideCustomPanelChildren();
        }
      } );

      dialog.addDisposeListener( new DisposeListener() {
        public void widgetDisposed( DisposeEvent arg0 ) {
          disposeImages();
        }
      } );

      if ( fileDialogMode != VFS_DIALOG_SAVEAS ) {
        dialog.setText( Messages.getString( "VfsFileChooserDialog.openFile" ) ); //$NON-NLS-1$
      } else {
        dialog.setText( Messages.getString( "VfsFileChooserDialog.saveAs" ) ); //$NON-NLS-1$
      }
      dialog.setLayout( new GridLayout() );
      dialog.setBackgroundMode( SWT.INHERIT_FORCE );
      dialog.setBackground( dialog.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
      createCustomUIPanel( dialog );
    }
  }

  private void disposeImages() {
    if ( imgFolderUp != null && !imgFolderUp.isDisposed() ) {
      imgFolderUp.dispose();
    }
    if ( imgDelete != null && !imgDelete.isDisposed() ) {
      imgDelete.dispose();
    }
    if ( imgNewFolder != null && !imgNewFolder.isDisposed() ) {
      imgNewFolder.dispose();
    }
  }


  public FileObject open( Shell applicationShell, FileObject defaultInitialFile, String fileName, String[] fileFilters,
                          String[] fileFilterNames,
                          int fileDialogMode ) {
    return open( applicationShell, defaultInitialFile, fileName, fileFilters, fileFilterNames, fileDialogMode, false );
  }

  public FileObject open( Shell applicationShell, FileObject defaultInitialFile, String fileName, String[] fileFilters,
                          String[] fileFilterNames,
                          int fileDialogMode, boolean returnUserAuthenticatedFile ) {
    this.defaultInitialFile = defaultInitialFile;
    return open( applicationShell, fileName, fileFilters, fileFilterNames, fileDialogMode,
      returnUserAuthenticatedFile );
  }

  public FileObject open( Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames,
                          int fileDialogMode ) {
    return open( applicationShell, fileName, fileFilters, fileFilterNames, fileDialogMode, false );
  }

  public FileObject open( Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames,
                          int fileDialogMode, boolean returnUserAuthenticatedFile ) {
    if ( this.defaultInitialFile != null && this.defaultInitialFile.getName() != null
      && this.defaultInitialFile.getName().getScheme() != null ) {
      return open( applicationShell, "", this.defaultInitialFile.getName().getScheme(),
        true, fileName, fileFilters, fileFilterNames, fileDialogMode, returnUserAuthenticatedFile );
    } else {
      return open( applicationShell, "", FILE_SCHEME, true, fileName, fileFilters, fileFilterNames, fileDialogMode,
        returnUserAuthenticatedFile );
    }
  }

  public FileObject open( Shell applicationShell, String schemeRestriction, String initialScheme,
                          boolean showFileScheme, String fileName, String[] fileFilters,
                          String[] fileFilterNames, int fileDialogMode ) {
    return open( applicationShell, schemeRestriction, initialScheme, showFileScheme, fileName, fileFilters,
      fileFilterNames, fileDialogMode, false );
  }

  public FileObject open( Shell applicationShell, String schemeRestriction, String initialScheme,
                          boolean showFileScheme, String fileName, String[] fileFilters,
                          String[] fileFilterNames, int fileDialogMode, boolean returnUserAuthenticatedFile ) {
    String[] restrictions = null;
    if ( schemeRestriction != null && !schemeRestriction.trim().equals( "" ) ) {
      restrictions = new String[ 1 ];
      restrictions[ 0 ] = schemeRestriction;
    }
    return open( applicationShell, restrictions, initialScheme, showFileScheme, fileName, fileFilters, fileFilterNames,
      returnUserAuthenticatedFile, fileDialogMode );
  }

  public FileObject open( Shell applicationShell, String[] schemeRestrictions, String initialScheme,
                          boolean showFileScheme, String fileName, String[] fileFilters,
                          String[] fileFilterNames, boolean returnUserAuthenticatedFile, int fileDialogMode ) {
    return open( applicationShell, schemeRestrictions, initialScheme, showFileScheme, fileName, fileFilters,
      fileFilterNames, returnUserAuthenticatedFile,
      fileDialogMode, true );
  }

  public FileObject open( Shell applicationShell, String[] schemeRestrictions, String initialScheme,
                          boolean showFileScheme, String fileName, String[] fileFilters,
                          String[] fileFilterNames, boolean returnUserAuthenticatedFile, int fileDialogMode,
                          boolean showLocation ) {
    return open( applicationShell, schemeRestrictions, initialScheme, showFileScheme, fileName, fileFilters,
      fileFilterNames, returnUserAuthenticatedFile,
      fileDialogMode, true, true );
  }

  public FileObject open( Shell applicationShell, String[] schemeRestrictions, String initialScheme,
                          boolean showFileScheme, String fileName, String[] fileFilters,
                          String[] fileFilterNames, boolean returnUserAuthenticatedFile, int fileDialogMode,
                          boolean showLocation, boolean showCustomUI ) {

    this.fileDialogMode = fileDialogMode;
    this.fileFilters = fileFilters;
    this.fileFilterNames = fileFilterNames;
    this.applicationShell = applicationShell;
    this.showFileScheme = showFileScheme;
    this.initialScheme = initialScheme;
    this.schemeRestrictions = schemeRestrictions;
    this.showLocation = showLocation;
    this.showCustomUI = showCustomUI;

    FileObject tmpInitialFile = initialFile;

    if ( defaultInitialFile != null && rootFile == null ) {
      try {
        rootFile = defaultInitialFile.getFileSystem().getRoot();
        initialFile = defaultInitialFile;
      } catch ( FileSystemException ignored ) {
        // well we tried
      }
    }

    createDialog( applicationShell );

    if ( !showLocation ) {
      comboPanel.setParent( fakeShell );
    } else {
      comboPanel.setParent( customUIPanel );
    }

    if ( !showCustomUI ) {
      customUIPanel.setParent( fakeShell );
    } else {
      customUIPanel.setParent( dialog );
    }

    // create our file chooser tool bar, contains parent folder combo and various controls
    createToolbarPanel( dialog );
    // create our vfs browser component
    createVfsBrowser( dialog );
    populateCustomUIPanel( dialog );
    if ( fileDialogMode == VFS_DIALOG_SAVEAS ) {
      createFileNamePanel( dialog, fileName );
    } else {
      // create file filter panel
      createFileFilterPanel( dialog );
    }
    // create our ok/cancel buttons
    createButtonPanel( dialog );

    initialFile = tmpInitialFile;
    // set the initial file selection
    try {
      if ( initialFile != null || rootFile != null ) {
        vfsBrowser.selectTreeItemByFileObject( initialFile != null ? initialFile : rootFile, true );
        updateParentFileCombo( initialFile != null ? initialFile : rootFile );
        setSelectedFile( initialFile != null ? initialFile : rootFile );
        openFileCombo.setText( initialFile != null ? initialFile.getName().getFriendlyURI() : rootFile.getName().getFriendlyURI() );
      }
    } catch ( FileSystemException e ) {
      MessageBox box = new MessageBox( dialog.getShell() );
      box.setText( Messages.getString( "VfsFileChooserDialog.error" ) ); //$NON-NLS-1$
      box.setMessage( e.getMessage() );
      box.open();
    }

    // set the size and show the dialog
    int height = 550;
    int width = 800;
    dialog.setSize( width, height );
    Rectangle bounds = dialog.getDisplay().getPrimaryMonitor().getClientArea();
    int x = ( bounds.width - width ) / 2;
    int y = ( bounds.height - height ) / 2;
    dialog.setLocation( x, y );
    dialog.open();

    if ( rootFile != null && fileDialogMode == VFS_DIALOG_SAVEAS ) {
      if ( !rootFile.getFileSystem().hasCapability( Capability.WRITE_CONTENT ) ) {
        MessageBox messageDialog = new MessageBox( dialog.getShell(), SWT.OK );
        messageDialog.setText( Messages.getString( "VfsFileChooserDialog.warning" ) ); //$NON-NLS-1$
        messageDialog.setMessage( Messages.getString( "VfsFileChooserDialog.noWriteSupport" ) ); //$NON-NLS-1$
        messageDialog.open();
      }
    }

    vfsBrowser.fileSystemTree.forceFocus();
    while ( !dialog.isDisposed() ) {
      if ( !dialog.getDisplay().readAndDispatch() ) {
        dialog.getDisplay().sleep();
      }
    }

    // we just woke up, we are probably disposed already..
    if ( !dialog.isDisposed() ) {
      hideCustomPanelChildren();
      dialog.dispose();
    }
    if ( okPressed ) {
      FileObject returnFile = vfsBrowser.getSelectedFileObject();
      if ( returnFile != null && fileDialogMode == VFS_DIALOG_SAVEAS ) {
        try {
          if ( returnFile.getType().equals( FileType.FILE ) ) {
            returnFile = returnFile.getParent();
          }
          returnFile = returnFile.resolveFile( enteredFileName );
        } catch ( FileSystemException e ) {
          e.printStackTrace();
        }
      }

      // put user/pass on the filename so it comes out in the getUri call.
      if ( !returnUserAuthenticatedFile ) {
        // make sure to put the user/pass on the url if it's not there
        if ( returnFile != null && returnFile.getName() instanceof URLFileName ) {
          URLFileName urlFileName = (URLFileName) returnFile.getName();
          if ( urlFileName.getUserName() == null || urlFileName.getPassword() == null ) {
            // set it
            String user = "";
            String pass = "";

            UserAuthenticator userAuthenticator = DefaultFileSystemConfigBuilder.getInstance()
              .getUserAuthenticator( returnFile.getFileSystem().getFileSystemOptions() );

            if ( userAuthenticator != null ) {
              UserAuthenticationData data = userAuthenticator.requestAuthentication( AUTHENTICATOR_TYPES );
              user = String.valueOf( data.getData( UserAuthenticationData.USERNAME ) );
              pass = String.valueOf( data.getData( UserAuthenticationData.PASSWORD ) );
              try {
                user = URLEncoder.encode( user, "UTF-8" );
                pass = URLEncoder.encode( pass, "UTF-8" );
              } catch ( UnsupportedEncodingException e ) {
                // ignore, just use the un encoded values
              }
            }

            // build up the url with user/pass on it
            int port = urlFileName.getPort();
            String portString = ( port < 1 ) ? "" : ( ":" + port );
            String urlWithUserPass = urlFileName.getScheme() + "://" + user + ":" + pass
              + "@" + urlFileName.getHostName() + portString + urlFileName.getPath();

            try {
              returnFile = currentPanel.resolveFile( urlWithUserPass );
            } catch ( FileSystemException e ) {
              // couldn't resolve with user/pass on url??? interesting
              e.printStackTrace();
            }

          }
        }
      }
      return returnFile;
    } else {
      return null;
    }
  }

  public void createButtonPanel( Shell dialog ) {
    Composite buttonPanel = new Composite( dialog, SWT.NONE );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
    buttonPanel.setLayoutData( gridData );
    buttonPanel.setLayout( new GridLayout( 4, false ) );

    String buttonAlign =
      System.getProperty( "org.pentaho.di.buttonPosition", "right" ).toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$

    if ( !"left".equals( buttonAlign ) ) { //$NON-NLS-1$
      Label emptyLabel = new Label( buttonPanel, SWT.NONE );
      gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      emptyLabel.setLayoutData( gridData );
    }
    okButton = new Button( buttonPanel, SWT.PUSH );
    okButton.setText( Messages.getString( "VfsFileChooserDialog.ok" ) ); //$NON-NLS-1$
    gridData = new GridData( SWT.FILL, SWT.FILL, false, false );
    gridData.widthHint = 90;
    okButton.setLayoutData( gridData );
    okButton.addSelectionListener( this );
    cancelButton = new Button( buttonPanel, SWT.PUSH );
    cancelButton.setText( Messages.getString( "VfsFileChooserDialog.cancel" ) ); //$NON-NLS-1$
    cancelButton.addSelectionListener( this );
    gridData = new GridData( SWT.FILL, SWT.FILL, false, false );
    gridData.widthHint = 90;
    cancelButton.setLayoutData( gridData );
    if ( "center".equals( buttonAlign ) ) { //$NON-NLS-1$
      Label emptyLabel = new Label( buttonPanel, SWT.NONE );
      gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
      emptyLabel.setLayoutData( gridData );
    }
  }

  public void createFileFilterPanel( Shell dialog ) {
    Composite filterPanel = new Composite( dialog, SWT.NONE );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
    filterPanel.setLayoutData( gridData );
    filterPanel.setLayout( new GridLayout( 3, false ) );
    // create filter label
    Label filterLabel = new Label( filterPanel, SWT.NONE );
    filterLabel.setText( Messages.getString( "VfsFileChooserDialog.filter" ) ); //$NON-NLS-1$
    gridData = new GridData( SWT.FILL, SWT.CENTER, false, false );
    filterLabel.setLayoutData( gridData );
    // create file filter combo
    fileFilterCombo = new Combo( filterPanel, SWT.READ_ONLY );
    gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
    fileFilterCombo.setLayoutData( gridData );
    fileFilterCombo.setItems( fileFilterNames );
    fileFilterCombo.addSelectionListener( this );
    fileFilterCombo.select( 0 );
  }

  public void createFileNamePanel( Shell dialog, String fileName ) {
    Composite fileNamePanel = new Composite( dialog, SWT.NONE );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, false );
    fileNamePanel.setLayoutData( gridData );
    fileNamePanel.setLayout( new GridLayout( 2, false ) );
    Label fileNameLabel = new Label( fileNamePanel, SWT.NONE );
    fileNameLabel.setText( Messages.getString( "VfsFileChooserDialog.fileName" ) ); //$NON-NLS-1$
    gridData = new GridData( SWT.FILL, SWT.CENTER, false, false );
    fileNameLabel.setLayoutData( gridData );
    fileNameText = new Text( fileNamePanel, SWT.BORDER );
    if ( fileName != null ) {
      fileNameText.setText( fileName );
    }
    gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    fileNameText.setLayoutData( gridData );
    fileNameText.addKeyListener( new KeyListener() {

      public void keyPressed( KeyEvent arg0 ) {
      }

      public void keyReleased( KeyEvent event ) {
        if ( event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR ) {
          okPressed();
        }
      }

    } );
  }

  public void createVfsBrowser( Shell dialog ) {
    String defaultFilter = null;
    if ( fileFilters != null && fileFilters.length > 0 ) {
      defaultFilter = fileFilters[ 0 ];
    }
    vfsBrowser =
      new VfsBrowser( dialog, SWT.NONE, rootFile, defaultFilter, fileDialogMode == VFS_DIALOG_SAVEAS ? true : false,
        false );
    // vfsBrowser.selectTreeItemByName(rootFile.getName().getURI(), true);
    vfsBrowser.addVfsBrowserListener( this );
    GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
    vfsBrowser.setLayoutData( gridData );
  }

  public void createToolbarPanel( Shell dialog ) {
    Composite chooserToolbarPanel = new Composite( dialog, SWT.NONE );
    chooserToolbarPanel.setLayout( new GridLayout( 6, false ) );
    GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    chooserToolbarPanel.setLayoutData( gridData );

    // changeRootButton = new Button(chooserToolbarPanel, SWT.PUSH);
    // changeRootButton.setToolTipText(Messages.getString("VfsFileChooserDialog.changeVFSRoot")); //$NON-NLS-1$
    // changeRootButton.setImage(new Image(chooserToolbarPanel.getDisplay(), getClass().getResourceAsStream
    // ("/icons/network.gif"))); //$NON-NLS-1$
    // gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    // changeRootButton.setLayoutData(gridData);
    // changeRootButton.addSelectionListener(this);

    Label parentFoldersLabel = new Label( chooserToolbarPanel, SWT.NONE );
    if ( fileDialogMode != VFS_DIALOG_SAVEAS ) {
      parentFoldersLabel.setText( Messages.getString( "VfsFileChooserDialog.openFromFolder" ) ); //$NON-NLS-1$
    } else {
      parentFoldersLabel.setText( Messages.getString( "VfsFileChooserDialog.saveInFolder" ) ); //$NON-NLS-1$
    }
    gridData = new GridData( SWT.FILL, SWT.CENTER, false, false );
    parentFoldersLabel.setLayoutData( gridData );
    openFileCombo = new Combo( chooserToolbarPanel, SWT.BORDER );
    gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    openFileCombo.setLayoutData( gridData );
    openFileCombo.addSelectionListener( this );
    openFileCombo.addKeyListener( new KeyListener() {

      public void keyPressed( KeyEvent event ) {
        // UP :
        //
        if ( ( event.keyCode == SWT.ARROW_UP ) && ( ( event.stateMask & SWT.CONTROL ) == 0 ) && (
          ( event.stateMask & SWT.ALT ) == 0 ) ) {
          resolveVfsBrowser();
          vfsBrowser.selectPreviousItem();
        }

        // DOWN:
        //
        if ( ( event.keyCode == SWT.ARROW_DOWN ) && ( ( event.stateMask & SWT.CONTROL ) == 0 ) && (
          ( event.stateMask & SWT.ALT ) == 0 ) ) {
          resolveVfsBrowser();
          vfsBrowser.selectNextItem();
        }
      }

      public void keyReleased( KeyEvent event ) {
        if ( event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR ) {
          try {
            // resolve the selected folder (without displaying access/secret keys in plain text)
            //            FileObject newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(folderURL
            // .getFolderURL(openFileCombo.getText()));
            //            FileObject newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile
            // (getSelectedFile().getName().getURI());
            FileObject newRoot = currentPanel.resolveFile( openFileCombo.getText() );

            vfsBrowser.resetVfsRoot( newRoot );
          } catch ( FileSystemException e ) {
            MessageBox errorDialog = new MessageBox( vfsBrowser.getDisplay().getActiveShell(), SWT.OK );
            errorDialog.setText( Messages.getString( "VfsFileChooserDialog.error" ) ); //$NON-NLS-1$
            errorDialog.setMessage( e.getMessage() );
            errorDialog.open();
          }
        }
      }

    } );
    folderUpButton = new Label( chooserToolbarPanel, 0 );
    folderUpButton.setToolTipText( Messages.getString( "VfsFileChooserDialog.upOneLevel" ) ); //$NON-NLS-1$
    folderUpButton.setImage( getFolderUpImage( chooserToolbarPanel.getDisplay() ) );
    gridData = new GridData( SWT.CENTER, SWT.CENTER, false, false );
    folderUpButton.setLayoutData( gridData );
    folderUpButton.addMouseListener( this );
    deleteFileButton = new Label( chooserToolbarPanel, 0 );
    deleteFileButton.setToolTipText( Messages.getString( "VfsFileChooserDialog.deleteFile" ) ); //$NON-NLS-1$
    deleteFileButton.setImage( getDeleteImage( chooserToolbarPanel.getDisplay() ) );
    gridData = new GridData( SWT.CENTER, SWT.CENTER, false, false );
    deleteFileButton.setLayoutData( gridData );
    deleteFileButton.addMouseListener( this );
    newFolderButton = new Label( chooserToolbarPanel, 0 );
    newFolderButton.setToolTipText( Messages.getString( "VfsFileChooserDialog.createNewFolder" ) ); //$NON-NLS-1$
    newFolderButton.setImage( getNewFolderImage( chooserToolbarPanel.getDisplay() ) );
    gridData = new GridData( SWT.CENTER, SWT.CENTER, false, false );
    newFolderButton.setLayoutData( gridData );
    newFolderButton.addMouseListener( this );
  }

  public void okPressed() {
    if ( fileDialogMode == VFS_DIALOG_SAVEAS && "".equals( fileNameText.getText() ) ) { //$NON-NLS-1$
      // do nothing, user did not enter a file name for saving
      MessageBox messageDialog = new MessageBox( dialog, SWT.OK );
      messageDialog.setText( Messages.getString( "VfsFileChooserDialog.error" ) ); //$NON-NLS-1$
      messageDialog.setMessage( Messages.getString( "VfsFileChooserDialog.noFilenameEntered" ) ); //$NON-NLS-1$
      messageDialog.open();
      return;
    }

    if ( fileDialogMode == VFS_DIALOG_SAVEAS ) {
      try {
        FileObject toBeSavedFile = vfsBrowser.getSelectedFileObject().resolveFile( fileNameText.getText() );
        if ( toBeSavedFile.exists() ) {
          MessageBox messageDialog = new MessageBox( dialog, SWT.YES | SWT.NO );
          messageDialog.setText( Messages.getString( "VfsFileChooserDialog.fileExists" ) ); //$NON-NLS-1$
          messageDialog.setMessage( Messages.getString( "VfsFileChooserDialog.fileExistsOverwrite" ) ); //$NON-NLS-1$
          int flag = messageDialog.open();
          if ( flag == SWT.NO ) {
            return;
          }
        }
      } catch ( FileSystemException e ) {
        e.printStackTrace();
      }
    }
    if ( fileDialogMode == VFS_DIALOG_SAVEAS ) {
      enteredFileName = fileNameText.getText();
    }

    try {
      if ( fileDialogMode == VFS_DIALOG_OPEN_FILE && vfsBrowser.getSelectedFileObject().getType()
        .equals( FileType.FOLDER ) ) {
        // try to open this node, it is a directory
        vfsBrowser.selectTreeItemByFileObject( vfsBrowser.getSelectedFileObject(), true );
        return;
      }
    } catch ( FileSystemException e ) {
      e.printStackTrace();
    }

    okPressed = true;
    hideCustomPanelChildren();
    dialog.dispose();
  }

  public void widgetDefaultSelected( SelectionEvent se ) {
  }

  public void widgetSelected( SelectionEvent se ) {
    if ( se.widget == openFileCombo ) {
      // String filePath = parentFoldersCombo.getItem(parentFoldersCombo.getSelectionIndex());
      // vfsBrowser.selectTreeItemByName(filePath, true);

      try {
        // resolve the selected folder (without displaying access/secret keys in plain text)
        //        FileObject newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(folderURL
        // .getFolderURL(openFileCombo.getText()));
        FileObject newRoot = currentPanel.resolveFile( openFileCombo.getText() );
        vfsBrowser.resetVfsRoot( newRoot );
      } catch ( FileSystemException e ) {
        e.printStackTrace();
      }

    } else if ( se.widget == okButton ) {
      okPressed();
      // } else if (se.widget == changeRootButton) {
      // promptForNewVfsRoot();
    } else if ( se.widget == fileFilterCombo ) {

      Runnable r = new Runnable() {
        public void run() {
          String filter = fileFilters[ fileFilterCombo.getSelectionIndex() ];
          vfsBrowser.setFilter( filter );
          try {
            vfsBrowser.applyFilter();
          } catch ( FileSystemException e ) {
            MessageBox mb = new MessageBox( newFolderButton.getShell(), SWT.OK );
            mb.setText( Messages.getString( "VfsFileChooserDialog.errorApplyFilter" ) ); //$NON-NLS-1$
            mb.setMessage( e.getMessage() );
            mb.open();
          }
        }
      };
      BusyIndicator.showWhile( fileFilterCombo.getDisplay(), r );
    } else {
      okPressed = false;
      hideCustomPanelChildren();
      dialog.dispose();
    }
  }

  @Override
  public void mouseDown( MouseEvent se ) {
    if ( se.widget == folderUpButton ) {
      try {
        FileObject newRoot = vfsBrowser.getSelectedFileObject().getParent();
        if ( newRoot != null ) {
          vfsBrowser.resetVfsRoot( newRoot );
          vfsBrowser.setSelectedFileObject( newRoot );
          // make sure access/secret keys not displayed in plain text
          // String str = folderURL.setFolderURL(newRoot.getName().getURI());
          openFileCombo.setText( newRoot.getName().getFriendlyURI() );
        }
      } catch ( Exception e ) {
        // top of root
      }
    } else if ( se.widget == newFolderButton ) {
      promptForNewFolder();
    } else if ( se.widget == deleteFileButton ) {
      MessageBox messageDialog = new MessageBox( se.widget.getDisplay().getActiveShell(), SWT.YES | SWT.NO );
      messageDialog.setText( Messages.getString( "VfsFileChooserDialog.confirm" ) ); //$NON-NLS-1$
      messageDialog.setMessage( Messages.getString( "VfsFileChooserDialog.deleteFile" ) ); //$NON-NLS-1$
      int status = messageDialog.open();
      if ( status == SWT.YES ) {
        try {
          vfsBrowser.deleteSelectedItem();
        } catch ( FileSystemException e ) {
          MessageBox errorDialog = new MessageBox( se.widget.getDisplay().getActiveShell(), SWT.OK );
          errorDialog.setText( Messages.getString( "VfsFileChooserDialog.error" ) ); //$NON-NLS-1$
          errorDialog.setMessage( e.getMessage() );
          errorDialog.open();
        }
      }
    }
  }

  @Override
  public void mouseUp( MouseEvent arg0 ) {
  }

  @Override
  public void mouseDoubleClick( MouseEvent arg0 ) {
  }

  public void promptForNewFolder() {
    boolean done = false;
    String defaultText = "New Folder";
    String text = defaultText;
    while ( !done ) {
      if ( text == null ) {
        text = defaultText;
      }
      TextInputDialog textDialog =
        new TextInputDialog( Messages.getString( "VfsBrowser.enterNewFolderName" ), text, 500, 160 ); //$NON-NLS-1$
      text = textDialog.open();
      if ( text != null && !"".equals( text ) ) { //$NON-NLS-1$
        try {
          vfsBrowser.createFolder( text ); //$NON-NLS-1$
          done = true;
        } catch ( FileSystemException e ) {
          MessageBox errorDialog = new MessageBox( newFolderButton.getShell(), SWT.OK );
          errorDialog.setText( Messages.getString( "VfsBrowser.error" ) ); //$NON-NLS-1$
          if ( e.getCause() != null ) {
            errorDialog.setMessage( e.getCause().getMessage() );
          } else {
            errorDialog.setMessage( e.getMessage() );
          }
          errorDialog.open();
        }
      } else {
        done = true;
      }
    }
  }

  public void promptForNewVfsRoot() {
    boolean done = false;
    String defaultText = vfsBrowser.rootFileObject.getName().getURI();
    String text = defaultText;
    while ( !done ) {
      if ( text == null ) {
        text = defaultText;
      }
      File[] fileRoots = File.listRoots();
      String[] roots = new String[ fileRoots.length ];
      for ( int i = 0; i < roots.length; i++ ) {
        try {
          roots[ i ] = fileRoots[ i ].toURI().toURL().toExternalForm();
        } catch ( MalformedURLException e ) {
          e.printStackTrace();
        }
      }
      ComboBoxInputDialog textDialog =
        new ComboBoxInputDialog( Messages.getString( "VfsFileChooserDialog.enterNewVFSRoot" ), text, roots, 650,
          100 ); //$NON-NLS-1$
      text = textDialog.open();
      if ( text != null && !"".equals( text ) ) { //$NON-NLS-1$
        try {
          vfsBrowser.resetVfsRoot( currentPanel.resolveFile( text ) );
          done = true;
        } catch ( FileSystemException e ) {
          MessageBox errorDialog = new MessageBox( vfsBrowser.getDisplay().getActiveShell(), SWT.OK );
          errorDialog.setText( Messages.getString( "VfsFileChooserDialog.error" ) ); //$NON-NLS-1$
          errorDialog.setMessage( e.getMessage() );
          errorDialog.open();
        }
      } else {
        done = true;
      }
    }
  }

  public void updateParentFileCombo( FileObject selectedItem ) {
    try {
      List<FileObject> parentChain = new ArrayList<FileObject>();
      // are we a directory?
      try {
        if ( selectedItem.getType() == FileType.FOLDER && selectedItem.getType().hasChildren() ) {
          // we have real children....
          parentChain.add( selectedItem );
        }
      } catch ( Exception e ) {
        // we are not a folder
      }
      FileObject parentFileObject;
      parentFileObject = selectedItem.getParent();
      while ( parentFileObject != null ) {
        parentChain.add( parentFileObject );
        parentFileObject = parentFileObject.getParent();
      }

      File[] roots = File.listRoots();
      if ( currentPanel != null ) {
        for ( int i = 0; i < roots.length; i++ ) {
          parentChain.add( currentPanel.resolveFile( roots[ i ].getAbsolutePath() ) );
        }
      }

      String[] items = new String[ parentChain.size() ];
      int idx = 0;
      for ( int i = parentChain.size() - 1; i >= 0; i-- ) {
        items[ idx++ ] = ( parentChain.get( i ) ).getName().getFriendlyURI();
      }

      openFileCombo.setItems( items );
      openFileCombo.select( items.length - 1 );
    } catch ( Exception e ) {
      e.printStackTrace();
      // then let's not update the GUI
    }
  }

  public void fireFileObjectDoubleClicked( FileObject selectedItem ) {
    if ( fileDialogMode != VFS_DIALOG_SAVEAS ) {
      // let's try drilling into the file as a new vfs root first

      String scheme = null;
      if ( selectedItem.getName().getExtension().contains( "jar" ) ) {
        scheme = "jar:";
      } else if ( selectedItem.getName().getExtension().contains( "zip" ) ) {
        scheme = "zip:";
      } else if ( selectedItem.getName().getExtension().contains( "gz" ) ) {
        scheme = "gz:";
      } else if ( selectedItem.getName().getExtension().contains( "war" ) ) {
        scheme = "war:";
      } else if ( selectedItem.getName().getExtension().contains( "ear" ) ) {
        scheme = "ear:";
      } else if ( selectedItem.getName().getExtension().contains( "sar" ) ) {
        scheme = "sar:";
      } else if ( selectedItem.getName().getExtension().contains( "tar" ) ) {
        scheme = "tar:";
      } else if ( selectedItem.getName().getExtension().contains( "tbz2" ) ) {
        scheme = "tbz2:";
      } else if ( selectedItem.getName().getExtension().contains( "tgz" ) ) {
        scheme = "tgz:";
      } else if ( selectedItem.getName().getExtension().contains( "bz2" ) ) {
        scheme = "bz2:";
      }

      if ( scheme != null ) {
        try {
          FileObject jarFileObject =
            selectedItem.getFileSystem().getFileSystemManager().resolveFile( scheme + selectedItem.getName().getURI() );
          vfsBrowser.resetVfsRoot( jarFileObject );
          updateParentFileCombo( jarFileObject );
          vfsBrowser.fileSystemTree.forceFocus();
        } catch ( FileSystemException e ) {
          e.printStackTrace();
          okPressed = true;
          hideCustomPanelChildren();
          dialog.dispose();
        }
      } else {
        okPressed = true;
        hideCustomPanelChildren();
        dialog.dispose();
      }

    }
  }

  public void fireFileObjectSelected( FileObject selectedItem ) {
    // something has just been selected, time to update the
    // parent file combo
    updateParentFileCombo( selectedItem );
    // openFileCombo.setText(selectedItem.getName().getURI());
  }

  public void resolveVfsBrowser() {
    FileObject newRoot = null;
    try {
      //      newRoot = rootFile.getFileSystem().getFileSystemManager().resolveFile(getSelectedFile().getName()
      // .getURI());
      if ( currentPanel != null ) {
        newRoot = currentPanel.resolveFile( getSelectedFile().getName().getURI() );
      }
    } catch ( FileSystemException e ) {
      displayMessageBox( SWT.OK, Messages.getString( "VfsFileChooserDialog.error" ), e.getMessage() );
    }
    //if (newRoot != null && !newRoot.equals(vfsBrowser.getRootFileObject())) {
    if ( newRoot != null ) {
      vfsBrowser.resetVfsRoot( newRoot );
    }
  }

  private void displayMessageBox( int widgetArguments, String title, String message ) {
    MessageBox errorDialog = new MessageBox( vfsBrowser.getDisplay().getActiveShell(), widgetArguments );
    errorDialog.setText( title ); //$NON-NLS-1$
    errorDialog.setMessage( message );
    errorDialog.open();
  }

  public FileObject getRootFile() {
    return rootFile;
  }

  public void setRootFile( FileObject rootFile ) {
    this.rootFile = rootFile;
  }

  public FileObject getInitialFile() {
    return initialFile;
  }

  public void setInitialFile( FileObject initialFile ) {
    this.initialFile = initialFile;
  }

  public FileObject getSelectedFile() {
    return selectedFile;
  }

  public void setSelectedFile( FileObject selectedFile ) {
    this.selectedFile = selectedFile;
    if ( selectedFile != null ) {
      this.openFileCombo.setText( selectedFile.getName().getFriendlyURI() );
      resolveVfsBrowser();
    }
  }

  public FileObject resolveFile( String fileUri, FileSystemOptions opts ) throws FileSystemException {
    // try to match up a CustomVfsUiPanel with the scheme from the fileUri
    CustomVfsUiPanel panel = getPanelFromFileUri( fileUri );
    if ( panel != null ) {
      if ( opts == null ) {
        return panel.resolveFile( fileUri );
      } else {
        return panel.resolveFile( fileUri, opts );
      }
    }
    return null;
  }

  public FileObject resolveFile( String fileUri ) throws FileSystemException {
    return resolveFile( fileUri, null );
  }

  private CustomVfsUiPanel getPanelFromFileUri( String fileUri ) {
    if ( getCustomVfsUiPanels() != null ) {
      for ( CustomVfsUiPanel panel : getCustomVfsUiPanels() ) {
        if ( fileUri.startsWith( panel.getVfsScheme() ) ) {
          return panel;
        }
      }
    }
    return null;
  }

  public Composite getCustomUIPanel() {
    return customUIPanel;
  }

  public void setCustomUIPanel( Composite customUIPanel ) {
    this.customUIPanel = customUIPanel;
  }

  private Image getFolderUpImage( Display display ) {
    if ( imgFolderUp == null || imgFolderUp.isDisposed() ) {
      imgFolderUp = new Image( display, getClass().getResourceAsStream( "/icons/folderup.png" ) ); //$NON-NLS-1$
    }
    return imgFolderUp;
  }

  private Image getDeleteImage( Display display ) {
    if ( imgDelete == null || imgDelete.isDisposed() ) {
      imgDelete = new Image( display, getClass().getResourceAsStream( "/icons/generic-delete.png" ) ); //$NON-NLS-1$
    }
    return imgDelete;
  }

  private Image getNewFolderImage( Display display ) {
    if ( imgNewFolder == null || imgNewFolder.isDisposed() ) {
      imgNewFolder = new Image( display, getClass().getResourceAsStream( "/icons/Add.png" ) ); //$NON-NLS-1$
    }
    return imgNewFolder;
  }

  public CustomVfsUiPanel getCurrentPanel() {
    return currentPanel;
  }
}
