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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.focus.ZmlTableEditingSupport;
import org.kalypso.zml.ui.table.provider.AppliedRule;
import org.kalypso.zml.ui.table.provider.RuleMapper;
import org.kalypso.zml.ui.table.provider.strategy.ZmlCollectRulesVisitor;
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
public class ZmlTableColumn extends AbstractZmlTableColumn implements IZmlTableColumn
{
  private final RuleMapper m_mapper;

  private CellStyle m_lastCellStyle;

  private IZmlModelRow m_lastRow;

  private IZmlLabelStrategy m_labeling;

  private IZmlEditingStrategy m_editing;

  private final int m_tableColumnIndex;

  private ZmlTableEditingSupport m_editingSupport;

  public ZmlTableColumn( final IZmlTable table, final TableViewerColumn column, final BaseColumn type, final int tableColumnIndex )
  {
    super( table, column, type );
    m_tableColumnIndex = tableColumnIndex;

    m_mapper = new RuleMapper( table, type );
  }

  @Override
  public IZmlEditingStrategy getEditingStrategy( )
  {
    if( m_editing != null )
      return m_editing;

    final AbstractColumnType type = getColumnType().getType();
    if( type instanceof IndexColumnType )
      return null;
    else
    {
      final DataColumnType dataColumnType = (DataColumnType) type;
      if( ITimeseriesConstants.TYPE_RAINFALL.equals( dataColumnType.getValueAxis() ) )
        m_editing = new SumValueEditingStrategy( this );
      else
        m_editing = new InterpolatedValueEditingStrategy( this );
    }

    return m_editing;
  }

  @Override
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

      if( ITimeseriesConstants.TYPE_RAINFALL.equals( dataColumnType.getValueAxis() ) )
        m_labeling = new SumValueLabelingStrategy( this );
      else
        m_labeling = new InstantaneousValueLabelingStrategy( this );
    }

    return m_labeling;
  }

  @Override
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
        final CellStyle style = rule.getStyle( row, getColumnType() );
        baseType = CellStyle.merge( baseType, style.getType() );
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
    final int resolution = getTable().getResolution();

    if( isIndexColumn() )
      return findSimpleActiveRules( row );

    if( resolution == 0 )
      return findSimpleActiveRules( row );
    else
    {
      final DataColumn type = getModelColumn().getDataColumn();
      if( ITimeseriesConstants.TYPE_RAINFALL.equals( type.getValueAxis() ) )
        return findAggregatedActiveRules( row );

      return findSimpleActiveRules( row );
    }

  }

  private ZmlRule[] findSimpleActiveRules( final IZmlModelRow row )
  {
    final IZmlValueReference reference = row.get( getColumnType().getType() );
    if( Objects.isNull( reference ) )
      return new ZmlRule[] {};

    return m_mapper.findActiveRules( reference );
  }

  private ZmlRule[] findAggregatedActiveRules( final IZmlModelRow row )
  {
    final IZmlTableCell current = findCell( row );
    final IZmlTableCell previous = current.findPreviousCell();

    final IZmlModelColumn modelColumn = current.getColumn().getModelColumn();

    IZmlValueReference previousReference = null;
    if( previous == null )
    {
      previousReference = new ZmlDataValueReference( row, modelColumn, 0 );
    }
    else
    {
      final IZmlValueReference reference = previous.getValueReference();
      final Integer index = reference.getModelIndex();

      previousReference = new ZmlDataValueReference( row, modelColumn, index + 1 );
    }

    final IZmlValueReference currentReference = current.getValueReference();
    if( Objects.isNull( previousReference, currentReference ) )
      return new ZmlRule[] {};

    try
    {
      final DateRange daterange = new DateRange( previousReference.getIndexValue(), currentReference.getIndexValue() );
      final ZmlCollectRulesVisitor visitor = new ZmlCollectRulesVisitor( m_mapper );
      getModelColumn().accept( visitor, daterange );

      return visitor.getRules();
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return new ZmlRule[] {};
  }

  @Override
  public void reset( )
  {
    super.reset();

    m_mapper.reset();
  }

  @Override
  public AppliedRule[] getAppliedRules( )
  {
    return m_mapper.getAppliedRules();
  }

  public int getTableColumnIndex( )
  {
    return m_tableColumnIndex;
  }

  @Override
  public String toString( )
  {
    return getColumnType().getIdentifier();
  }

  public void setEditingSupport( final ZmlTableEditingSupport editingSupport )
  {
    m_editingSupport = editingSupport;
    getTableViewerColumn().setEditingSupport( editingSupport );
  }

  @Override
  public ZmlTableEditingSupport getEditingSupport( )
  {
    return m_editingSupport;
  }
}