/*
 * Copyright 2021 Hitachi Vantara.  All rights reserved.
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

/**
 * Class to get properties from the core.Const class using reflection.
 * The artifact containing core.Const is not available when the pipeline builds this class.
 * For this reason we are using reflection.
 *
 */

package org.pentaho.vfs.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.rap.rwt.RWT;

public class ConstProxy {

  private static String CONST_CLASS_NAME = "org.pentaho.di.core.Const";
  private static Class<?> constClass;

  private ConstProxy() { } // prevents instances

  private static void init() throws ClassNotFoundException {
    if ( constClass == null ) {
      constClass = Class.forName( CONST_CLASS_NAME );
    }
  }

  public static String getKettleUserDataDirectory() {
    try {
      init();
      Method getKettleUserDataDirectory = constClass.getMethod( "getKettleUserDataDirectory", null );
      String path = (String) getKettleUserDataDirectory.invoke( null );
      Field fileSeparatorField = constClass.getDeclaredField( "FILE_SEPARATOR" );
      fileSeparatorField.setAccessible( true );
      String fileSeparator = (String) fileSeparatorField.get( null );
      String user = getUser();
      if ( user != null ) {
        path += fileSeparator + "users" + fileSeparator + user;
      }
      return path;
    } catch ( Exception e ) {
      return "/";
    }
  }

  private static String getUser() {
    try {
      return RWT.getRequest().getRemoteUser();
    } catch ( Exception e ) { // when accessed from background threads (e.g., when the webSpoon server is starting)
      return null;
    }
  }
}
