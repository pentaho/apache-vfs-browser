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

package org.pentaho.vfs.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.vfs.messages.Messages;

public class ComboBoxInputDialog {
  String title;
  String text;
  String enteredText;
  int width;
  int height;
  Shell dialog;
  boolean okPressed = false;
  Combo rootInput = null;
  String roots[] = null;
  
  public ComboBoxInputDialog(String title, String text, String roots[], int width, int height) {
    this.title = title;
    this.text = text;
    this.width = width;
    this.height = height;
    this.roots = roots;
    init();
  }

  public String open() {
    dialog.open();
    while (!dialog.isDisposed()) {
      if (!dialog.getDisplay().readAndDispatch())
        dialog.getDisplay().sleep();
    }
    String returnValue = text;
    if (okPressed) {
      returnValue = enteredText;
    } else {
      returnValue = null;
    }
    dialog.dispose();
    return returnValue;
  }

  public void init() {
    dialog = createModalDialogShell(width, height, title);
    dialog.setLayout(new GridLayout(4, false));
    Composite content = new Composite(dialog, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    gridData.horizontalSpan = 4;
    content.setLayoutData(gridData);
    content.setLayout(new GridLayout(1, false));
    rootInput = new Combo(content, SWT.BORDER | SWT.SINGLE);
    rootInput.setItems(roots);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    rootInput.setLayoutData(gridData);
//    rootInput.setText(text);
    Label left = new Label(dialog, SWT.NONE);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    left.setLayoutData(gridData);
    final Button ok = new Button(dialog, SWT.PUSH);
    gridData = new GridData(SWT.RIGHT, SWT.FILL, false, false);
    ok.setLayoutData(gridData);
    ok.setText(Messages.getString("ComboBoxInputDialog.ok")); //$NON-NLS-1$
    Button cancel = new Button(dialog, SWT.PUSH);
    gridData = new GridData(SWT.RIGHT, SWT.FILL, false, false);
    cancel.setLayoutData(gridData);
    cancel.setText(Messages.getString("ComboBoxInputDialog.cancel")); //$NON-NLS-1$
    SelectionListener listener = new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == ok) {
          okPressed = true;
        } else {
          okPressed = false;
        }
        enteredText = rootInput.getText();
        dialog.close();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
      }
    };
    ok.addSelectionListener(listener);
    rootInput.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
          okPressed = true;
          enteredText = rootInput.getText();
          dialog.close();
        } else {
          okPressed = false;
        }
      }
    });
    cancel.addSelectionListener(listener);
  }
  
  public static void centerShellOnDisplay(Shell shell, Display display, int desiredWidth, int desiredHeight) {
    int screenWidth = display.getPrimaryMonitor().getBounds().width;
    int screenHeight = display.getPrimaryMonitor().getBounds().height;
    int applicationX = ((Math.abs(screenWidth - desiredWidth)) / 2);
    int applicationY = ((Math.abs(screenHeight - desiredHeight)) / 2);
    shell.setSize(desiredWidth, desiredHeight);
    shell.setLocation(applicationX, applicationY);
  }

  public static Shell createModalDialogShell(int desiredWidth, int desiredHeight, String title) {
    Shell shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    shell.setText(title);
    centerShellOnDisplay(shell, Display.getCurrent(), desiredWidth, desiredHeight);
    return shell;
  }
  
  
}
