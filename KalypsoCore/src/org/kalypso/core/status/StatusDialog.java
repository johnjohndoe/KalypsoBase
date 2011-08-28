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
package org.kalypso.core.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog showing a status in full details.<br>
 * TODO:
 * <ul>
 * <li>Compactify exceptioncontrol, add details button for exceptions</li>
 * <li>'send mail' button</li>
 * <li>'copy to clipboard' button</li>
 * <li>toolbar for tree: collapse all, expand all</li>
 * </ul>
 * 
 * @author Gernot Belger
 */
public class StatusDialog extends AbstractStatusDialog
{
  private boolean m_showAsTree = true;

  public StatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle )
  {
    super( parentShell, status, dialogTitle );
  }

  public StatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle, final String[] dialogButtonLabels, final int defaultIndex )
  {
    super( parentShell, status, dialogTitle, dialogButtonLabels, defaultIndex );
  }

  /**
   * Set to <code>true</code>, if the dialog should show the status elements in a tree viewer. Else they are shown in a
   * table viewer (default).
   */
  public void setShowAsTree( final boolean showAsTree )
  {
    m_showAsTree = showAsTree;
  }

  @Override
  protected Control createCustomArea( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    GridLayoutFactory.fillDefaults().applyTo( composite );

    final IStatus status = getStatus();

    // TODO: the exception control is not nice for normal users -> put into popup
    // TODO: Layout problem: table might suppress exception panel completely :-(
    createExceptionControl( composite, status );

    createStatusControl( composite, status );

    return composite;
  }

  @Override
  protected boolean isResizable( )
  {
    return true;
  }

  private void createExceptionControl( final Composite parent, final IStatus status )
  {
    final Throwable exception = status.getException();
    if( exception == null )
      return;

    final StringWriter sw = new StringWriter();
    exception.printStackTrace( new PrintWriter( sw ) );

    final Group exceptionGroup = new Group( parent, SWT.NONE );
    exceptionGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    exceptionGroup.setLayout( new GridLayout() );
    exceptionGroup.setText( "Exception" ); //$NON-NLS-1$

    final String excMsg = exception.getLocalizedMessage();
    if( excMsg != null )
    {
      final Text msgLabel = new Text( exceptionGroup, SWT.READ_ONLY );
      msgLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
      msgLabel.setText( excMsg );
    }

    final Text stackText = new Text( exceptionGroup, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
    final GridData stackLayoutData = new GridData( SWT.FILL, SWT.FILL, true, true );
    stackLayoutData.widthHint = 100;
    stackLayoutData.heightHint = 100;
    stackText.setLayoutData( stackLayoutData );
    stackText.setText( sw.toString() );
  }

  private void createStatusControl( final Composite parent, final IStatus status )
  {
    final IStatus[] children = status.getChildren();
    if( children == null || children.length == 0 )
      return;

    if( children.length == 1 )
    {
      final StatusComposite statusPanel = new StatusComposite( parent, StatusComposite.DETAILS );
      statusPanel.setStatus( children[0] );
      statusPanel.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
      return;
    }

    final StatusViewer viewer = createViewer( parent );
    final GridData viewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    // Protect against vanishing...
    viewerData.minimumHeight = 100;
    // ...and too many children
    viewerData.heightHint = 100;
    viewer.getControl().setLayoutData( viewerData );

    final boolean showTimeColumn = StatusViewer.hasTime( children );
    if( showTimeColumn )
      viewer.addTimeColumn();

    viewer.setInput( children );
  }

  private StatusViewer createViewer( final Composite parent )
  {
    if( m_showAsTree )
      return new StatusTreeViewer( parent, SWT.BORDER | SWT.FULL_SELECTION );
    else
      return new StatusTableViewer( parent, SWT.BORDER | SWT.FULL_SELECTION );
  }
}