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
