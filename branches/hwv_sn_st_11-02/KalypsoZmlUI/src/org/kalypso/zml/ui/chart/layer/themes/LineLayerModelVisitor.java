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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.util.resource.IPair;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author Dirk Kuch
 */
public class LineLayerModelVisitor implements IObservationVisitor
{
  private final Collection<IPair<Number, Number>> m_path = new ArrayList<IPair<Number, Number>>();

  private final ZmlLineLayer m_layer;

  private IAxis m_dateAxis;

  private final IChartLayerFilter[] m_filters;

  private final IDataRange<Number> m_domainIntervall;

  private Date m_to;

  private Date m_from;

  public LineLayerModelVisitor( final ZmlLineLayer layer, final IChartLayerFilter[] filters, final IDataRange<Number> domainIntervall )
  {
    m_layer = layer;
    m_filters = filters;
    m_domainIntervall = domainIntervall;
  }

  private IAxis getValueAxis( )
  {
    final IZmlLayerDataHandler handler = m_layer.getDataHandler();

    return handler.getValueAxis();
  }

  private IAxis getDateAxis( )
  {
    if( m_dateAxis == null )
    {
      final IZmlLayerDataHandler handler = m_layer.getDataHandler();
      final IObservation observation = handler.getObservation();
      if( Objects.isNull( observation ) )
        return null;

      final IAxis[] axes = observation.getAxes();

      m_dateAxis = AxisUtils.findDateAxis( axes );
    }

    return m_dateAxis;
  }

  @Override
  public void visit( final IObservationValueContainer container )
  {
    try
    {
      final IAxis dateAxis = getDateAxis();
      final IAxis valueAxis = getValueAxis();
      if( Objects.isNull( dateAxis, valueAxis ) )
        return;

      if( !container.hasAxis( getDateAxis().getType(), valueAxis.getType() ) )
        return;

      final Object dateObject = container.get( dateAxis );
      final Object valueObject = container.get( valueAxis );
      if( Objects.isNull( dateObject, valueObject ) )
        return;

      if( isFiltered( container ) )
        return;

      final Date domain = (Date) dateObject;

      if( !isPartOfDomainInterval( domain ) )
        return;

      final Number domainNumeric = m_layer.getRangeHandler().getDateDataOperator().logicalToNumeric( domain );
      final Number targetNumeric = m_layer.getRangeHandler().getNumberDataOperator().logicalToNumeric( (Double) valueObject );

      final IPair<Number, Number> numeric = new Pair<Number, Number>( domainNumeric, targetNumeric );
      m_path.add( numeric );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private boolean isFiltered( final IObservationValueContainer container )
  {
    if( ArrayUtils.isEmpty( m_filters ) )
      return false;

    for( final IChartLayerFilter filter : m_filters )
    {
      if( filter.isFiltered( container ) )
        return true;
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  public IPair<Number, Number>[] getPoints( )
  {
    return m_path.toArray( new IPair[m_path.size()] );
  }

  private boolean isPartOfDomainInterval( final Date date )
  {
    if( Objects.isNull( m_domainIntervall ) )
      return true;
    final Date from = getFrom();
    final Date to = getTo();

    final DateRange dateRange = new DateRange( from, to );

    return dateRange.containsLazyInclusive( date );
  }

  private Date getTo( )
  {
    if( Objects.isNotNull( m_to ) )
      return m_to;

    final Number max = m_domainIntervall.getMax();
    if( Objects.isNull( max ) )
      return null;

    m_to = new Date( max.longValue() );
    return m_to;
  }

  private Date getFrom( )
  {
    if( Objects.isNotNull( m_from ) )
      return m_from;

    final Number min = m_domainIntervall.getMin();
    if( Objects.isNull( min ) )
      return null;

    m_from = new Date( min.longValue() );
    return m_from;
  }
}
