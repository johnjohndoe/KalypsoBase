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
package org.kalypso.zml.ui.table.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.table.IZmlTableColumn;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public class ZmlColumnRegistry
{
  private final Set<ZmlColumnLoadCommand> m_commands = new HashSet<ZmlColumnLoadCommand>();

  private final List<ZmlTableColumn> m_columns = new ArrayList<ZmlTableColumn>();

  private final ZmlTableType m_type;

  private final IZmlTableComposite m_table;

  public ZmlColumnRegistry( final IZmlTableComposite table, final ZmlTableType type )
  {
    m_table = table;
    m_type = type;
  }

  public IZmlTableComposite getTable( )
  {
    return m_table;
  }

  public void loadColumn( final IZmlTableColumn column )
  {
    m_commands.add( new ZmlColumnLoadCommand( this, column ) );
  }

  protected void addColumn( final ZmlTableColumn column )
  {
    m_columns.add( column );

    new UIJob( "" )
    {

      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        getTable().refresh();

        return Status.OK_STATUS;
      }
    }.schedule();

  }

  public ZmlTableColumn[] getColumns( )
  {
    return m_columns.toArray( new ZmlTableColumn[] {} );
  }

  public void clean( )
  {
    final ZmlTableColumn[] columns;
    final ZmlColumnLoadCommand[] commands;
    synchronized( this )
    {
      columns = m_columns.toArray( new ZmlTableColumn[] {} );
      m_columns.clear();

      commands = m_commands.toArray( new ZmlColumnLoadCommand[] {} );
      m_commands.clear();
    }

    for( final ZmlTableColumn column : columns )
    {
      column.dispose();
    }

    for( final ZmlColumnLoadCommand command : commands )
    {
      command.cancel();
    }
  }

  public ZmlTableRow[] getInput( ) throws SensorException
  {
    // TODO always date?!?
    final Map<Object, ZmlTableRow> map = new TreeMap<Object, ZmlTableRow>();

    for( final ZmlTableColumn column : getColumns() )
    {
      final IAxis indexAxis = column.getIndexAxis();

      for( int i = 0; i < column.size(); i++ )
      {
        final Object index = column.get( i, indexAxis );

        ZmlTableRow structure = map.get( index );
        if( structure == null )
        {
          structure = new ZmlTableRow( index );
          map.put( index, structure );
        }

        final ZmlValueReference reference = new ZmlValueReference( column, i );

        structure.add( reference );
      }
    }

    return map.values().toArray( new ZmlTableRow[] {} );
  }

  public DataColumnType getDataColumnType( final String id )
  {
    final List<AbstractColumnType> columns = m_type.getColumns().getColumn();
    for( final AbstractColumnType column : columns )
    {
      if( column.getId().equals( id ) )
        return (DataColumnType) column;
    }

    return null;
  }

  public ZmlTableColumn getColumn( final String id )
  {
    for( final ZmlTableColumn column : m_columns )
    {
      if( column.getIdentifier().equals( id ) )
        return column;
    }

    return null;
  }

}
