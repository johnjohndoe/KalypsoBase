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
package org.kalypso.core.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

/**
 * A dialog showing a status in full details.
 * 
 * @author Gernot Belger
 */
public class StatusDialog extends AbstractStatusDialog
{
  private boolean m_showAsTree;

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

  /**
   * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createCustomArea( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    composite.setLayout( new GridLayout() );

    final IStatus status = getStatus();
    createExceptionControl( parent, status );

    createStatusControl( parent, status );

    return composite;
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
    stackText.setEnabled( true );
  }

  private void createStatusControl( final Composite parent, final IStatus status )
  {
    final IStatus[] children = status.getChildren();
    if( children == null || children.length == 0 )
      return;

    final ColumnViewer columnViewer = createViewer( parent );

    final Control viewerControl = columnViewer.getControl();
    final GridData viewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
// viewerData.widthHint = 200;
// viewerData.heightHint = 100;
    viewerControl.setLayoutData( viewerData );

    if( columnViewer instanceof TreeViewer )
      StatusLabelProvider.addNavigationColumn( columnViewer );
    StatusLabelProvider.addSeverityColumn( columnViewer );
    StatusLabelProvider.addMessageColumn( columnViewer );
    StatusLabelProvider.addTimeColumn( columnViewer );

    if( columnViewer instanceof TreeViewer )
      columnViewer.setContentProvider( new StatusTreeContentProvider() );
    else
      columnViewer.setContentProvider( new ArrayContentProvider() );
    columnViewer.setInput( children );

    columnViewer.addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        final IStatus selection = (IStatus) sel.getFirstElement();
        if( selection != null )
        {
          final StatusDialog dialog = new StatusDialog( getShell(), selection, "Details" ); //$NON-NLS-1$
          dialog.open();
        }
      }
    } );
  }

  private ColumnViewer createViewer( final Composite parent )
  {
    final int viewerStyle = SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL;

    if( m_showAsTree )
    {
      final TreeViewer treeViewer = new TreeViewer( parent, viewerStyle );
      final Tree tree = treeViewer.getTree();
      tree.setHeaderVisible( true );
      tree.setLinesVisible( true );
      return treeViewer;
    }
    else
    {
      final TableViewer tableViewer = new TableViewer( parent, viewerStyle );
      final Table table = tableViewer.getTable();
      table.setHeaderVisible( true );
      table.setLinesVisible( true );
      return tableViewer;
    }
  }
}