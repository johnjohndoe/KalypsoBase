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
package org.kalypso.zml.ui.table.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableColumnVisitor;
import org.kalypso.zml.ui.table.IZmlTableRowVisitor;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.model.rows.IZmlTableValueRow;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractZmlTableModel implements IZmlTableModel
{
  protected final Set<IZmlTableColumn> m_columns = new LinkedHashSet<IZmlTableColumn>();

  protected List<IZmlTableRow> m_rows = new ArrayList<IZmlTableRow>();

  private final IZmlModel m_model;

  private final IZmlTable m_table;

  public AbstractZmlTableModel( final IZmlTable table, final IZmlModel model )
  {
    m_table = table;
    m_model = model;
  }

  @Override
  public IZmlModel getModel( )
  {
    return m_model;
  }

  @Override
  public IZmlTable getTable( )
  {
    return m_table;
  }

  @Override
  public void add( final IZmlTableColumn column )
  {
    // hmmm... fire columns changed?!?
    m_columns.add( column );
  }

  @Override
  public boolean isEmpty( )
  {
    if( m_columns.isEmpty() )
      return true;
    else if( ArrayUtils.getLength( m_rows ) == 0 ) // TODO correct?
      return true;

    return false;

  }

  @Override
  public IZmlTableColumn[] getColumns( )
  {
    return m_columns.toArray( new IZmlTableColumn[] {} );
  }

  @Override
  public void accept( final IZmlTableColumnVisitor visitor )
  {
    for( final IZmlTableColumn column : getColumns() )
    {
      visitor.visit( column );
    }
  }

  @Override
  public void accept( final IZmlTableRowVisitor visitor )
  {
    for( final IZmlTableRow row : getRows() )
    {
      try
      {
        if( row instanceof IZmlTableValueRow )
          visitor.visit( (IZmlTableValueRow) row );
      }
      catch( final CancelVisitorException e )
      {
        return;
      }
    }
  }

  @Override
  public IZmlTableRow getRow( final int index )
  {
    if( index < 0 )
      return null;

    final List<IZmlTableRow> rows = getRows();
    if( index < rows.size() )
      return rows.get( index );

    return null;
  }

  @Override
  public void reset( )
  {
    m_rows.clear();
  }

  @Override
  public IZmlTableColumn findColumn( final int columnIndex )
  {
    final IZmlTableColumn[] columns = getColumns();
    for( final IZmlTableColumn column : columns )
    {
      if( column.getIndex() == columnIndex )
        return column;
    }

    return null;
  }
}
