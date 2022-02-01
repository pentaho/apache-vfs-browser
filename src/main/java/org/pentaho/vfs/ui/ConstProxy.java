/*
 * Copyright 2022 Hitachi Vantara.  All rights reserved.
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

public class ConstProxy {

  private ConstProxy() { } // prevents instances

  public static String getKettleUserDataDirectory() {
    return NVL( System.getenv( "WEBSPOON_USER_HOME" ), NVL( System.getProperty( "WEBSPOON_USER_HOME" ),
      System.getProperty( "user.home" ) ) );
  }

  /**
   * Determines if the RUNNING_ON_WEBSPOON_MODE flag is set and returns its boolean value.
   * This is per user-basis.
   *
   * @return Boolean signalig the use of Webspoon mode.
   */
  public static boolean isRunningOnWebspoonMode() {
    return Boolean.parseBoolean( NVL( System.getenv( "RUNNING_ON_WEBSPOON_MODE" ), NVL( System.getProperty( "RUNNING_ON_WEBSPOON_MODE" ),
      "false" ) ) );
  }

  /**
   * Implements Oracle style NVL function
   *
   * @param source
   *          The source argument
   * @param def
   *          The default value in case source is null or the length of the string is 0
   * @return source if source is not null, otherwise return def
   */
  public static String NVL( String source, String def ) {
    if ( source == null || source.length() == 0 ) {
      return def;
    }
    return source;
  }

}
