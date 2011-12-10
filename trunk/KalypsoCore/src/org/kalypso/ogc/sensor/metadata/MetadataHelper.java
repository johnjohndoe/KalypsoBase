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
package org.kalypso.ogc.sensor.metadata;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.commons.time.PeriodUtils;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.contribs.java.util.CalendarUtilities.FIELD;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;

/**
 * @author Dirk Kuch
 */
public class MetadataHelper implements ITimeseriesConstants, ICopyObservationMetaDataConstants
{
  protected MetadataHelper( )
  {
  }

  public static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
  static
  {
    DATE_FORMAT.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
  }

  public static String formatDate( final Date date )
  {
    return DATE_FORMAT.format( date );
  }

  public static DateRange getDateRange( final MetadataList mdl, final String fromTag, final String endTag )
  {
    final String propertyFrom = mdl.getProperty( fromTag, "" ); //$NON-NLS-1$
    final String propertyTo = mdl.getProperty( endTag, "" ); //$NON-NLS-1$

    return parseDateRange( propertyFrom, propertyTo );
  }

  private static DateRange parseDateRange( final String propertyFrom, final String propertyTo )
  {
    Date from = null;
    Date to = null;

    if( !StringUtils.isBlank( propertyFrom ) )
      from = DateUtilities.parseDateTime( propertyFrom );

    if( !StringUtils.isBlank( propertyTo ) )
      to = DateUtilities.parseDateTime( propertyTo );

    return DateRange.createDateRangeOrNull( from, to );
  }

  public static Date getForecastStart( final MetadataList mdl )
  {
    final DateRange dateRange = getForecastDateRange( mdl );
    if( Objects.isNull( dateRange ) )
      return null;

    return dateRange.getFrom();
  }

  public static Date getForecastEnd( final MetadataList mdl )
  {
    final DateRange dateRange = getForecastDateRange( mdl );
    if( dateRange == null )
      return null;

    return dateRange.getTo();
  }

  public static DateRange getForecastDateRange( final MetadataList mdl )
  {
    return parseDateRange( mdl.getProperty( MD_VORHERSAGE_START ), mdl.getProperty( MD_VORHERSAGE_ENDE ) );
  }

  public static DateRange getDateRange( final MetadataList mdl )
  {
    return parseDateRange( mdl.getProperty( MD_DATE_BEGIN ), mdl.getProperty( MD_DATE_END ) );
  }

  public static String getCountedHeaderItem( final String item, final int number )
  {
    if( item.endsWith( "_" ) )//$NON-NLS-1$
      return item + Integer.valueOf( number ).toString();

    return item + "_" + Integer.valueOf( number ).toString();//$NON-NLS-1$
  }

  public static Integer getCount( final String parameter )
  {
    final String[] parts = parameter.split( "_" );//$NON-NLS-1$
    if( parts.length < 2 )
      return -1;

    return Integer.valueOf( parts[parts.length - 1] );
  }

  public static void setLastUpdated( final MetadataList mdl )
  {
    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    final Calendar calendar = Calendar.getInstance( timeZone );

    final SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" ); //$NON-NLS-1$
    sdf.setTimeZone( timeZone );

    mdl.put( LAST_UPDATE, sdf.format( calendar.getTime() ) );
  }

  public static MetadataList clone( final MetadataList mdl )
  {
    final MetadataList clone = new MetadataList();

    final Set<Object> keys = mdl.keySet();
    for( final Object key : keys )
    {
      final Object value = mdl.get( key );
      clone.put( key, value );
    }
    return clone;
  }

  public static String getWqTable( final MetadataList mdl )
  {
    return mdl.getProperty( ITimeseriesConstants.MD_WQ_TABLE );
  }

  public static void setWqTable( final MetadataList mdl, final String table )
  {
    if( Strings.isEmpty( table ) )
      mdl.remove( ITimeseriesConstants.MD_WQ_TABLE );
    else
      mdl.setProperty( ITimeseriesConstants.MD_WQ_TABLE, table );
  }

  /**
   * Sets the 'forecast' metadata of the given observation using the given date range. If from or to are null, does
   * nothing.
   */
  public static void setTargetForecast( final MetadataList metadata, final DateRange range )
  {
    if( range == null )
      return;

    setTargetForecast( metadata, range.getFrom(), range.getTo() );
  }

  /**
   * Sets the 'forecast' metadata of the given observation using the given date range. If from or to are null, does
   * nothing.
   */
  public static void setTargetForecast( final MetadataList metadata, final Date from, final Date to )
  {
    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    if( from != null )
    {
      final String fromStr = DateUtilities.printDateTime( from, timeZone );
      metadata.setProperty( ITimeseriesConstants.MD_VORHERSAGE_START, fromStr ); //$NON-NLS-1$
    }

    if( to != null )
    {
      final String toStr = DateUtilities.printDateTime( to, timeZone );
      metadata.setProperty( ITimeseriesConstants.MD_VORHERSAGE_ENDE, toStr ); //$NON-NLS-1$
    }
  }

  /**
   * Sets the 'forecast' metadata of the given observation using the given date range. If from or to are null, does
   * nothing.
   */
  public static void setTargetDateRange( final MetadataList metadata, final DateRange range )
  {
    if( range == null )
      return;

    setTargetDateRange( metadata, range.getFrom(), range.getTo() );
  }

  /**
   * Sets the 'forecast' metadata of the given observation using the given date range. If from or to are null, does
   * nothing.
   */
  public static void setTargetDateRange( final MetadataList metadata, final Date from, final Date to )
  {
    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    if( from != null )
    {
      final String fromStr = DateUtilities.printDateTime( from, timeZone );
      metadata.setProperty( ITimeseriesConstants.MD_DATE_BEGIN, fromStr ); //$NON-NLS-1$
    }

    if( to != null )
    {
      final String toStr = DateUtilities.printDateTime( to, timeZone );
      metadata.setProperty( ITimeseriesConstants.MD_DATE_END, toStr ); //$NON-NLS-1$
    }
  }

  public static void setTimestep( final MetadataList metadata, final int calendarField, final int amount )
  {
    final String fieldName = CalendarUtilities.getName( calendarField );
    final Object value = String.format( "%s#%d", fieldName, amount );
    metadata.put( MD_TIMESTEP, value );
  }

  public static Period getTimestep( final MetadataList metadata )
  {
    final String property = metadata.getProperty( MD_TIMESTEP, null );
    if( StringUtils.isBlank( property ) )
      return null;

    final String[] split = property.split( "#" );
    if( split.length != 2 )
      return null;

    final String fieldName = split[0];
    final Integer amount = NumberUtils.parseQuietInteger( split[1] );
    if( amount == null )
      return null;

    final int field = CalendarUtilities.getCalendarField( fieldName );
    return PeriodUtils.getPeriod( field, amount );
  }

  public static Date getAusgabeZeitpunkt( final MetadataList metadata ) throws ParseException
  {
    final String property = metadata.getProperty( IMetadataConstants.AUSGABE_ZEITPUNKT );
    if( Strings.isNotEmpty( property ) )
      return DATE_FORMAT.parse( property );

    return null;
  }

  public static void setTimestep( final MetadataList mdl, final Period timestep )
  {
    final int amount = PeriodUtils.findCalendarAmount( timestep );
    final FIELD calendarField = PeriodUtils.findCalendarField( timestep );

    if( amount == Integer.MAX_VALUE || calendarField == null )
      throw new IllegalArgumentException( "Unable to set 0 timestep" );

    setTimestep( mdl, calendarField.getField(), amount );

    return;
  }
}
