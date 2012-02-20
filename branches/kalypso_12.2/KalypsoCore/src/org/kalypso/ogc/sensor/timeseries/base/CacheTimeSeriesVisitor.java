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
package org.kalypso.ogc.sensor.timeseries.base;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;

/**
 * @author Dirk Kuch
 */
public class CacheTimeSeriesVisitor implements ITupleModelVisitor
{
  private final Map<Date, TupleModelDataSet[]> m_values = new TreeMap<Date, TupleModelDataSet[]>();

  private final String m_source;

  public CacheTimeSeriesVisitor( final String source )
  {
    m_source = source;
  }

  public Map<Date, TupleModelDataSet[]> getValueMap( )
  {
    return m_values;
  }

  public DatedDataSets[] getValues( )
  {
    final Set<DatedDataSets> sets = new LinkedHashSet<DatedDataSets>();

    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      sets.add( new DatedDataSets( entry.getKey(), entry.getValue() ) );
    }

    return sets.toArray( new DatedDataSets[] {} );
  }

  @Override
  public void visit( final ITupleModelValueContainer container )
  {
    try
    {
      final Date date = (Date) container.get( getDateAxis( container ) );

      final Set<TupleModelDataSet> sets = new LinkedHashSet<TupleModelDataSet>();

      final IAxis[] axes = container.getAxes();
      final IAxis[] valueAxes = AxisUtils.findValueAxes( axes );
      for( final IAxis valueAxis : valueAxes )
      {
        final Number value = (Number) container.get( getValueAxis( container ) );
        final Integer status = getStatus( container, valueAxis );

        sets.add( new TupleModelDataSet( valueAxis, value, status, m_source ) );
      }

      m_values.put( date, sets.toArray( new TupleModelDataSet[] {} ) );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private Integer getStatus( final ITupleModelValueContainer container, final IAxis valueAxis ) throws SensorException
  {
    final IAxis statusAxis = AxisUtils.findStatusAxis( container.getAxes(), valueAxis );
    if( Objects.isNotNull( statusAxis ) )
    {
      final Object status = container.get( statusAxis );
      if( status instanceof Number )
        return ((Number) status).intValue();
    }

    return KalypsoStati.BIT_OK;
  }

  private IAxis getValueAxis( final ITupleModelValueContainer container )
  {
    return AxisUtils.findValueAxis( container.getAxes() );
  }

  private IAxis getDateAxis( final ITupleModelValueContainer container )
  {
    return AxisUtils.findDateAxis( container.getAxes() );
  }

  public TupleModelDataSet[] getValue( final Date date )
  {
    return m_values.get( date );
  }

  public TupleModelDataSet getValue( final Date date, final IAxis valueAxis )
  {
    final TupleModelDataSet[] dataSets = m_values.get( date );
    if( ArrayUtils.isEmpty( dataSets ) )
      return null;

    for( final TupleModelDataSet dataset : dataSets )
    {
      if( dataset.getValueAxis().equals( valueAxis ) )
        return dataset;
    }

    return null;
  }

  public TupleModelDataSet findValueBefore( final IAxis axis, final Date date )
  {
    TupleModelDataSet ptr = null;

    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      final Date key = entry.getKey();
      if( key.before( date ) )
      {
        final TupleModelDataSet[] values = entry.getValue();
        for( final TupleModelDataSet value : values )
        {
          if( AxisUtils.isEqual( value.getValueAxis(), axis ) )
          {
            ptr = value;
            break;
          }
        }
      }
      else
        break;
    }

    return ptr;
  }

  public TupleModelDataSet findValueAfter( final IAxis axis, final Date date )
  {
    final Set<Entry<Date, TupleModelDataSet[]>> entries = m_values.entrySet();
    for( final Entry<Date, TupleModelDataSet[]> entry : entries )
    {
      final Date key = entry.getKey();
      if( key.after( date ) )
      {
        final TupleModelDataSet[] values = entry.getValue();
        for( final TupleModelDataSet value : values )
        {
          if( AxisUtils.isEqual( value.getValueAxis(), axis ) )
          {
            return value;
          }
        }
      }
    }

    return null;
  }
}
