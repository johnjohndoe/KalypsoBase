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
package org.kalypso.zml.core.table.model.references;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.labeling.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.references.labeling.ZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.core.table.schema.CellStyleType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelValueCell extends AbstractZmlCell implements IZmlModelValueCell
{
  private CellStyle m_style;

  private ZmlCellRule[] m_rules;

  private final ZmlModelCellLabelProvider m_styleProvider;

  private Object m_oldRuleValue;

  private String m_oldRuleSource;

  public ZmlModelValueCell( final IZmlModelRow row, final IZmlModelColumn column, final int tupleModelIndex )
  {
    super( row, column, tupleModelIndex );

    m_styleProvider = new ZmlModelCellLabelProvider( column );
  }

  @Override
  public void reset( )
  {
    m_style = null;
    m_rules = null;
  }

  @Override
  public Date getIndexValue( )
  {
    try
    {
      final DataColumn type = getColumn().getDataColumn();
      final IAxis[] axes = getColumn().getAxes();
      final IAxis axis = AxisUtils.findAxis( axes, type.getIndexAxis() );

      return (Date) getColumn().get( getModelIndex(), axis );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return getRow().getIndex();
  }

  @Override
  public IZmlModelCellLabelProvider getStyleProvider( )
  {
    return m_styleProvider;
  }

  @Override
  public Object getValue( ) throws SensorException
  {
    return getColumn().get( getModelIndex(), getColumn().getValueAxis() );
  }

  @Override
  public void doUpdate( final Object value, final String source, final Integer status ) throws SensorException
  {
    final Object oldValue = getValue();
    if( Objects.equal( value, oldValue ) )
      return;

    getColumn().doUpdate( getModelIndex(), value, source, status );
  }

  public String getIdentifier( )
  {
    return getColumn().getIdentifier();
  }

  @Override
  public Integer getStatus( ) throws SensorException
  {
    final IAxis axis = getColumn().getStatusAxis();
    if( axis == null )
      return null;

    final Object value = getColumn().get( getModelIndex(), axis );
    if( value instanceof Number )
      return ((Number) value).intValue();

    return null;
  }

  @Override
  public String getDataSource( ) throws SensorException
  {
    final MetadataList metadata = getColumn().getMetadata();
    final IAxis axis = AxisUtils.findDataSourceAxis( getColumn().getAxes(), getColumn().getValueAxis() );

    final Object objIndex = getColumn().get( getModelIndex(), axis );
    if( objIndex instanceof Number )
    {
      final Number index = (Number) objIndex;

      final DataSourceHandler handler = new DataSourceHandler( metadata );
      return handler.getDataSourceIdentifier( index.intValue() );
    }

    return null;
  }

  @Override
  public String getHref( )
  {
    final IObservation observation = getColumn().getObservation();
    final String href = observation.getHref();

    try
    {
      if( StringUtils.isBlank( href ) )
        return null;

      final IFile resource = ResourceUtilities.findFileFromPath( new Path( href ) );
      if( resource != null )
        return resource.getProjectRelativePath().toOSString();
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return href;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlModelValueCell )
    {
      try
      {
        final ZmlModelValueCell other = (ZmlModelValueCell) obj;

        final EqualsBuilder builder = new EqualsBuilder();
        builder.append( getColumn().getIdentifier(), other.getColumn().getIdentifier() );
        builder.append( getModelIndex(), getModelIndex() );
        builder.append( getValue(), other.getValue() );

        return builder.isEquals();
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getColumn().getIdentifier() );
    builder.append( getModelIndex() );

    return builder.toHashCode();
  }

  @Override
  public CellStyle getStyle( final ZmlModelViewport viewport ) throws CoreException, SensorException
  {
    if( Objects.isNotNull( m_style ) )
      return m_style;

    final ZmlCellRule[] rules = findActiveRules( viewport );
    final DataColumn column = getColumn().getDataColumn();

    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = column.getDefaultStyle().getType();
      for( final ZmlCellRule rule : rules )
      {

        final CellStyle style = rule.getStyle( this );
        baseType = CellStyle.merge( baseType, style.getType() );
      }

      m_style = new CellStyle( baseType );
    }
    else
    {
      m_style = column.getDefaultStyle();
    }

    return m_style;
  }

  @Override
  public ZmlCellRule[] findActiveRules( final ZmlModelViewport viewport ) throws SensorException
  {
    final DataColumn column = getColumn().getDataColumn();
    if( isSimple( viewport, column ) )
    {
      final Object value = getValue();
      if( Objects.notEqual( value, m_oldRuleValue ) )
        m_rules = null;

      final String source = getDataSource();
      if( Objects.notEqual( source, m_oldRuleSource ) )
        m_rules = null;

      if( m_rules != null )
        return m_rules;

      m_style = null;
      m_rules = findSimpleActiveRules();

      m_oldRuleValue = value;
      m_oldRuleSource = source;
    }
    else
    {
      m_rules = findAggregatedActiveRules( viewport );
    }

    return m_rules;
  }

  private boolean isSimple( final ZmlModelViewport viewport, final DataColumn column )
  {
    if( viewport == null ) // viewport can be null for aggregated non visible cells
      return true;

    if( viewport.getResolution() == 0 )
      return true;
    else if( ITimeseriesConstants.TYPE_RAINFALL.equals( column.getValueAxis() ) )
      return false;

    return true;
  }

  private ZmlCellRule[] findAggregatedActiveRules( final ZmlModelViewport viewport )
  {
    final IZmlModel zml = viewport.getModel();

    IZmlModelValueCell previous = viewport.findPreviousCell( this );
    if( previous == null )
    {
      final IZmlModelRow row = zml.getRowAt( 0 );
      previous = row.get( getColumn() );
    }
    else
    {
      final IZmlModelRow row = zml.getRowAt( previous.getModelIndex() + 1 );
      previous = row.get( getColumn() );
    }

    try
    {
      final DateRange daterange = new DateRange( previous.getIndexValue(), getIndexValue() );
      final ZmlCollectRulesVisitor visitor = new ZmlCollectRulesVisitor();
      getColumn().accept( visitor, daterange );

      return visitor.getRules();
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return new ZmlCellRule[] {};
  }

  private ZmlCellRule[] findSimpleActiveRules( )
  {
    final DataColumn column = getColumn().getDataColumn();

    final List<ZmlCellRule> rules = new ArrayList<ZmlCellRule>();
    final ZmlCellRule[] columnRules = column.getCellRules();
    for( final ZmlCellRule rule : columnRules )
    {
      final IZmlCellRuleImplementation impl = rule.getImplementation();
      if( impl.apply( rule, this ) )
      {
        rules.add( rule );
      }
    }

    return rules.toArray( new ZmlCellRule[] {} );
  }

  @Override
  public String toString( )
  {
    return super.toString();
  }
}