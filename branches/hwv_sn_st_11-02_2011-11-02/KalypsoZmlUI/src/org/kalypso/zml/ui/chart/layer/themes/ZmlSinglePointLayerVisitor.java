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

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;

/**
 * @author Dirk Kuch
 */
public class ZmlSinglePointLayerVisitor implements IObservationVisitor
{
  double m_diff = Double.MAX_VALUE;

  Number m_value = null;

  private IAxis m_valueAxis;

  private IAxis m_dateAxis;

  private final Date m_position;

  private final IChartLayerFilter[] m_filters;

  public ZmlSinglePointLayerVisitor( final Date position, final IChartLayerFilter[] filters )
  {
    m_position = position;
    m_filters = filters;
  }

  @Override
  public void visit( final IObservationValueContainer container ) throws CancelVisitorException
  {
    try
    {
      final Date date = (Date) container.get( getDateAxis( container ) );
      final Number v = (Number) container.get( getValueAxis( container ) );

      if( isFiltered( container ) )
        return;

      final double d = Math.abs( date.getTime() - m_position.getTime() );
      if( d < m_diff )
      {
        if( d == 0 )
        {
          m_value = v.doubleValue();
          throw new CancelVisitorException();
        }

        m_diff = d;
        m_value = v;
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

  }

  private boolean isFiltered( final IObservationValueContainer container )
  {
    for( final IChartLayerFilter filter : m_filters )
    {
      if( filter.isFiltered( container ) )
        return true;
    }

    return false;
  }

  private IAxis getValueAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_valueAxis ) )
      m_valueAxis = AxisUtils.findValueAxis( container.getAxes() );

    return m_valueAxis;
  }

  private IAxis getDateAxis( final IObservationValueContainer container )
  {
    if( Objects.isNull( m_dateAxis ) )
      m_dateAxis = AxisUtils.findDateAxis( container.getAxes() );

    return m_dateAxis;
  }

  public Double getValue( )
  {
    if( m_value == null )
      return null;

    return m_value.doubleValue();
  }
}
