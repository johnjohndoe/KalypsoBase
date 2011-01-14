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
package org.kalypso.zml.core.table.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.zml.core.table.IZmlTableElement;
import org.kalypso.zml.core.table.model.loader.ZmlColumnLoadCommand;
import org.kalypso.zml.core.table.model.loader.ZmlModelBuilder;
import org.kalypso.zml.core.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public class ZmlModel implements IZmlModel
{
  private final List<ZmlModelColumn> m_columns = new ArrayList<ZmlModelColumn>();

  private final Set<ZmlColumnLoadCommand> m_commands = new HashSet<ZmlColumnLoadCommand>();

  private final Set<IZmlColumnModelListener> m_listeners = new HashSet<IZmlColumnModelListener>();

  private final Map<Object, IZmlModelRow> m_rows = new TreeMap<Object, IZmlModelRow>();

  private boolean m_rowsDirty = true;

  private final ZmlTableType m_type;

  public ZmlModel( final ZmlTableType type )
  {
    m_type = type;
  }

  public void accept( final IZmlModelColumnVisitor visitor )
  {
    for( final ZmlModelColumn column : m_columns )
    {
      visitor.visit( column );
    }
  }

  public void accept( final IZmlModelRowVisitor visitor )
  {
    final Set<Entry<Object, IZmlModelRow>> entries = m_rows.entrySet();
    for( final Entry<Object, IZmlModelRow> entry : entries )
    {
      visitor.visit( entry.getValue() );
    }
  }

  public void add( final IZmlModelRow row )
  {
    synchronized( m_rows )
    {
      m_rows.put( row.getIndexValue(), row );
    }
  }

  public void addColumn( final ZmlModelColumn column )
  {
    m_columns.add( column );

    fireModelChanged();
  }

  @Override
  public void addListener( final IZmlColumnModelListener listener )
  {
    m_listeners.add( listener );
  }

  public void clean( )
  {
    final ZmlModelColumn[] columns;
    final ZmlColumnLoadCommand[] commands;

    synchronized( this )
    {
      columns = m_columns.toArray( new ZmlModelColumn[] {} );
      m_columns.clear();

      commands = m_commands.toArray( new ZmlColumnLoadCommand[] {} );
      m_commands.clear();

      m_rows.clear();
    }

    for( final ZmlModelColumn column : columns )
    {
      column.dispose();
    }

    for( final ZmlColumnLoadCommand command : commands )
    {
      command.cancel();
    }

    fireModelChanged();
  }

  @Override
  public void fireModelChanged( )
  {
    m_rowsDirty = true;

    final IZmlColumnModelListener[] listeners = m_listeners.toArray( new IZmlColumnModelListener[] {} );
    for( final IZmlColumnModelListener listener : listeners )
    {
      listener.modelChanged();
    }
  }

  @Override
  public ZmlModelColumn getColumn( final String id )
  {
    for( final ZmlModelColumn column : m_columns )
    {
      if( column.getIdentifier().equals( id ) )
        return column;
    }

    return null;
  }

  @Override
  public ZmlModelColumn[] getColumns( )
  {
    return m_columns.toArray( new ZmlModelColumn[] {} );
  }

  @Override
  public IZmlModelRow getRow( final Object index )
  {
    if( m_rowsDirty )
    {
      synchronized( this )
      {
        final ZmlModelBuilder builder = new ZmlModelBuilder( this );
        ProgressUtilities.busyCursorWhile( builder );

        m_rowsDirty = false;
      }
    }

    return m_rows.get( index );
  }

  /**
   * @see org.kalypso.zml.core.table.model.IZmlModel#getRowAt(int)
   */
  @Override
  public IZmlModelRow getRowAt( final int index )
  {
    final IZmlModelRow[] rows = getRows();
    if( index >= rows.length )
      return null;

    return rows[index];
  }

  /**
   * @see org.kalypso.zml.ui.table.model.IZmlDataModel#getRows()
   */
  @Override
  public IZmlModelRow[] getRows( )
  {
    synchronized( this )
    {
      if( m_rowsDirty )
      {
        final ZmlModelBuilder builder = new ZmlModelBuilder( this );
        builder.execute( new NullProgressMonitor() );

        m_rowsDirty = false;
      }

      return m_rows.values().toArray( new IZmlModelRow[] {} );
    }
  }

  @Override
  public ZmlTableType getTableType( )
  {
    return m_type;
  }

  public void loadColumn( final IZmlTableElement column )
  {
    m_commands.add( new ZmlColumnLoadCommand( this, column ) );
  }
}
