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
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Khayrutdinov
 */
public class VfsFileChooserDialogTest {

  @Test( expected = NullPointerException.class )
  public void rejectsNullManager() {
    new VfsFileChooserDialog( null, (FileSystemManager) null, null, null );
  }

  @Test
  public void testOrderForCustomUIPanels() {
    VfsFileChooserDialog dialog = mock( VfsFileChooserDialog.class );
    doCallRealMethod().when( dialog ).getCustomVfsUiPanels();
    doCallRealMethod().when( dialog ).addVFSUIPanel( any( CustomVfsUiPanel.class ) );
    doCallRealMethod().when( dialog ).addVFSUIPanel( anyInt(), any( CustomVfsUiPanel.class ) );
    // will create this manually since we have a mock of dialog and this field does not initialized
    dialog.customUIPanelsOrderedMap = new TreeMap<Integer, CustomVfsUiPanel>();

    CustomVfsUiPanel panelFirst = mock( CustomVfsUiPanel.class );
    CustomVfsUiPanel panelSecond = mock( CustomVfsUiPanel.class );
    CustomVfsUiPanel panelLast = mock( CustomVfsUiPanel.class );

    List<CustomVfsUiPanel> panels = Arrays.asList( panelFirst, panelSecond, panelLast );

    int firstOrder = 1;
    int secondOrder = 2;

    dialog.addVFSUIPanel( secondOrder, panelSecond );
    dialog.addVFSUIPanel( panelLast );
    dialog.addVFSUIPanel( firstOrder, panelFirst );

    for ( int i = 0; i < panels.size(); i++ ) {
      assertEquals( panels.get( i ), dialog.getCustomVfsUiPanels().get( i ) );
    }
  }

}
