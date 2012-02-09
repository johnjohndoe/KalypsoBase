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
package org.kalypso.zml.ui.table.model.columns;

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
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.CellStyleType;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.core.table.schema.IndexColumnType;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.focus.ZmlTableEditingSupport;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.rows.IZmlTableRow;
import org.kalypso.zml.ui.table.model.visitors.FindTableRowVisitor;
import org.kalypso.zml.ui.table.provider.AppliedRule;
import org.kalypso.zml.ui.table.provider.RuleMapper;
import org.kalypso.zml.ui.table.provider.strategy.ZmlCollectRulesVisitor;
import org.kalypso.zml.ui.table.provider.strategy.editing.ContinuedInterpolatedValueEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.editing.InterpolatedValueEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.editing.SumValueEditingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IZmlLabelStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.InstantaneousValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.labeling.SumValueLabelingStrategy;

/**
 * @author Dirk Kuch
 */
public class ZmlTableValueColumn extends AbstractZmlTableColumn implements IZmlTableValueColumn
{
  /** visibility flag is used by hide columns command */
  private boolean m_visible = true;

  private final RuleMapper m_mapper;

  private CellStyle m_lastCellStyle;

  private IZmlModelRow m_lastRow;

  private IZmlLabelStrategy m_labeling;

  private IZmlEditingStrategy m_editing;

  private ZmlTableEditingSupport m_editingSupport;

  public ZmlTableValueColumn( final IZmlTable table, final TableViewerColumn column, final BaseColumn type, final int tableColumnIndex )
  {
    super( table, column, type, tableColumnIndex );

    m_mapper = new RuleMapper( table, type );
  }

  @Override
  public boolean isVisible( )
  {
    final IZmlModelColumn column = getModelColumn();
    if( Objects.isNull( column ) )
      return false;

    if( !m_visible )
      return false;

    return column.isActive();
  }

  @Override
  public IZmlTableValueCell findCell( final IZmlModelRow row )
  {
    final FindTableRowVisitor visitor = new FindTableRowVisitor( row );
    getTable().accept( visitor );

    final IZmlTableRow tableRow = visitor.getRow();
    if( Objects.isNull( tableRow ) )
      return null;

    return (IZmlTableValueCell) tableRow.getCell( this );
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
      else if( ITimeseriesConstants.TYPE_WECHMANN_E.equals( dataColumnType.getValueAxis() ) )
        m_editing = new ContinuedInterpolatedValueEditingStrategy( this );
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

    final DataColumnType dataColumnType = (DataColumnType) getColumnType().getType();
    if( ITimeseriesConstants.TYPE_RAINFALL.equals( dataColumnType.getValueAxis() ) )
      m_labeling = new SumValueLabelingStrategy( this );
    else
      m_labeling = new InstantaneousValueLabelingStrategy( this );

    return m_labeling;
  }

  @Override
  public CellStyle findStyle( final IZmlModelRow row ) throws CoreException
  {
    if( m_lastRow == row )
      return m_lastCellStyle;

    final ZmlCellRule[] rules = findActiveRules( row );
    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = getColumnType().getDefaultStyle().getType();
      for( final ZmlCellRule rule : rules )
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

  @Override
  public ZmlCellRule[] findActiveRules( final IZmlModelRow row )
  {
    final int resolution = getTable().getResolution();

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

  private ZmlCellRule[] findSimpleActiveRules( final IZmlModelRow row )
  {
    final IZmlModelCell reference = row.get( getColumnType().getType() );
    if( Objects.isNull( reference ) )
      return new ZmlCellRule[] {};

    return m_mapper.findActiveRules( reference );
  }

  private ZmlCellRule[] findAggregatedActiveRules( final IZmlModelRow row )
  {
    final IZmlTableValueCell current = findCell( row );
    final IZmlTableValueCell previous = current.findPreviousCell();

    final IZmlModelColumn modelColumn = current.getColumn().getModelColumn();

    IZmlModelValueCell previousReference = null;
    if( previous == null )
    {
      previousReference = new ZmlDataValueReference( row, modelColumn, 0 );
    }
    else
    {
      final IZmlModelValueCell reference = previous.getValueReference();
      final Integer index = reference.getModelIndex();

      previousReference = new ZmlDataValueReference( row, modelColumn, index + 1 );
    }

    final IZmlModelValueCell currentReference = current.getValueReference();
    if( Objects.isNull( previousReference, currentReference ) )
      return new ZmlCellRule[] {};

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

    return new ZmlCellRule[] {};
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

  @Override
  public String toString( )
  {
    return String.format( "id: %s, label: %s", getColumnType().getIdentifier(), getTableViewerColumn().getColumn().getText() );
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

  @Override
  public void setVisible( final boolean visibility )
  {
    if( Objects.notEqual( m_visible, visibility ) )
    {
      m_visible = visibility;

      getTable().fireTableChanged( IZmlTableListener.TYPE_ACTIVE_RULE_CHANGED, getModelColumn() );
    }
  }
}
