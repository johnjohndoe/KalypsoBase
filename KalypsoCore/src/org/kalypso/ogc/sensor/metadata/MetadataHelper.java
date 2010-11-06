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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;

/**
 * @author Dirk Kuch
 */
public class MetadataHelper implements ITimeseriesConstants, ICopyObservationMetaDataConstants
{
  /** default date format used within some of the timeseries dependent properties */
  /** @deprecated Should not be used any more. We use xs:dateTime format now for printing times into zml files. */
  @Deprecated
  private static final DateFormat FORECAST_DF = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.GERMANY );

// private static final DateFormat FORECAST_DF = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss", Locale.GERMANY );

  private static SimpleDateFormat SDF = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );

  public static final transient String WQ_TABLE = "WQ-Tabelle";

  public static final transient String LAST_UPDATE = "Letzte_Aktualisierung";

  protected MetadataHelper( )
  {

  }

  protected static DateRange getDateRange( final MetadataList mdl, final String fromTag, final String endTag )
  {
    final String propertyFrom = mdl.getProperty( fromTag, "" );
    final String propertyTo = mdl.getProperty( endTag, "" );

    return getDateRange( propertyFrom, propertyTo );
  }

  protected static DateRange getDateRange( final String propertyFrom, final String propertyTo )
  {
    Date from = null;
    Date to = null;

    if( propertyFrom != null && !propertyFrom.isEmpty() )
      from = DateUtilities.parseDateTime( propertyFrom );

    if( propertyTo != null && !propertyTo.isEmpty() )
      to = DateUtilities.parseDateTime( propertyTo );

    return DateRange.createDateRangeOrNull( from, to );
  }

  public static Date getForecastStart( final MetadataList mdl )
  {
    final DateRange dateRange = getForecastDateRange( mdl );
    if( dateRange == null )
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
    return getDateRange( mdl.getProperty( MD_VORHERSAGE_START ), mdl.getProperty( MD_VORHERSAGE_ENDE ) );
  }

  public static DateRange getDateRange( final MetadataList mdl )
  {
    return getDateRange( mdl.getProperty( MD_DATE_BEGIN ), mdl.getProperty( MD_DATE_END ) );
  }

  public static String getCountedHeaderItem( final String item, final int number )
  {
    if( item.endsWith( "_" ) )
      return item + Integer.valueOf( number ).toString();

    return item + "_" + Integer.valueOf( number ).toString();
  }

  public static Integer getCount( final String parameter )
  {
    final String[] parts = parameter.split( "_" );
    if( parts.length < 2 )
      return -1;

    return Integer.valueOf( parts[parts.length - 1] );
  }

  public static final void setLastUpdated( final MetadataList mdl )
  {
    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    final Calendar calendar = Calendar.getInstance( timeZone );

    mdl.put( LAST_UPDATE, SDF.format( calendar.getTime() ) );
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
    return mdl.getProperty( ITimeseriesConstants.MD_WQTABLE );
  }

  public static void setWqTable( final MetadataList mdl, final String table )
  {
    mdl.setProperty( ITimeseriesConstants.MD_WQTABLE, table );
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
}
