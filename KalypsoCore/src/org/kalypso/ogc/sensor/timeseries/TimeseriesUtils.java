/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.timeseries;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TIMESERIES_TYPE;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.wq.IWQConverter;
import org.kalypso.ogc.sensor.timeseries.wq.WQException;
import org.kalypso.ogc.sensor.timeseries.wq.WQFactory;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * Utility for dealing Kalypso time series.
 * 
 * @author schlienger
 */
public final class TimeseriesUtils implements ITimeseriesConstants
{
  public static final String[] TYPES_ALL;

  /**
   * to enable searching in types the array must be sorted
   */
  static
  {
    final String[] types = new String[] { TYPE_DATE, TYPE_EVAPORATION, TYPE_RAINFALL, TYPE_RUNOFF, TYPE_TEMPERATURE, TYPE_VOLUME, TYPE_WATERLEVEL, TYPE_NORM, TYPE_AREA, TYPE_HOURS, TYPE_NORMNULL,
        TYPE_KC, TYPE_WT, TYPE_LAI, TYPE_HUMIDITY, TYPE_VELOCITY, TYPE_SUNSHINE_HOURS, TYPE_MEAN_WIND_VELOCITY, TYPE_MEAN_TEMPERATURE, TYPE_MEAN_HUMIDITY, TYPE_EVAPORATION_LAND_BASED,
        TYPE_EVAPORATION_WATER_BASED };
    Arrays.sort( types );
    TYPES_ALL = types;
  }

  /** default date format used within some of the time series dependent properties */
  /** @deprecated Should not be used any more. We use xs:dateTime format now for printing times into zml files. */
  @Deprecated
  private static final DateFormat FORECAST_DF = DateFormat.getDateTimeInstance( DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.GERMANY );

  private static final String PROP_TIMESERIES_CONFIG = "kalypso.timeseries.properties"; //$NON-NLS-1$

  private static URL CONFIG_BASE_URL = TimeseriesUtils.class.getResource( "resource/" ); //$NON-NLS-1$

  private static String BASENAME = "config"; //$NON-NLS-1$

  private static Properties CONFIG;

  private static HashMap<String, NumberFormat> FORMAT_MAP = new HashMap<String, NumberFormat>();

  private static NumberFormat DEFAULT_FORMAT = null;

  private TimeseriesUtils( )
  {
    // no instantiation
  }

  /**
   * Finds out which metadata of the given observation begin with the given prefix.
   * <p>
   * This is for instance useful for the Alarmstufen
   * 
   * @param obs
   * @param mdPrefix
   * @return list of metadata keys or empty array if nothing found
   */
  public static String[] findOutMDBeginningWith( final IObservation obs, final String mdPrefix )
  {
    if( obs == null )
      return ArrayUtils.EMPTY_STRING_ARRAY;

    final MetadataList mdl = obs.getMetadataList();

    final ArrayList<String> mds = new ArrayList<String>();

    final Set<Object> keySet = mdl.keySet();
    for( final Object object : keySet )
    {
      final String md = object.toString();

      if( md.startsWith( mdPrefix ) )
        mds.add( md );
    }

    return mds.toArray( new String[mds.size()] );
  }

  /**
   * Finds out the list of alarmstufen metadata keys
   * 
   * @return list of metadata keys
   */
  public static String[] findOutMDAlarmLevel( final IObservation obs )
  {
    return findOutMDBeginningWith( obs, "Alarmstufe" ); //$NON-NLS-1$
  }

  /**
   * Returns the color to use when displaying the value of the given Alarmstufe.
   * 
   * @return color
   */
  public static Color getColorForAlarmLevel( final String mdAlarm )
  {
    final String strColor = getProperties().getProperty( "COLOR_" + mdAlarm ); //$NON-NLS-1$
    if( strColor == null )
      return Color.RED;

    return StringUtilities.stringToColor( strColor );
  }

  /**
   * Lazy loading of the properties
   * 
   * @return config of the timeseries package
   */
  private static synchronized Properties getProperties( )
  {
    if( CONFIG == null )
    {
      CONFIG = new Properties();

      final Properties defaultConfig = new Properties();
      CONFIG = new Properties( defaultConfig );

      // The config file in the sources is used as defaults
      PropertiesUtilities.loadI18nProperties( defaultConfig, CONFIG_BASE_URL, BASENAME );

      // TODO: also load configured properties via i18n mechanism
      InputStream configIs = null;
      try
      {
        // If we have a configured config file, use it as standard
        final URL configUrl = Platform.isRunning() ? Platform.getConfigurationLocation().getURL() : null;
        final String timeseriesConfigLocation = System.getProperty( PROP_TIMESERIES_CONFIG );
        final URL timeseriesConfigUrl = timeseriesConfigLocation == null ? null : new URL( configUrl, timeseriesConfigLocation );

        // TODO: load time series ini from local config: ni order to support debugging correctly, sue this pattern:
        // final URL proxyConfigLocation = HwvProductSachsenAnhalt.findConfigLocation( CONFIG_PROXY_PATH );

        try
        {
          if( timeseriesConfigUrl != null )
            configIs = timeseriesConfigUrl.openStream();
        }
        catch( final Throwable t )
        {
          // ignore: there is no config file; we are using standard instead
          final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, "Specified timeseries config file at " + timeseriesConfigUrl.toExternalForm() //$NON-NLS-1$
              + " does not exist. Using default settings.", null ); //$NON-NLS-1$
          KalypsoCorePlugin.getDefault().getLog().log( status );

          t.printStackTrace();
        }

        if( configIs != null )
        {
          CONFIG.load( configIs );
          configIs.close();
        }
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
      finally
      {
        IOUtils.closeQuietly( configIs );
      }
    }
    return CONFIG;
  }

  /**
   * Returns a new instance of DateRangeArgument containing the beginning and the end of the forecast, given the
   * observation is a forecast.
   * <p>
   * An observation is a forecast when it has the MD_VORHERSAGE Metadata.
   * 
   * @param obs
   * @return date range of the forecast or null if obs isn't a forecast.
   */
  public static DateRange isTargetForecast( final IObservation obs )
  {
    if( obs == null )
      return null;

    final MetadataList mdl = obs.getMetadataList();

    final String forecastFrom = mdl.getProperty( ITimeseriesConstants.MD_VORHERSAGE_START );
    final String forecastTo = mdl.getProperty( ITimeseriesConstants.MD_VORHERSAGE_ENDE );
    if( forecastFrom != null || forecastTo != null )
    {
      // new version: if one of the two is set, we assume that the zml is in new format
      final Date from = forecastFrom == null ? null : DateUtilities.parseDateTime( forecastFrom );
      final Date to = forecastTo == null ? null : DateUtilities.parseDateTime( forecastTo );
      return new DateRange( from, to );
    }

    // Backwards compability: still try to parse old 'Vorhersage' metadata
    final String range = mdl.getProperty( ITimeseriesConstants.MD_VORHERSAGE );
    if( range != null )
    {
      final String[] splits = range.split( ";" ); //$NON-NLS-1$
      if( splits.length == 2 )
      {
        final String fromStr = splits[0];
        final String toStr = splits[1];

        try
        {
          final Date from = DateUtilities.parseDateTime( fromStr );
          final Date to = DateUtilities.parseDateTime( toStr );
          return new DateRange( from, to );
        }
        catch( final IllegalArgumentException e )
        {
          // ignore, probably it is an old zml
        }

        // TRICKY: in order to support backwards compatibility, we still try to parse the old format
        try
        {
          final Date from = FORECAST_DF.parse( fromStr );
          final Date to = FORECAST_DF.parse( toStr );
          return new DateRange( from, to );
        }
        catch( final ParseException e )
        {
          e.printStackTrace();
        }
      }
    }

    return null;
  }

  /**
   * Units are read from the config.properties file.
   * 
   * @param type
   * @return corresponding unit
   */
  public static String getUnit( final String type )
  {
    return getProperties().getProperty( "AXISUNIT_" + type, "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns a user-friendly name for the given type.
   * <p>
   * Note to Developer: keep the config.properties file up-to-date
   * 
   * @return corresponding name (user friendly)
   */
  public static String getName( final String type )
  {
    return getProperties().getProperty( "AXISNAME_" + type, "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns a color for the given type.
   * <p>
   * Note to Developer: keep the config.properties file up-to-date
   * 
   * @return a Color that is defined to be used with the given axis type, or a random color when no fits
   */
  public static Color[] getColorsFor( final String type )
  {
    final String strColor = getProperties().getProperty( "AXISCOLOR_" + type ); //$NON-NLS-1$

    if( strColor == null )
      return new Color[] { ColorUtilities.random() };

    final String[] strings = strColor.split( "#" ); //$NON-NLS-1$
    if( strings.length == 0 )
      return new Color[] { ColorUtilities.random() };

    final Color[] colors = new Color[strings.length];
    for( int i = 0; i < colors.length; i++ )
      colors[i] = StringUtilities.stringToColor( strings[i] );

    return colors;
  }

  /**
   * @param mdKey
   * @return color for the given Metadata information
   */
  public static Color getColorForMD( final String mdKey )
  {
    final String strColor = getProperties().getProperty( "MDCOLOR_" + mdKey ); //$NON-NLS-1$

    if( strColor != null )
      return StringUtilities.stringToColor( strColor );

    // no color found? so return random one
    return ColorUtilities.random();
  }

  /**
   * @return true if the axis type is known to be a key axis
   */
  public static boolean isKey( final String type )
  {
    return Boolean.valueOf( getProperties().getProperty( "IS_KEY_" + type, "false" ) ).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Create a default axis for the given type.
   */
  public static IAxis createDefaultAxis( final String type )
  {
    return new DefaultAxis( getName( type ), type, getUnit( type ), getDataClass( type ), isKey( type ) );
  }

  /**
   * Create a default axis for the given type and the key flag.
   */
  public static IAxis createDefaultAxis( final String type, final boolean isKey )
  {
    return new DefaultAxis( getName( type ), type, getUnit( type ), getDataClass( type ), isKey );
  }

  /**
   * Returns a NumberFormat instance according to the given timeserie type. If there is no specific instance for the
   * given type, then a default number format is returned.
   * 
   * @return instance of NumberFormat that can be used to display the values to the user
   */
  public static NumberFormat getNumberFormatFor( final String type )
  {
    return getNumberFormat( getDefaultFormatString( type ) );
  }

  /**
   * Returns the adequate NumberFormat for the given format-string. It currently only supports formats of the form %X.Yf
   * where actually only the Y is used to build a NumberFormat with Y minimum/maximum-fraction-digits.
   * <p>
   * The plan is, once we'll be using JDK 5.0, we'll try to replace this with the built-in functionality provided with
   * formated printing.
   * <p>
   * TODO once on JDK 5.0 use formated printing if possible. Note that some refactoring might need to be done since we
   * currently work with NumberFormats.
   */
  public static synchronized NumberFormat getNumberFormat( final String format )
  {
    final NumberFormat nf = FORMAT_MAP.get( format );
    if( nf != null )
      return nf;

    if( "%d".equals( format ) ) //$NON-NLS-1$
    {
      final NumberFormat wf = NumberFormat.getIntegerInstance();
      wf.setGroupingUsed( false );
      FORMAT_MAP.put( format, wf );
      return wf;
    }

    // parse the format spec and only take the min-fraction-digit part
    final String regex = "%([0-9]*)\\.?([0-9]*)f"; //$NON-NLS-1$
    final Pattern pattern = Pattern.compile( regex );
    final Matcher matcher = pattern.matcher( format );
    if( matcher.matches() )
    {
      final String minfd = matcher.group( 2 );

      final NumberFormat wf = NumberFormat.getInstance();
      final int intValue = Integer.valueOf( minfd ).intValue();
      wf.setMinimumFractionDigits( intValue );
      wf.setMaximumFractionDigits( intValue );
      FORMAT_MAP.put( format, wf );

      return wf;
    }

    return getDefaultFormat();
  }

  private static synchronized NumberFormat getDefaultFormat( )
  {
    if( DEFAULT_FORMAT == null )
    {
      DEFAULT_FORMAT = NumberFormat.getNumberInstance();
      DEFAULT_FORMAT.setMinimumFractionDigits( 3 );
    }

    return DEFAULT_FORMAT;
  }

  /**
   * It is currently fix and is: "dd.MM.yy HH:mm"
   * 
   * @return the date format to use when displaying dates for observations/timeseries
   */
  public static DateFormat getDateFormat( )
  {
    final DateFormat sdf = new SimpleDateFormat( "dd.MM.yy HH:mm" ); //$NON-NLS-1$
    final TimeZone timeZone = KalypsoCorePlugin.getDefault().getTimeZone();
    sdf.setTimeZone( timeZone );

    return sdf;
  }

  public static Class< ? > getDataClass( final String type )
  {
    try
    {
      return Class.forName( getProperties().getProperty( "AXISCLASS_" + type, "" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch( final ClassNotFoundException e )
    {
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.TimeserieUtils.19" ) + type ); //$NON-NLS-1$
    }
  }

  public static IAxis[] createDefaultAxes( final String[] axisTypes, final boolean firstWithKey )
  {
    final List<IAxis> axisList = new ArrayList<IAxis>();
    if( axisTypes != null && axisTypes.length > 0 )
    {
      axisList.add( TimeseriesUtils.createDefaultAxis( axisTypes[0], firstWithKey ) );
      for( int i = 1; i < axisTypes.length; i++ )
      {
        axisList.add( TimeseriesUtils.createDefaultAxis( axisTypes[i], false ) );
      }
    }
    return axisList.toArray( new IAxis[axisList.size()] );
  }

  /**
   * @return the default format string for the given type
   */
  public static String getDefaultFormatString( final String type )
  {
    return getProperties().getProperty( "FORMAT_" + type ); //$NON-NLS-1$
  }

  /**
   * @return the default top margin defined for the given type or null if none
   */
  public static Double getTopMargin( final String type )
  {
    final String margin = getProperties().getProperty( "TOP_MARGIN_" + type ); //$NON-NLS-1$
    if( margin == null )
      return null;

    return Double.valueOf( margin );
  }

  /**
   * @param gkr
   *          the Gausskr�ger Rechtswert as string
   * @return the corresponding Gausskr�ger Coordinate System Name
   */
  public static String getCoordinateSystemNameForGkr( final String gkr )
  {
    final String crsName = getProperties().getProperty( "GK_" + gkr.substring( 0, 1 ), null ); //$NON-NLS-1$
    if( crsName == null )
      KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    return crsName;
  }

  /**
   * Return the value of the alarmLevel in regard to the given axisType. The alarm-levels are stored according to the
   * W-axis. If you want the value according to the Q-axis you should call this function with axisType = Q
   * 
   * @param axisType
   *          the type of the axis for which to convert the alarm-level
   * @throws WQException
   */
  public static Double convertAlarmLevel( final IObservation obs, final ITupleModel model, final Integer index, final String axisType, final Double alarmLevel ) throws SensorException, WQException
  {
    if( axisType.equals( ITimeseriesConstants.TYPE_WATERLEVEL ) )
      return alarmLevel;

    final IWQConverter converter = WQFactory.createWQConverter( obs );

    if( axisType.equals( ITimeseriesConstants.TYPE_RUNOFF ) || axisType.equals( ITimeseriesConstants.TYPE_VOLUME ) )
      return new Double( converter.computeQ( model, index, alarmLevel.doubleValue() ) );

    throw new WQException( Messages.getString( "org.kalypso.ogc.sensor.timeseries.TimeserieUtils.22" ) + axisType + Messages.getString( "org.kalypso.ogc.sensor.timeseries.TimeserieUtils.23" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns the class name for the given axis-type. The class must inherit from
   * <code>org.jfree.chart.axis.ValueAxis</code>.
   * 
   * @return The class name for the given axis-type. The class must inherit from
   *         <code>org.jfree.chart.axis.ValueAxis</code>.
   */
  public static String getAxisClassFor( final String type )
  {
    return getProperties().getProperty( "AXISJFREECHARTCLASS_" + type, null ); //$NON-NLS-1$
  }

  public static DateRange getDateRange( final IRequest args )
  {
    if( args == null )
      return null;

    return args.getDateRange();
  }

  /**
   * This function guesses the timestep of a given timeseries from several timsteps.
   * 
   * @param timeseries
   *          The tuple model of a timeseries.
   * @return The timestep or null.
   */
  public static Period guessTimestep( final ITupleModel timeseries ) throws SensorException
  {
    final TimestepGuesser timestepGuesser = new TimestepGuesser( timeseries, -1 );
    return timestepGuesser.execute();
  }

  /**
   * This function guesses the timestamp of a given timeseries from several timsteps. Only timeseries with a timestep of
   * 1 day do have a timestamp.
   * 
   * @param timeseries
   *          The tuple model of a timeseries.
   * @param timestep
   *          The timestep of the timeseries.
   * @return The timestamp in UTC or null.
   */
  public static LocalTime guessTimestamp( final ITupleModel timeseries, final Period timestep ) throws SensorException
  {
    /* The timestamp is only relevant for day values. */
    if( timestep != null && timestep.toStandardMinutes().getMinutes() == 1440 )
    {
      /* Guess the timestamp of the timeseries. */
      final TimestampGuesser guesser = new TimestampGuesser( timeseries, -1 );
      return guesser.execute();
    }

    return null;
  }

  public static TIMESERIES_TYPE getType( final String axisType )
  {
    return TIMESERIES_TYPE.getType( axisType );
  }
}