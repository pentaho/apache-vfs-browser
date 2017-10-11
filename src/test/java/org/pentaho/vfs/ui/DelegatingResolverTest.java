/*
* Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
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

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Andrey Khayrutdinov
 */
public class DelegatingResolverTest {

  @Test( expected = NullPointerException.class )
  public void onNullManager_ThrowsNpe() {
    new DelegatingResolver( null );
  }

  @Test
  public void delegatesToManager_OneParam() throws Exception {
    FileSystemManager mock = mock( FileSystemManager.class );
    new DelegatingResolver( mock ).resolveFile( "url" );

    verify( mock ).resolveFile( "url" );
  }

  @Test
  public void delegatesToManager_TwoParams() throws Exception {
    FileSystemManager mock = mock( FileSystemManager.class );
    FileSystemOptions opts = new FileSystemOptions();
    new DelegatingResolver( mock ).resolveFile( "url", opts );

    verify( mock ).resolveFile( eq( "url" ), eq( opts ) );
  }
}
