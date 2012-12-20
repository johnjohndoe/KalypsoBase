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
package org.kalypso.zml.ui.chart.layer.themes;

import java.util.Date;

import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
class ZmlBarLayerRangeHandler
{
  private final ZmlBarLayer m_layer;

  private IDataRange<Double> m_targetRange;

  private IDataRange<Double> m_domainRange;

  public ZmlBarLayerRangeHandler( final ZmlBarLayer layer )
  {
    m_layer = layer;
  }

  public synchronized IDataRange<Double> getDomainRange( )
  {
    if( m_domainRange == null )
      m_domainRange = calculateDomainRange();

    return m_domainRange;
  }

  private IDataRange<Double> calculateDomainRange( )
  {
    try
    {
      final IObservation observation = (IObservation)m_layer.getDataHandler().getAdapter( IObservation.class );
      if( Objects.isNull( observation ) )
        return null;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( observation.getAxes() );
      final ITupleModel model = observation.getValues( m_layer.getDataHandler().getRequest() );
      final IAxisRange range = model.getRange( dateAxis );
      if( Objects.isNull( range ) )
        return null;

      Date min = (Date)range.getLower();
      Date max = (Date)range.getUpper();

      final IAxis axis = m_layer.getDataHandler().getValueAxis();
      if( axis == null )
        return null;

      // FIXME: instead using parameter type, distinguish between 'forward' and 'backward' sums

      final boolean isForward = ITimeseriesConstants.TYPE_POLDER_CONTROL.equals( axis.getType() );

      // The domain range is a bit longer, because we are working with sum values
      if( isForward )
        max = doAdjustMax( observation, max );
      else
        min = doAdjustMin( observation, min );
      final ICoordinateMapper mapper = m_layer.getCoordinateMapper();
      final de.openali.odysseus.chart.framework.model.mapper.IAxis domainAxis = mapper.getDomainAxis();
      final Double numMin = domainAxis.logicalToNumeric( min );
      final Double numMax = domainAxis.logicalToNumeric( max );
      return new DataRange( numMin, numMax );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  private Date doAdjustMax( final IObservation observation, final Date max )
  {
    final Period timestep = MetadataHelper.getTimestep( observation.getMetadataList() );
    if( Objects.isNull( timestep ) )
      return max;

    final long ms = Double.valueOf( timestep.toStandardSeconds().getSeconds() * 1000.0 ).longValue();

    return new Date( max.getTime() + ms );
  }

  private Date doAdjustMin( final IObservation observation, final Date min )
  {
    final Period timestep = MetadataHelper.getTimestep( observation.getMetadataList() );
    if( Objects.isNull( timestep ) )
      return min;

    final long ms = Double.valueOf( timestep.toStandardSeconds().getSeconds() * 1000.0 ).longValue();

    return new Date( min.getTime() - ms );
  }

  public synchronized IDataRange<Double> getTargetRange( )
  {
    if( m_targetRange == null )
      m_targetRange = calculateTargetRange();

    return m_targetRange;
  }

  private IDataRange<Double> calculateTargetRange( )
  {
    try
    {
      final IZmlLayerDataHandler handler = m_layer.getDataHandler();
      final IObservation observation = (IObservation)handler.getAdapter( IObservation.class );
      if( Objects.isNull( observation ) )
        return null;

      final ITupleModel model = observation.getValues( handler.getRequest() );

      final IAxis valueAxis = handler.getValueAxis();
      if( valueAxis == null )
        return null;

      /** hack for polder control which consists of boolean values */
      // FIXME: the axis is responsible for that!
      // axis should do this now, just remove and test
      final Class< ? > dataClass = valueAxis.getDataClass();
      if( Boolean.class.equals( dataClass ) )
        return new DataRange<>( 0.0, 1.0 );

      final IAxisRange range = model.getRange( valueAxis );
      if( range == null )
        return null;
      final ICoordinateMapper mapper = m_layer.getCoordinateMapper();
      final de.openali.odysseus.chart.framework.model.mapper.IAxis targetAxis = mapper.getTargetAxis();
      final Double numMin = targetAxis.logicalToNumeric( range.getLower() );
      final Double numMax = targetAxis.logicalToNumeric( range.getUpper() );
      return new DataRange( numMin, numMax );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return null;
    }
  }

  public synchronized void invalidateRange( )
  {
    m_domainRange = null;
    m_targetRange = null;
  }
}