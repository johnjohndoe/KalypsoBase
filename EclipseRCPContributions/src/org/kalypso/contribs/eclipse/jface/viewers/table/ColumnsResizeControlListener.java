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
package org.kalypso.contribs.eclipse.jface.viewers.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.progress.UIJob;

/**
 * This listener resizes the columns of a table to match its borders, if the table is resized.
 * 
 * @author Holger Albert
 */
public class ColumnsResizeControlListener extends ControlAdapter
{
  private static final String DATA_WIDTH_INFO = "columnWidthInfo"; //$NON-NLS-1$

  public static final int MIN_COL_WIDTH_PACK = -2;

  private final UIJob m_resizeColumnsJob = new UIJob( "Resize columns" ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      resizeColumns();
      monitor.done();
      return Status.OK_STATUS;
    }
  };

  private boolean m_isActive;

  private Composite m_tableOrTree;

  public ColumnsResizeControlListener( )
  {
    m_resizeColumnsJob.setSystem( true );
  }

  @Override
  public void controlResized( final ControlEvent e )
  {
    if( !(e.widget instanceof Table) && !(e.widget instanceof Tree) )
      return;

    if( m_tableOrTree == null )
      m_tableOrTree = (Composite) e.widget;

    if( m_isActive )
      return;

    updateColumnSizes();
  }

  public void updateColumnSizes( )
  {
    // In order to handle many resize events in short time, we schedule the real update into a job
    m_resizeColumnsJob.cancel();
    // REMARK: using a long delay here also protects against strange effect in combinasion with
    // scrolled forms.
    m_resizeColumnsJob.schedule( 250 );
  }

  void resizeColumns( )
  {
    if( m_tableOrTree == null || m_tableOrTree.isDisposed() )
      return;

    try
    {
      m_isActive = true;

      doRefreshColumnsWidth( m_tableOrTree );
    }
    finally
    {
      m_isActive = false;
    }
  }

  public static void refreshColumnsWidth( final Table table )
  {
    doRefreshColumnsWidth( table );
  }

  public static void refreshColumnsWidth( final Tree tree )
  {
    doRefreshColumnsWidth( tree );
  }

  private static void doRefreshColumnsWidth( final Composite tableOrTree )
  {
    /* Set the size for each colum. */
    final Item[] columns = getItems( tableOrTree );
    final ColumnWidthInfo[] infos = new ColumnWidthInfo[columns.length];
    for( int i = 0; i < columns.length; i++ )
      infos[i] = updateWidthInfo( columns[i] );

    /* Get the area of the parent. */
    final Rectangle area = tableOrTree.getClientArea();
    final int width = area.width;

    final ColumnResizeUpdater updater = new ColumnResizeUpdater( width, infos );
    updater.updateColumnsWidth();
  }

  /**
   * Make sure all items have a info.
   */
  private static ColumnWidthInfo updateWidthInfo( final Item item )
  {
    final Object info = item.getData( DATA_WIDTH_INFO );
    if( info instanceof ColumnWidthInfo )
      return (ColumnWidthInfo) info;

    final ColumnWidthInfo newInfo = new ColumnWidthInfo( item );
    item.setData( DATA_WIDTH_INFO, newInfo );
    return newInfo;
  }

  private static Item[] getItems( final Composite tableOrTree )
  {
    if( tableOrTree instanceof Table )
      return ((Table) tableOrTree).getColumns();

    if( tableOrTree instanceof Tree )
      return ((Tree) tableOrTree).getColumns();

    throw new IllegalArgumentException();
  }

  public static void setMinimumWidth( final Item column, final int minimumWidth )
  {
    final ColumnWidthInfo info = new ColumnWidthInfo( column );
    info.setMinimumWidth( minimumWidth );
    column.setData( DATA_WIDTH_INFO, info );
  }

  public static void setMinimumPackWidth( final Item column )
  {
    final ColumnWidthInfo info = new ColumnWidthInfo( column );
    info.setMinimumWidth( ColumnWidthInfo.PACK );
    column.setData( DATA_WIDTH_INFO, info );
  }

  public static void setFixedWidth( final Item column, final int fixedWidth )
  {
    final ColumnWidthInfo info = new ColumnWidthInfo( column );
    info.setFixedWidth( fixedWidth );
    column.setData( DATA_WIDTH_INFO, info );
  }
}