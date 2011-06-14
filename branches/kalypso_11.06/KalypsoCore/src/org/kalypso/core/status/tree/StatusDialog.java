/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.core.status.tree;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.status.StatusComposite;
import org.kalypso.core.status.tree.listener.StatusDoubleClickListener;
import org.kalypso.core.status.tree.provider.StatusContentProvider;
import org.kalypso.core.status.tree.provider.StatusLabelProvider;

/**
 * This dialog displays the validation results.
 * 
 * @author Holger Albert
 */
public class StatusDialog extends Dialog
{
  /**
   * The title of the dialog.
   */
  private String m_title;

  /**
   * The validation results.
   */
  private IStatus m_messages;

  /**
   * The constructor.
   * 
   * @param shell
   *          The parent shell, or null to create a top-level shell.
   * @param messages
   *          The validation results.
   */
  public StatusDialog( Shell shell, String title, IStatus messages )
  {
    super( shell );

    /* Initialize. */
    m_title = "Statusanzeige";
    if( title != null )
      m_title = title;
    m_messages = messages;
  }

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The object that returns the current parent shell.
   * @param messages
   *          The validation results.
   */
  public StatusDialog( IShellProvider parentShell, String title, IStatus messages )
  {
    super( parentShell );

    /* Initialize. */
    m_title = "Statusanzeige";
    if( title != null )
      m_title = title;
    m_messages = messages;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( m_title );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 2, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* If no messages are available, create a default warning. */
    if( m_messages == null )
      m_messages = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), "Es wurde kein Ergebnis übergeben..." );

    /* Display a single message. */
    if( !m_messages.isMultiStatus() )
      createSingleStatus( main );
    else
      createForMultiStatus( main );

    /* Create a group. */
    Group resultGroup = new Group( main, SWT.NONE );
    resultGroup.setLayout( new GridLayout( 2, false ) );
    resultGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    resultGroup.setText( "Ergebnis" );

    /* Create a label. */
    Label imageLabel = new Label( resultGroup, SWT.NONE );
    imageLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageLabel.setImage( StatusComposite.getStatusImage( m_messages ) );

    /* Create a label. */
    Label noticeLabel = new Label( resultGroup, SWT.NONE );
    noticeLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    noticeLabel.setText( getText( m_messages ) );

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar( Composite parent )
  {
    /* Create OK button. */
    createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
  }

  /**
   * This function creates the controls for the messages, if there is a single status.
   * 
   * @param parent
   *          The parent.
   */
  private void createSingleStatus( Composite parent )
  {
    /* Create a group. */
    Group messageGroup = new Group( parent, SWT.NONE );
    messageGroup.setLayout( new GridLayout( 1, false ) );
    messageGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    messageGroup.setText( "Nachricht" );

    /* Create a label. */
    Label messageLabel = new Label( messageGroup, SWT.WRAP );
    GridData messageData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    messageData.widthHint = 350;
    messageLabel.setLayoutData( messageData );
    messageLabel.setText( m_messages.getMessage() );

    /* If there is an exception available, show it. */
    Throwable exception = m_messages.getException();
    if( exception != null )
    {
      /* Get the exception text. */
      StringWriter sw = new StringWriter();
      exception.printStackTrace( new PrintWriter( sw ) );

      /* Create a text. */
      Text exceptionText = new Text( messageGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
      GridData exceptionData = new GridData( SWT.FILL, SWT.FILL, true, true );
      exceptionData.heightHint = 150;
      exceptionData.widthHint = 350;
      exceptionText.setLayoutData( exceptionData );
      exceptionText.setText( sw.toString() );
      exceptionText.setEditable( false );
    }
  }

  /**
   * This function creates the controls for the messages, if there is a multi status.
   * 
   * @param parent
   *          The parent.
   */
  private void createForMultiStatus( Composite parent )
  {
    /* Create a group. */
    Group messagesGroup = new Group( parent, SWT.NONE );
    messagesGroup.setLayout( new GridLayout( 1, false ) );
    messagesGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    messagesGroup.setText( "Nachrichten" );

    /* Create a tree viewer. */
    TreeViewer messagesViewer = new TreeViewer( messagesGroup, SWT.SINGLE | SWT.BORDER );
    GridData messagesData = new GridData( SWT.FILL, SWT.FILL, true, true );
    messagesData.heightHint = 250;
    messagesData.widthHint = 350;
    messagesViewer.getTree().setLayoutData( messagesData );
    messagesViewer.setLabelProvider( new StatusLabelProvider() );
    messagesViewer.setContentProvider( new StatusContentProvider() );
    messagesViewer.setInput( m_messages );
    messagesViewer.addDoubleClickListener( new StatusDoubleClickListener( m_title ) );
  }

  /**
   * This function returns a text representation for the severity of the given status.
   * 
   * @param status
   *          A status.
   * @return A text representation for the severity of the given status.
   */
  private String getText( IStatus status )
  {
    int severity = status.getSeverity();

    switch( severity )
    {
      case IStatus.OK:
        return "OK";
      case IStatus.INFO:
        return "Information";
      case IStatus.WARNING:
        return "Warnung";
      case IStatus.ERROR:
        return "Fehler";
      case IStatus.CANCEL:
        return "Abbruch";
      default:
        return "Information";
    }
  }
}