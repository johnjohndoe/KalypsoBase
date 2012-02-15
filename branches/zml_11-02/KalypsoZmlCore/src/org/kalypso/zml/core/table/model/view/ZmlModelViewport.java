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
package org.kalypso.zml.core.table.model.view;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.model.IZmlColumnModelListener;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModelColumn;
import org.kalypso.zml.core.table.model.editing.ContinuedInterpolatedValueEditingStrategy;
import org.kalypso.zml.core.table.model.editing.IZmlEditingStrategy;
import org.kalypso.zml.core.table.model.editing.InterpolatedValueEditingStrategy;
import org.kalypso.zml.core.table.model.editing.SumValueEditingStrategy;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelViewport
{
  Set<IZmlColumnModelListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IZmlColumnModelListener>() );

  protected final Set<String> m_hiddenTypes = Collections.synchronizedSet( new HashSet<String>() ); // FIXME

  // FIXME set as zml table base
  // FIXME IZmlModelListener change listener
  // FIXME caching
  // FIXME zml time filter

  private final IZmlModel m_model;

  private final ZmlViewResolutionFilter m_filter;

  public ZmlModelViewport( final IZmlModel model )
  {
    m_model = model;
    m_filter = new ZmlViewResolutionFilter( this );

    // dirty only for visible columns
    // row caching
// m_model.addListener( IZmlColumnModelListener(){} );
  }

  public IZmlModelValueCell getCell( final IZmlModelRow row, final int columnIndex )
  {
    if( columnIndex == -1 )
      return null;

    final ZmlModelColumn[] columns = m_model.getActiveColumns();
    if( ArrayUtils.getLength( columns ) < columnIndex )
      return null;

    final ZmlModelColumn column = columns[columnIndex];
    return row.get( column );
  }

  public IZmlModelColumn[] getColumns( )
  {
    final Set<IZmlModelColumn> collection = new LinkedHashSet<IZmlModelColumn>();
    final ZmlModelColumn[] columns = m_model.getActiveColumns();
    for( final ZmlModelColumn column : columns )
    {
      final DataColumnType type = column.getDataColumn().getType();
      if( m_hiddenTypes.contains( type.getValueAxis() ) )
        continue;

      collection.add( column );
    }

    return collection.toArray( new IZmlModelColumn[] {} );
  }

  public IZmlModelColumn getColum( final int index )
  {
    if( index == -1 )
      return null;

    final IZmlModelColumn[] columns = getColumns();
    if( ArrayUtils.getLength( columns ) < index )
      return null;

    return columns[index];
  }

  public IZmlModelRow[] getRows( )
  {
    final Set<IZmlModelRow> collection = new LinkedHashSet<IZmlModelRow>();

    final IZmlModelRow[] rows = m_model.getRows();
    for( final IZmlModelRow row : rows )
    {
      if( m_filter.select( row ) )
        collection.add( row );
    }

    return collection.toArray( new IZmlModelRow[] {} );
  }

  public IZmlModelValueCell getCell( final int rowIndex, final int columnIndex )
  {
    if( rowIndex == -1 || columnIndex == -1 )
      return null;

    final IZmlModelRow row = getRow( rowIndex );
    final IZmlModelColumn column = getColum( columnIndex );

    if( Objects.isNotNull( row, column ) )
      return row.get( column );

    return null;
  }

  public IZmlModelRow getRow( final int rowIndex )
  {
    if( rowIndex == -1 )
      return null;

    final IZmlModelRow[] rows = getRows();

    if( ArrayUtils.getLength( rows ) < rowIndex )
      return null;

    return rows[rowIndex];
  }

  public IZmlModelValueCell findPreviousCell( final IZmlModelValueCell current )
  {
    throw new UnsupportedOperationException();
  }

  public IZmlModelValueCell findNextCell( final IZmlModelValueCell current )
  {
    throw new UnsupportedOperationException();
  }

  public int getResolution( )
  {
    return getFilter().getResolution();
  }

  public IZmlEditingStrategy getEditingStrategy( final IZmlModelColumn column )
  {
    final AbstractColumnType type = column.getDataColumn().getType();
    if( type instanceof IndexColumnType )
      return null;
    else
    {
      final DataColumnType dataColumnType = (DataColumnType) type;
      if( ITimeseriesConstants.TYPE_RAINFALL.equals( dataColumnType.getValueAxis() ) )
        return new SumValueEditingStrategy( this );
      else if( ITimeseriesConstants.TYPE_WECHMANN_E.equals( dataColumnType.getValueAxis() ) )
        return new ContinuedInterpolatedValueEditingStrategy( this );
      else
        return new InterpolatedValueEditingStrategy( this );
    }
  }

  public ZmlViewResolutionFilter getFilter( )
  {
    return m_filter;
  }

  public void addListener( final IZmlColumnModelListener listener )
  {
    m_listeners.add( listener );
  }

  public void fireModelChanged( )
  {
    final IZmlColumnModelListener[] listeners = m_listeners.toArray( new IZmlColumnModelListener[] {} );
    for( final IZmlColumnModelListener listener : listeners )
    {
      listener.modelChanged();
    }
  }

  public IZmlModelValueCell[] getCells( final IZmlModelColumn column )
  {
    final Set<IZmlModelValueCell> cells = new LinkedHashSet<IZmlModelValueCell>();

    final IZmlModelRow[] rows = getRows();
    for( final IZmlModelRow row : rows )
    {
      cells.add( row.get( column ) );
    }

    return cells.toArray( new IZmlModelValueCell[] {} );
  }

  public IZmlModel getModel( )
  {
    return m_model;
  }

  public void setVisible( final String type, final boolean hide )
  {
    if( hide )
      m_hiddenTypes.remove( type );
    else
      m_hiddenTypes.add( type );

    // TODO model change event
  }
}
