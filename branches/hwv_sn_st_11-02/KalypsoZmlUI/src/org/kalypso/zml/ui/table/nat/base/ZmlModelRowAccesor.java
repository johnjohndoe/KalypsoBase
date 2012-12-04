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
package org.kalypso.zml.ui.table.nat.base;

import net.sourceforge.nattable.data.IColumnAccessor;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.editing.IZmlEditingStrategy;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.labeling.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

/**
 * @author Dirk Kuch
 */
public class ZmlModelRowAccesor implements IColumnAccessor<IZmlModelRow>
{

  private final ZmlModelViewport m_model;

  public ZmlModelRowAccesor( final ZmlModelViewport model )
  {
    m_model = model;
  }

  @Override
  public IZmlModelValueCell getDataValue( final IZmlModelRow row, final int columnIndex )
  {
    final IZmlModelColumn column = m_model.getColum( columnIndex );
    if( Objects.isNotNull( row, column ) )
      return row.get( column );

    return null;
  }

  @Override
  public void setDataValue( final IZmlModelRow row, final int columnIndex, final Object newValue )
  {
    final IZmlModelValueCell cell = m_model.getCell( row, columnIndex );
    final IZmlModelValueCell valueCell = cell;
    if( Objects.isNull( valueCell ) )
      return;

    final IZmlModelColumn column = valueCell.getColumn();

    final IZmlModelCellLabelProvider provider = cell.getStyleProvider();
    final String oldValue = provider.getText( m_model, cell );
    if( Objects.equal( oldValue, newValue ) )
      return;

    final IZmlEditingStrategy strategy = m_model.getEditingStrategy( column );
    final Object targetValue = strategy.parseValue( valueCell, newValue.toString() );
    strategy.setValue( valueCell, targetValue );
  }

  @Override
  public int getColumnCount( )
  {
    throw new UnsupportedOperationException();
  }

}
