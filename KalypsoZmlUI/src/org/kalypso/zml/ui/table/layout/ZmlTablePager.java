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
package org.kalypso.zml.ui.table.layout;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.IZmlTable;

/**
 * @author Dirk Kuch
 */
public class ZmlTablePager
{
  private Date m_index;

  protected final IZmlTable m_table;

  private IStructuredSelection m_selection;

  private boolean m_firstRun = true;

  public ZmlTablePager( final IZmlTable table )
  {
    m_table = table;
  }

  public void update( )
  {
    final TableViewer viewer = m_table.getViewer();
    setIndex( viewer );
    setSelection( viewer );
  }

  private void setSelection( final TableViewer viewer )
  {
    final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    if( Objects.isNull( m_selection ) )
    {
      m_selection = selection;
    }
    else if( Objects.isNull( selection ) && selection.isEmpty() )
      return;

    m_selection = selection;

  }

  private void setIndex( final TableViewer viewer )
  {

    Date date = null;
    if( m_firstRun )
    {
      date = findForecastDate();
      if( Objects.isNotNull( date ) )
        m_firstRun = false;
    }

    if( Objects.isNull( date ) )
    {
      date = getIndex( viewer );
    }

    if( Objects.isNotNull( date ) )
      m_index = date;
  }

  private Date getIndex( final TableViewer viewer )
  {
    final Rectangle bounds = viewer.getControl().getBounds();
    if( bounds.width <= 0 || bounds.height <= 0 )
      return null;

    /** strategy: try to find last visible cell */
    final Set<Point> grep = new LinkedHashSet<Point>();

    int ptr = bounds.height;
    while( ptr > 0 )
    {
      ptr -= 40; // magic number!
      grep.add( new Point( 10, ptr ) );
    }

    final ViewerCell cell = findCell( viewer, grep.toArray( new Point[] {} ) );
    if( Objects.isNull( cell ) )
      return null;

    final Object element = cell.getElement();
    if( !(element instanceof IZmlModelRow) )
      return null;

    final IZmlModelRow row = (IZmlModelRow) element;
    final Date date = row.getIndexValue();
    final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    calendar.setTime( date );
    calendar.add( Calendar.HOUR_OF_DAY, +1 );

    return calendar.getTime();
  }

  private ViewerCell findCell( final TableViewer viewer, final Point... points )
  {
    for( final Point point : points )
    {
      final ViewerCell cell = viewer.getCell( point );
      if( Objects.isNotNull( cell ) )
        return cell;
    }

    return null;
  }

  public void reveal( )
  {
    final TableViewer viewer = m_table.getViewer();
    if( !m_selection.isEmpty() )
      viewer.setSelection( m_selection );

    if( Objects.isNull( m_index ) )
      return;

    final ClosestDateVisitor visitor = new ClosestDateVisitor( m_index );
    m_table.accept( visitor );

    final IZmlModelRow row = visitor.getModelRow();
    if( Objects.isNull( row ) )
      return;

    viewer.reveal( row );

    // FIXME AbstractCellCursor has to listen to reveal events
    new UIJob( "" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        m_table.getFocusHandler().getCursor().redraw();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  private Date findForecastDate( )
  {
    final IZmlModel model = m_table.getDataModel();
    final IZmlModelColumn[] columns = model.getColumns();
    for( final IZmlModelColumn column : columns )
    {
      if( column.isMetadataSource() && column.isActive() )
      {
        final Date date = findForecastDate( column );
        if( Objects.isNotNull( date ) )
          return date;
      }
    }

    for( final IZmlModelColumn column : columns )
    {
      if( !column.isActive() )
        continue;

      final Date date = findForecastDate( column );
      if( Objects.isNotNull( date ) )
        return date;
    }

    return null;
  }

  private Date findForecastDate( final IZmlModelColumn column )
  {
    final MetadataList metadata = column.getMetadata();
    return MetadataHelper.getForecastStart( metadata );
  }

}
