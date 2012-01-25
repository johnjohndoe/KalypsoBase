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
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A dialog showing a status in full details.
 * 
 * @author Gernot Belger
 */
public class StatusDialog extends AbstractStatusDialog
{
  private final Collection<IAction> m_actions = new ArrayList<IAction>();

  private boolean m_showAsTree;

  private boolean m_showTime = true;

  public static void open( final Shell parentShell, final IStatus status, final String dialogTitle )
  {
    new StatusDialog( parentShell, status, dialogTitle ).open();
  }

  public static void open( final Shell parentShell, final IStatus status, final String dialogTitle, final String[] dialogButtonLabels, final int defaultIndex )
  {
    new StatusDialog( parentShell, status, dialogTitle, dialogButtonLabels, defaultIndex ).open();
  }

  public StatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle )
  {
    super( parentShell, status, dialogTitle );
  }

  public StatusDialog( final Shell parentShell, final IStatus status, final String dialogTitle, final String[] dialogButtonLabels, final int defaultIndex )
  {
    super( parentShell, status, dialogTitle, dialogButtonLabels, defaultIndex );
  }

  public void setShowTimeTime( final boolean showTime )
  {
    m_showTime = showTime;
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
  protected Control createMessageArea( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    GridLayoutFactory.fillDefaults().numColumns( 3 ).applyTo( panel );

    super.createMessageArea( panel );

    final ToolBarManager toolBarManager = new ToolBarManager( SWT.HORIZONTAL );
    final ToolBar toolBar = toolBarManager.createControl( panel );

    toolBar.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false ) );

    for( final IAction additionalAction : m_actions )
      toolBarManager.add( additionalAction );

    // FIXME
    // toolBarManager.add( new MailStatusAction( getStatus() ) );
    toolBarManager.add( new CopyStatusClipboardAction( getStatus() ) );
    toolBarManager.update( true );

    return panel;
  }

  @Override
  protected Control createCustomArea( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    composite.setLayout( new GridLayout() );

    final IStatus status = getStatus();
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

    final String shortException = exception.toString();

    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter( sw );
    exception.printStackTrace( pw );
    pw.flush();

    final Section exceptionGroup = new Section( parent, ExpandableComposite.TREE_NODE );
    exceptionGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    exceptionGroup.setText( "Exception: " + shortException );

    final Text stackText = new Text( exceptionGroup, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL );
    stackText.setText( sw.toString() );
    stackText.setEnabled( true );

    exceptionGroup.setClient( stackText );

    exceptionGroup.addExpansionListener( new ExpansionAdapter()
    {
      @Override
      public void expansionStateChanged( final ExpansionEvent e )
      {
        // TODO: maybe more intelligent behaviour?
        getShell().pack();
      }
    } );
  }

  private void createStatusControl( final Composite parent, final IStatus status )
  {
    final IStatus[] children = status.getChildren();
    if( children == null || children.length == 0 )
      return;

    final ColumnViewer columnViewer = createViewer( parent );

    final Control viewerControl = columnViewer.getControl();
    viewerControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    if( columnViewer instanceof TreeViewer )
      StatusLabelProvider.addNavigationColumn( columnViewer );
    StatusLabelProvider.addSeverityColumn( columnViewer );
    StatusLabelProvider.addMessageColumn( columnViewer );
    if( m_showTime )
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

  /**
   * Adds an action to the toolbar of this dialog.<br/>
   * Must be called before the dialog is opened.
   */
  public void addAction( final IAction action )
  {
    m_actions.add( action );
  }
}