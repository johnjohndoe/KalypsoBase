/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.view;

import java.awt.Frame;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.cache.ObservationCache;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.ui.repository.view.RepositoryExplorerPart;

/**
 * Table QuickView.
 * 
 * @author schlienger
 */
public class TableViewPart extends ViewPart implements ISelectionChangedListener, IPartListener
{
  public static final String ID = "org.kalypso.ogc.sensor.view.TableViewPart"; //$NON-NLS-1$

  private final TableView m_tableView;

  private ObservationTable m_table;

  public TableViewPart( )
  {
    m_tableView = new TableView();
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    m_table = new ObservationTable( m_tableView, false, false );

    // SWT-AWT Br�cke f�r die Darstellung von JTable
    final Frame vFrame = SWT_AWT.new_Frame( new Composite( parent, SWT.RIGHT | SWT.EMBEDDED ) );

    vFrame.setVisible( true );
    m_table.setVisible( true );

    vFrame.add( m_table );

    getSite().getPage().addPartListener( this );
  }

  @Override
  public void dispose( )
  {
    getSite().getPage().removePartListener( this );

    if( m_table != null )
      m_table.dispose();

    m_tableView.dispose();

    super.dispose();
  }

  @Override
  public void setFocus( )
  {
    // noch nix
  }

  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    // always remove items first (we don't know which selection we get)
    m_tableView.removeAllItems();

    final StructuredSelection selection = (StructuredSelection) event.getSelection();

    if( !(selection.getFirstElement() instanceof IRepositoryItem) )
      return;

    final IRepositoryItem item = (IRepositoryItem) selection.getFirstElement();

    final IObservation obs = ObservationCache.getInstance().getObservationFor( item );
    if( obs != null )
    {
      final PlainObsProvider provider = new PlainObsProvider( obs, new ObservationRequest( ObservationViewHelper.makeDateRange( item ) ) );
      m_tableView.addObservation( provider, ObservationTokenHelper.DEFAULT_ITEM_NAME, new ObsView.ItemData( false, null, null, true ) );
    }
  }

  @Override
  public void partActivated( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).addSelectionChangedListener( this );
  }

  @Override
  public void partBroughtToTop( final IWorkbenchPart part )
  {
    // nada
  }

  @Override
  public void partClosed( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).removeSelectionChangedListener( this );
  }

  @Override
  public void partDeactivated( final IWorkbenchPart part )
  {
    if( part != null && part instanceof RepositoryExplorerPart )
      ((RepositoryExplorerPart) part).removeSelectionChangedListener( this );
  }

  @Override
  public void partOpened( final IWorkbenchPart part )
  {
    // Siehe partActivated...
  }
}