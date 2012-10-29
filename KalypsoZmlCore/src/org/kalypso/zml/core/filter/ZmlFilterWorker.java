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
package org.kalypso.zml.core.filter;

import java.net.URL;

import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.creators.IntervallFilterCreator;
import org.kalypso.ogc.sensor.filter.creators.RoundFilterCreator;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalDefinition;
import org.kalypso.ogc.sensor.timeseries.interpolation.InterpolationFilterCreator;
import org.kalypso.zml.core.filter.binding.IZmlFilter;
import org.kalypso.zml.core.filter.binding.InterpolationZmlFilter;
import org.kalypso.zml.core.filter.binding.IntervalZmlFilter;
import org.kalypso.zml.core.filter.binding.RoundZmlFilter;
import org.kalypso.zml.core.i18n.Messages;

/**
 * Worker that applies one or more {@link org.kalypso.zml.core.filter.binding.IZmlFilter}s to existing
 * {@link org.kalypso.ogc.sensor.IObservation}s.
 * 
 * @author Gernot Belger
 */
public final class ZmlFilterWorker
{
  public static IObservation applyFilters( final IObservation source, final IZmlFilter... filters ) throws SensorException
  {
    return applyFilters( source, null, filters );
  }

  public static IObservation applyFilters( final IObservation source, final URL context, final IZmlFilter... filters ) throws SensorException
  {
    final ZmlFilterWorker worker = new ZmlFilterWorker( filters, context );
    return worker.execute( source );
  }

  /**
   * The context against all links will be resolved.
   */
  private final URL m_context;

  private final IZmlFilter[] m_filter;

  private ZmlFilterWorker( final IZmlFilter filter, final URL context )
  {
    this( new IZmlFilter[] { filter }, context );
  }

  private ZmlFilterWorker( final IZmlFilter[] filter, final URL context )
  {
    m_filter = filter;
    m_context = context;
  }

  /**
   * Applies all filters of this worker to the given observation and returns the reuslt.
   */
  public IObservation execute( final IObservation input ) throws SensorException
  {
    // TODO: some filter might work with a null-base observation, so how can we know which one does?
    // REMARK: for now, return null if we get null
    if( input == null )
      return null;

    IObservation result = input;

    // Apply all filters in the given order
    for( final IZmlFilter filter : m_filter )
      result = applySourceFilter( filter, result );

    return result;
  }

  private IObservation applySourceFilter( final IZmlFilter filter, final IObservation input ) throws SensorException
  {
    if( filter instanceof InterpolationZmlFilter )
    {
      final InterpolationZmlFilter interpolationZmlFilter = (InterpolationZmlFilter) filter;
      return InterpolationFilterCreator.createFilter( interpolationZmlFilter.getCalendarAmount(), interpolationZmlFilter.getCalendarField(), interpolationZmlFilter.getDefaultStatus(), interpolationZmlFilter.getDefaultValue(), interpolationZmlFilter.isForceFill(), input, m_context );
    }

    if( filter instanceof IntervalZmlFilter )
    {
      final IntervalZmlFilter intervalZmlFilter = (IntervalZmlFilter) filter;

      final String fieldText = intervalZmlFilter.getCalendarField();
      final int field = CalendarUtilities.getCalendarField( fieldText );
      final Integer amount = intervalZmlFilter.getCalendarAmount();

      final double defaultValue = intervalZmlFilter.getDefaultValue();
      final Integer defaultStatus = intervalZmlFilter.getDefaultStatus();

      final IntervalDefinition definition = new IntervalDefinition( field, amount, defaultValue, defaultStatus );
      return IntervallFilterCreator.createFilter( definition, input, m_context );
    }

    if( filter instanceof RoundZmlFilter )
    {
      final RoundZmlFilter roundZmlFilter = (RoundZmlFilter) filter;
      return RoundFilterCreator.createFilter( roundZmlFilter.getFactor(), roundZmlFilter.getMode(), roundZmlFilter.getAxisType(), input, m_context );
    }

    final String error = String.format( Messages.ZmlFilterWorker_0, filter );
    throw new UnsupportedOperationException( error );
  }
}