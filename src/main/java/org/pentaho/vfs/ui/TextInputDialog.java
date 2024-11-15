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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.vfs.messages.Messages;

public class TextInputDialog {
  String title;
  String text;
  String enteredText;
  int width;
  int height;
  Shell dialog;
  boolean okPressed = false;
  Text textInput = null;

  public TextInputDialog(String title, String text, int width, int height) {
    this.title = title;
    this.text = text;
    this.width = width;
    this.height = height;
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
    textInput = new Text(content, SWT.BORDER | SWT.SINGLE);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    textInput.setLayoutData(gridData);
    textInput.setText(text);
    Label left = new Label(dialog, SWT.NONE);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    left.setLayoutData(gridData);
    final Button ok = new Button(dialog, SWT.PUSH);
    gridData = new GridData(SWT.RIGHT, SWT.FILL, false, false);
    ok.setLayoutData(gridData);
    ok.setText(Messages.getString("TextInputDialog.ok")); //$NON-NLS-1$
    Button cancel = new Button(dialog, SWT.PUSH);
    gridData = new GridData(SWT.RIGHT, SWT.FILL, false, false);
    cancel.setLayoutData(gridData);
    cancel.setText(Messages.getString("TextInputDialog.cancel")); //$NON-NLS-1$
    SelectionListener listener = new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == ok) {
          okPressed = true;
        } else {
          okPressed = false;
        }
        enteredText = textInput.getText();
        dialog.close();
      }

      public void widgetDefaultSelected(SelectionEvent e) {
      }
    };
    ok.addSelectionListener(listener);
    textInput.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
          okPressed = true;
          enteredText = textInput.getText();
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
    try {
      shell.setImage(Display.getCurrent().getActiveShell().getImage());
    } catch (Throwable ignore) {
      try {
        shell.setImage(Display.getDefault().getActiveShell().getImage());
      } catch (Throwable ignore2) {
      }
    }
    centerShellOnDisplay(shell, Display.getCurrent(), desiredWidth, desiredHeight);
    return shell;
  }
  
  
}
