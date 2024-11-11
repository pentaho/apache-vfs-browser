/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.vfs.ui;

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
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
