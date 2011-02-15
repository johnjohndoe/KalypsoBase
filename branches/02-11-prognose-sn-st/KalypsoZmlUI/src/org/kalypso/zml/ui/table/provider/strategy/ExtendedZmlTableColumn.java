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
package org.kalypso.zml.ui.table.provider.strategy;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.model.ZmlTableColumn;
import org.kalypso.zml.ui.table.provider.RuleMapper;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.editing.InterpolatedValueEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.editing.SumValueEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IZmlLabelStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IndexValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.InstantaneousValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.SumValueLabelingStrategy;

/**
 * @author Dirk Kuch
 */
public class ExtendedZmlTableColumn extends ZmlTableColumn implements IExtendedZmlTableColumn
{
  private final RuleMapper m_mapper;

  private CellStyle m_lastCellStyle;

  private IZmlModelRow m_lastRow;

  private IZmlLabelStrategy m_labeling;

  private IZmlEditingStrategy m_editing;

  private final int m_tableColumnIndex;

  public ExtendedZmlTableColumn( final IZmlTable table, final TableViewerColumn column, final BaseColumn type, final int tableColumnIndex )
  {
    super( table, column, type );
    m_tableColumnIndex = tableColumnIndex;

    m_mapper = new RuleMapper( type );
  }

  public IZmlEditingStrategy getEditingStrategy( final ZmlLabelProvider labelProvider )
  {
    if( m_editing != null )
      return m_editing;

    final AbstractColumnType type = getColumnType().getType();
    if( type instanceof IndexColumnType )
      return null;
    else
    {
      final DataColumnType dataColumnType = (DataColumnType) type;
      if( "N".equals( dataColumnType.getValueAxis() ) )
        m_editing = new SumValueEditingStrategy( this, labelProvider );
      else
        m_editing = new InterpolatedValueEditingStrategy( this );
    }

    return m_editing;
  }

  public IZmlLabelStrategy getLabelingStrategy( )
  {
    if( m_labeling != null )
      return m_labeling;

    // index column type?
    final AbstractColumnType type = getColumnType().getType();
    if( type instanceof IndexColumnType )
      m_labeling = new IndexValueLabelingStrategy( this );
    else
    {
      final DataColumnType dataColumnType = (DataColumnType) type;

      if( "N".equals( dataColumnType.getValueAxis() ) )
        m_labeling = new SumValueLabelingStrategy( this );
      else
        m_labeling = new InstantaneousValueLabelingStrategy( this );
    }

    return m_labeling;
  }

  public CellStyle findStyle( final IZmlModelRow row ) throws CoreException
  {
    if( m_lastRow == row )
      return m_lastCellStyle;

    final ZmlRule[] rules = findActiveRules( row );
    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = getColumnType().getDefaultStyle().getType();
      for( final ZmlRule rule : rules )
      {
        baseType = CellStyle.merge( baseType, rule.getStyle( row, getColumnType() ).getType() );
      }

      m_lastCellStyle = new CellStyle( baseType );
    }
    else
    {
      m_lastCellStyle = getColumnType().getDefaultStyle();
    }

    m_lastRow = row;

    return m_lastCellStyle;
  }

  public ZmlRule[] findActiveRules( final IZmlModelRow row )
  {
    return m_mapper.findActiveRules( row.get( getColumnType().getType() ) );
  }

  public boolean isVisible( )
  {
    if( isIndexColumn() )
      return true;

    return getModelColumn() != null;
  }

  public void reset( )
  {
    m_mapper.reset();
  }

  public ZmlRule[] getAppliedRules( )
  {
    return m_mapper.getAppliedRules();
  }

  public int getTableColumnIndex( )
  {
    return m_tableColumnIndex;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return getColumnType().getIdentifier();
  }
}
