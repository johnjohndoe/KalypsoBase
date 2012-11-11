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
package org.kalypso.ogc.sensor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.repository.IDataSourceItem;

/**
 * @author Dirk Kuch
 */
public class TupleModelDataSet
{
  private Object m_value;

  private Integer m_status;

  private String m_source;

  private final IAxis m_valueAxis;

  public TupleModelDataSet( final IAxis valueAxis, final Object value, final Integer status, final String source )
  {
    m_valueAxis = valueAxis;
    m_value = value;
    m_status = status;
    m_source = source;
  }

  public Object getValue( )
  {
    return m_value;
  }

  public Integer getStatus( )
  {
    return m_status;
  }

  public String getSource( )
  {
    return m_source;
  }

  public IAxis getValueAxis( )
  {
    return m_valueAxis;
  }

  @Override
  public String toString( )
  {
    final StringBuilder builder = new StringBuilder();
    builder.append( String.format( "Axis:\t\t%s\n", m_valueAxis.getName() ) ); //$NON-NLS-1$

    if( Objects.isNotNull( m_value ) )
    {
      if( m_value instanceof Number )
        builder.append( String.format( "Value:\t\t%.2f\n", ((Number)m_value).doubleValue() ) ); //$NON-NLS-1$
      else
        builder.append( String.format( "Value:\t\t%s\n", m_value ) ); //$NON-NLS-1$
    }

    if( Objects.isNotNull( m_status ) )
      builder.append( String.format( "Status:\t\t%s\n", KalypsoStatusUtils.getTooltipFor( m_status ) ) ); //$NON-NLS-1$

    if( StringUtils.isNotEmpty( m_source ) )
      builder.append( String.format( "DataSource:\t%s", m_source ) ); //$NON-NLS-1$

    return builder.toString();
  }

  @Override
  public TupleModelDataSet clone( )
  {
    // REMARK: deep clone not necessary, as all objects are always immutable
    final Object value = getValue();
    if( !(value instanceof Number) )
    {
      System.out.println( "oups" ); //$NON-NLS-1$
    }

    final Integer status = getStatus();
    final String source = getSource();

    return new TupleModelDataSet( getValueAxis(), value, status, source );
  }

  public static TupleModelDataSet[] clone( final TupleModelDataSet[] dataSets )
  {
    final Set<TupleModelDataSet> clones = new LinkedHashSet<>();
    for( final TupleModelDataSet dataSet : dataSets )
    {
      clones.add( dataSet.clone() );
    }

    return clones.toArray( new TupleModelDataSet[] {} );
  }

  public void setValue( final double value )
  {
    m_value = value;
  }

  public void setStatus( final int status )
  {
    m_status = status;
  }

  public void setSource( final String source )
  {
    m_source = source;
  }

  public static TupleModelDataSet toDataSet( final ITupleModelValueContainer container, final MetadataList metadata, final String valueAxisType ) throws SensorException
  {
    final IAxis valueAxis = AxisUtils.findAxis( container.getAxes(), valueAxisType );
    if( valueAxis == null )
      return null;

    if( !AxisUtils.isValueAxis( valueAxis ) )
      throw new SensorException( "TupleModelDataSet.toDataSet() only works for value axes!" ); //$NON-NLS-1$

    final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), valueAxis );
    final IAxis dataSourceAxis = AxisUtils.findDataSourceAxis( container.getAxes(), valueAxis );

    Integer status = KalypsoStati.BIT_OK;
    String source = IDataSourceItem.SOURCE_UNKNOWN;

    if( statusAxis != null )
    {
      final Object objStatus = container.get( statusAxis );
      if( objStatus instanceof Number )
        status = ((Number)objStatus).intValue();
    }

    if( dataSourceAxis != null )
    {
      final Object objDataSourceIndex = container.get( dataSourceAxis );
      if( objDataSourceIndex instanceof Number )
      {
        final int dataSourceIndex = ((Number)objDataSourceIndex).intValue();
        final DataSourceHandler handler = new DataSourceHandler( metadata );
        source = handler.getDataSourceIdentifier( dataSourceIndex );
      }
    }

    return new TupleModelDataSet( valueAxis, container.get( valueAxis ), status, source );
  }
}
