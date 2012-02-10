/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlForecastLayer extends AbstractChartLayer implements IObsProviderListener
{
  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private IObsProvider m_provider;

  private final ILineStyle m_style;

  public ZmlForecastLayer( final ILayerProvider layerProvider, final ILineStyle style )
  {
    super( layerProvider );
    m_style = style;
  }

  public void setObsProvider( final IObsProvider provider )
  {
    if( m_provider != null )
    {
      m_provider.removeListener( this );
      m_provider.dispose();
    }

    m_provider = provider;

    if( provider != null )
    {
      provider.addListener( this );
      if( !provider.isLoaded() )
      {
        setVisible( false );
      }
    }
  }

  @Override
  public void paint( final GC gc )
  {
    if( m_provider == null )
      return;

    final ICoordinateMapper mapper = getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Number min = domainRange.getMin();
    final Number max = domainRange.getMax();
    if( min == null || max == null )
      return;

    final Calendar forecast = getForecast();
    if( forecast == null )
      return;

    final Calendar from = getCalendar( min.longValue() );
    if( !from.before( forecast ) )
      return;

    final Calendar to = getCalendar( max.longValue() );
    if( !to.after( forecast ) )
      return;

    final Integer x = Math.abs( domainAxis.numericToScreen( forecast.getTimeInMillis() ) );

    final Integer y0 = targetAxis.numericToScreen( targetRange.getMin() );
    final Integer y1 = targetAxis.numericToScreen( targetRange.getMax() );

    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( m_style );
    polylineFigure.setPoints( new Point[] { new Point( x, y0 ), new Point( x, y1 ) } );
    polylineFigure.paint( gc );
  }

  private Calendar getForecast( )
  {
    if( m_provider == null )
      return null;

    final IObservation observation = m_provider.getObservation();
    if( observation == null )
      return null;

    final MetadataList metadata = observation.getMetadataList();
    final Date forecastStart = MetadataHelper.getForecastStart( metadata );
    if( forecastStart == null )
      return null;

    final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    calendar.setTime( forecastStart );

// final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
// final int timeZoneOffset = timeZone.getRawOffset();
// calendar.add( Calendar.MILLISECOND, timeZoneOffset );

    return calendar;
  }

  private Calendar getCalendar( final Number number )
  {
    final Calendar instance = Calendar.getInstance();
    instance.setTimeInMillis( number.longValue() );

    return instance;
  }

  @Override
  public IDataRange<Number> getDomainRange( )
  {
    // TODO: all three parameters should eventually be set from outside
    final boolean shouldAutomax = true;
    final int bufferField = Calendar.HOUR_OF_DAY;
    final int bufferAmount = -2;

    if( !shouldAutomax )
      return null;

    final Calendar forecast = getForecast();
    if( forecast == null )
      return null;

    final Calendar from = (Calendar) forecast.clone();
    final Calendar end = (Calendar) forecast.clone();

    from.add( bufferField, -bufferAmount );
    end.add( bufferField, bufferAmount );

    return new DataRange<Number>( m_dateDataOperator.logicalToNumeric( from.getTime() ), m_dateDataOperator.logicalToNumeric( end.getTime() ) );
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return null;
  }

  @Override
  public void dispose( )
  {
    if( m_provider != null )
      m_provider.dispose();
  }

  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

  @Override
  public void observationReplaced( )
  {
    setVisible( true );
    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public void observationChanged( final Object source )
  {
    getEventHandler().fireLayerContentChanged( this );
  }
}
