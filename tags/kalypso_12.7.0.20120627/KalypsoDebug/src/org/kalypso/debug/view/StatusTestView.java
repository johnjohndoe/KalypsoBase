/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.debug.view;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.core.status.StatusDialog2;

/**
 * @author Gernot Belger
 */
public class StatusTestView extends ViewPart
{
  private final Exception m_cause = new Exception();

  final Exception m_exception = new Exception( "Ganz schlimm!", m_cause );

  private final IStatus PLAIN_OK = Status.OK_STATUS;

  private final IStatus PLAIN_INFO = new Status( IStatus.INFO, KalypsoCorePlugin.getID(), "For your information" );

  private final IStatus PLAIN_WARNING = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), "be careful" );

  private final IStatus PLAIN_ERROR = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Ouch!" );

  private final IStatus PLAIN_CANCEL = Status.CANCEL_STATUS;

  private final IStatus EXCEPTION_OK = new Status( IStatus.OK, KalypsoCorePlugin.getID(), "Success!", m_exception );

  private final IStatus EXCEPTION_INFO = new Status( IStatus.INFO, KalypsoCorePlugin.getID(), "For your information", m_exception );

  private final IStatus EXCEPTION_WARNING = new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), "be careful", m_exception );

  private final IStatus EXCEPTION_ERROR = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), "Ouch!", m_exception );

  private final IStatus EXCEPTION_CANCEL = new Status( IStatus.CANCEL, KalypsoCorePlugin.getID(), "Forget it....", m_exception );

  public StatusTestView( )
  {
    m_cause.fillInStackTrace();
    m_exception.fillInStackTrace();
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( panel );

    // Plain stati
    createStatusButtons( panel, PLAIN_OK );
    createStatusButtons( panel, PLAIN_INFO );
    createStatusButtons( panel, PLAIN_WARNING );
    createStatusButtons( panel, PLAIN_ERROR );
    createStatusButtons( panel, PLAIN_CANCEL );

    new Label( panel, SWT.SEPARATOR | SWT.HORIZONTAL ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    // the same with an exception
    createStatusButtons( panel, EXCEPTION_OK );
    createStatusButtons( panel, EXCEPTION_INFO );
    createStatusButtons( panel, EXCEPTION_WARNING );
    createStatusButtons( panel, EXCEPTION_ERROR );
    createStatusButtons( panel, EXCEPTION_CANCEL );

    new Label( panel, SWT.SEPARATOR | SWT.HORIZONTAL ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    // with sub stati
    createStatusButtons( panel, createMulti( PLAIN_OK, EXCEPTION_OK ) );
    createStatusButtons( panel, createMulti( PLAIN_INFO, PLAIN_INFO, PLAIN_OK ) );
    createStatusButtons( panel, createMulti( PLAIN_WARNING, PLAIN_OK, PLAIN_WARNING ) );
    createStatusButtons( panel, createMulti( PLAIN_ERROR, PLAIN_ERROR, PLAIN_WARNING, PLAIN_INFO ) );
    createStatusButtons( panel, createMulti( PLAIN_CANCEL, PLAIN_OK, PLAIN_CANCEL ) );

    new Label( panel, SWT.SEPARATOR | SWT.HORIZONTAL ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    // multi-status+eception
    createStatusButtons( panel, createMulti( EXCEPTION_OK, EXCEPTION_OK, EXCEPTION_OK ) );
    createStatusButtons( panel, createMulti( EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_OK ) );
    createStatusButtons( panel, createMulti( EXCEPTION_WARNING, EXCEPTION_OK, EXCEPTION_WARNING ) );
    createStatusButtons( panel, createMulti( EXCEPTION_ERROR, EXCEPTION_ERROR, EXCEPTION_WARNING, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO, EXCEPTION_INFO ) );
    createStatusButtons( panel, createMulti( EXCEPTION_CANCEL, EXCEPTION_OK, EXCEPTION_CANCEL ) );

    new Label( panel, SWT.SEPARATOR | SWT.HORIZONTAL ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    final IStatus treeChildStatus = createMulti( EXCEPTION_WARNING, EXCEPTION_OK, EXCEPTION_WARNING );
    final IStatus treeStatus = createMulti( EXCEPTION_CANCEL, EXCEPTION_OK, EXCEPTION_CANCEL, treeChildStatus, EXCEPTION_WARNING, EXCEPTION_ERROR );
    createStatusButtons( panel, treeStatus );
  }

  @Override
  public void setFocus( )
  {
  }

  private void createStatusButtons( final Composite parent, final IStatus status )
  {
    createStatusButton( parent, status );
    createStatus2Button( parent, status );
  }

  private void createStatusButton( final Composite parent, final IStatus status )
  {
    final Shell shell = parent.getShell();

    final Button button = new Button( parent, SWT.PUSH );
    button.setText( status.getMessage() );
    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        openStatusDialog( shell, status );
      }
    } );
  }

  private void createStatus2Button( final Composite parent, final IStatus status )
  {
    final Shell shell = parent.getShell();

    final Button button = new Button( parent, SWT.PUSH );
    button.setText( status.getMessage() );
    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        openStatusDialog2( shell, status );
      }
    } );
  }

  protected void openStatusDialog( final Shell shell, final IStatus status )
  {
    new StatusDialog( shell, status, "Titel" ).open();
  }

  protected void openStatusDialog2( final Shell shell, final IStatus status )
  {
    new StatusDialog2( shell, status, "Titel", "Meine Message" ).open();
  }

  private IStatus createMulti( final IStatus status, final IStatus... subStati )
  {
    return new MultiStatus( KalypsoCorePlugin.getID(), 0, subStati, status.getMessage(), status.getException() );
  }
}
