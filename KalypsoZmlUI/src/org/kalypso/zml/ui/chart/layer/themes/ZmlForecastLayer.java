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
import java.util.TimeZone;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;

import de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlForecastLayer extends AbstractChartLayer implements IObsProviderListener
{
  private final IObsProvider m_provider;

  public ZmlForecastLayer( final IObsProvider provider )
  {
    m_provider = provider;

    synchronized( provider )
    {
      provider.addListener( this );

      if( !provider.isLoaded() )
      {
        setVisible( false );
      }
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final ICoordinateMapper mapper = getCoordinateMapper();

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    final IDataRange<Number> domainRange = domainAxis.getNumericRange();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Number min = domainRange.getMin();
    final Number max = domainRange.getMax();

    if( min == null || max == null )
      return;

    final Calendar from = getCalendar( min.longValue() );
    final Calendar to = getCalendar( max.longValue() );
    final Calendar forecast = getForecast();
    if( forecast == null )
      return;

    if( !from.before( forecast ) )
      return;

    if( !to.after( forecast ) )
      return;

    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    final int timeZoneOffset = timeZone.getRawOffset();

    final double logicalX = min.doubleValue() + (forecast.getTimeInMillis() - min.doubleValue() + timeZoneOffset);
    final Integer x = Math.abs( domainAxis.numericToScreen( logicalX ) );

    final Integer y0 = targetAxis.numericToScreen( targetRange.getMin() );
    // FIXME: use directly the screensize; do not use any targetAxis
    final Integer y1 = targetAxis.numericToScreen( targetRange.getMax() );

    final ILineStyle style = new LineStyle( 3, new RGB( 255, 0, 0 ), 100, 0F, new float[] { 12, 7 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );

    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( style );
    polylineFigure.setPoints( new Point[] { new Point( x, y0 ), new Point( x, y1 ) } );
    polylineFigure.paint( gc );
  }

  private Calendar getForecast( )
  {
    final IObservation observation = m_provider.getObservation();
    if( observation == null )
      return null;

    final MetadataList metadata = observation.getMetadataList();
    final Date forecastStart = MetadataHelper.getForecastStart( metadata );
    if( forecastStart == null )
      return null;

    final Calendar calendar = Calendar.getInstance( KalypsoCorePlugin.getDefault().getTimeZone() );
    calendar.setTime( forecastStart );

    return calendar;
  }

  private Calendar getCalendar( final Number number )
  {
    final Calendar instance = Calendar.getInstance();
    instance.setTimeInMillis( number.longValue() );

    return instance;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange(de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    m_provider.dispose();
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationLoadedEvent()
   */
  @Override
  public void observationReplaced( )
  {
    setVisible( true );
    getEventHandler().fireLayerContentChanged( this );
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChangedX(java.lang.Object)
   */
  @Override
  public void observationChanged( final Object source )
  {
    getEventHandler().fireLayerContentChanged( this );
  }

}
