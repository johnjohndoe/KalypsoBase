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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;

/**
 * This listener resizes the columns of a table to match its borders, if the table is resized.
 * 
 * @author Holger Albert
 */
public class ColumnsResizeControlListener extends ControlAdapter
{
  /** If set to a column, the column will always get this fixed width */
  public static final String DATA_FIXED_COL_WIDTH = ColumnsResizeControlListener.class.getName() + ".fixedColumnWidth"; //$NON-NLS-1$

  /** Defines the minimal column width of each table column with this data-entry */
  public static final String DATA_MIN_COL_WIDTH = ColumnsResizeControlListener.class.getName() + ".minColumnWidth"; //$NON-NLS-1$

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

  private Table m_table;

  public ColumnsResizeControlListener( )
  {
    m_resizeColumnsJob.setSystem( true );
  }

  @Override
  public void controlResized( final ControlEvent e )
  {
    if( !(e.widget instanceof Table) )
      return;

    if( m_table == null )
      m_table = (Table) e.widget;

    if( m_isActive )
      return;

    // In order to handle many resize events in short time, we schedule the real update into a job
    m_resizeColumnsJob.cancel();
    m_resizeColumnsJob.schedule( 50 );
  }

  protected void resizeColumns( )
  {
    if( m_table.isDisposed() )
      return;

    try
    {
      m_isActive = true;

      /* Get the area of the parent. */
      final Rectangle area = m_table.getClientArea();

      final int width = area.width;

      /* Set the size for each colum. */
      final TableColumn[] columns = m_table.getColumns();
      int remainingWidth = width;

      for( int i = 0; i < columns.length; i++ )
      {
        final TableColumn column = columns[i];

        final int fixedWidth = getFixedWidth( column );
        if( fixedWidth != -1 )
        {
          column.setWidth( fixedWidth );
          remainingWidth -= fixedWidth;
        }
        else
        {
          final int minColWidth = getMinimumColumnWidth( column );

          if( remainingWidth > 0 )
          {
            final int remainingColumns = columns.length - i;
            final int columnWidth = Math.max( minColWidth, remainingWidth / remainingColumns );
            column.setWidth( columnWidth );
            remainingWidth -= columnWidth;
          }
          else
            column.setWidth( minColWidth );
        }
      }
    }
    finally
    {
      m_isActive = false;
    }
  }

  private int getFixedWidth( final TableColumn column )
  {
    final Object data = column.getData( DATA_FIXED_COL_WIDTH );
    if( data instanceof Integer )
      return (Integer) data;

    return -1;
  }

  private int getMinimumColumnWidth( final TableColumn column )
  {
    final Object data = column.getData( DATA_MIN_COL_WIDTH );
    if( data instanceof Integer )
    {
      final int minColWidth = (Integer) data;
      if( minColWidth == MIN_COL_WIDTH_PACK )
        return calculatePack( column );
      else
        return Math.max( 0, minColWidth );
    }

    return 0;
  }

  private int calculatePack( final TableColumn column )
  {
    column.pack();
    return column.getWidth();
  }
}