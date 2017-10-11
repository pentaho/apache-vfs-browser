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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Test;
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
