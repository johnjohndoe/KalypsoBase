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
package org.kalypso.zml.ui.table.viewmodel;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.binding.CellStyle;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.provider.RuleMapper;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IZmlLabelStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IndexValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.InstantaneousValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.SumValueLabelingStrategy;
import org.kalypso.zml.ui.table.schema.AbstractColumnType;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.DataColumnType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ExtendedZmlTableColumn extends ZmlTableColumn
{
  private IZmlLabelStrategy m_strategy;

  private final RuleMapper m_mapper = new RuleMapper();

  private CellStyle m_lastCellStyle;

  private IZmlModelRow m_lastRow;

  public ExtendedZmlTableColumn( final IZmlTable table, final TableViewerColumn column, final BaseColumn type )
  {
    super( table, column, type );
  }

  public IZmlLabelStrategy getLabelingStrategy( )
  {
    if( m_strategy != null )
      return m_strategy;

    // index column type?
    final AbstractColumnType type = getColumnType().getType();
    if( type instanceof IndexColumnType )
      m_strategy = new IndexValueLabelingStrategy( this );
    else
    {
      final DataColumnType dataColumnType = (DataColumnType) type;

      if( "N".equals( dataColumnType.getValueAxis() ) )
        m_strategy = new SumValueLabelingStrategy( this );
      else
        m_strategy = new InstantaneousValueLabelingStrategy( this );
    }

    return m_strategy;
  }

  public CellStyle findStyle( final IZmlModelRow row ) throws CoreException
  {
    if( m_lastRow == row )
      return m_lastCellStyle;

    final BaseColumn columnType = getColumnType();

    final ZmlRule[] rules = m_mapper.findActiveRules( row, columnType );
    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = columnType.getDefaultStyle().getType();
      for( final ZmlRule rule : rules )
      {
        baseType = CellStyle.merge( baseType, rule.getStyle( row, columnType ).getType() );
      }

      m_lastCellStyle = new CellStyle( baseType );
    }
    else
    {
      m_lastCellStyle = columnType.getDefaultStyle();
    }

    m_lastRow = row;

    return m_lastCellStyle;
  }

  public ZmlRule[] findActiveRules( final IZmlModelRow row )
  {
    return m_mapper.findActiveRules( row, getColumnType() );
  }
}
