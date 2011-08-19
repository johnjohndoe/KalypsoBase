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
package org.kalypso.ogc.sensor.filter.filters.interval.test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kalypso.core.IKalypsoCoreConstants;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalDefinition;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilter;
import org.kalypso.ogc.sensor.filter.filters.interval.IntervalFilterOperation;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.util.ObservationAssert;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.xml.sax.InputSource;

/**
 * Tests the interval filter.
 * 
 * @author Gernot Belger
 */
public class IntervalFilterTest extends Assert
{
  private final DateFormat m_df = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );

  private final boolean m_useOldFilter = false;

  @Before
  public void init( )
  {
    System.setProperty( IKalypsoCoreConstants.CONFIG_PROPERTY_TIMEZONE, "GMT+1" );

    m_df.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
  }

  @Test
  public void testTargetWithinFirstSourceInterval( ) throws Throwable
  {
    final Date start = m_df.parse( "18.03.2011 00:00" );
    final Date end = m_df.parse( "05.04.2011 00:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 1, Double.NaN, KalypsoStati.BIT_CHECK );
    // FIXME: expected result is actually not correct; but we know the filter i buggy at the moment
    final String expectedZml;
    if( m_useOldFilter )
      expectedZml = "expectedSourceTooSmallBUGGY.zml";
    else
      expectedZml = "expectedSourceTooSmall.zml";

    doTestFilter( "days.zml", expectedZml, start, end, intervalDefinition );
  }

  @Test
  public void testFilterOnEmptySource( ) throws Throwable
  {
    final Date start = m_df.parse( "04.04.2011 00:00" );
    final Date end = m_df.parse( "05.04.2011 00:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 1, Double.NaN, KalypsoStati.BIT_CHECK );
    doTestFilter( "empty.zml", "expectedFromEmpty.zml", start, end, intervalDefinition );
  }

  @Test
  public void testAggregation( ) throws Throwable
  {
    final Date start = m_df.parse( "02.04.2011 07:00" );
    final Date end = m_df.parse( "09.04.2011 07:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 24, Double.NaN, KalypsoStati.BIT_CHECK );
    doTestFilter( "dresden-klotsche1h_7Tage.zml", "expectedFromDresden2Tage.zml", start, end, intervalDefinition );
  }

  @Test
  public void testBigAggregation( ) throws Throwable
  {
    final Date start = m_df.parse( "01.03.2009 00:00" );
    final Date end = m_df.parse( "01.03.2011 00:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 24, Double.NaN, KalypsoStati.BIT_CHECK );

    final String expectedZml;
    if( m_useOldFilter )
      expectedZml = "expectedFromDresden777TageBUGGY.zml";
    else
      expectedZml = "expectedFromDresden777Tage.zml";

    doTestFilter( "dresden-klotsche1h_777Tage.zml", expectedZml, start, end, intervalDefinition );
  }

  @Test
  public void testDisaggregation( ) throws Throwable
  {
    final Date start = m_df.parse( "04.04.2011 00:00" );
    final Date end = m_df.parse( "05.04.2011 00:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 6, Double.NaN, KalypsoStati.BIT_CHECK );
    doTestFilter( "days.zml", "expectedDaysTo6hours.zml", start, end, intervalDefinition );
  }

  @Test
  public void testSourceHasHoles( ) throws Throwable
  {
    final Date start = m_df.parse( "17.03.2011 06:00" );
    final Date end = m_df.parse( "10.04.2011 06:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 1, Double.NaN, KalypsoStati.BIT_CHECK );
    // FIXME: expected result is actually not correct; but we know the filter i buggy at the moment
    String expectedZml;
    if( m_useOldFilter )
      expectedZml = "expectedHasHoles24BUGGY.zml";
    else
      expectedZml = "expectedHasHoles24.zml";

    doTestFilter( "hasHoles24h.zml", expectedZml, start, end, intervalDefinition );
  }

  @Test
  public void testSourceAndTargetIdentical( ) throws Throwable
  {
    final Date start = m_df.parse( "17.03.2011 06:00" );
    final Date end = m_df.parse( "08.04.2011 06:00" );

    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 24, Double.NaN, KalypsoStati.BIT_CHECK );
    doTestFilter( "days.zml", "days.zml", start, end, intervalDefinition );
  }

  @Test
  @Ignore
  public void testPerformanceLoadOften( ) throws Throwable
  {
    for( int i = 0; i < 100; i++ )
      testBigAggregation();
  }

  @Test
  @Ignore
  public void testPerformanceFilterOften( ) throws Exception
  {
    final IObservation sourceObservation = readZml( "dresden-klotsche1h_777Tage.zml" );
    final Date start = m_df.parse( "01.03.2009 00:00" );
    final Date end = m_df.parse( "01.03.2011 00:00" );
    final IntervalDefinition intervalDefinition = new IntervalDefinition( Calendar.HOUR_OF_DAY, 24, Double.NaN, KalypsoStati.BIT_CHECK );

    for( int i = 0; i < 1000; i++ )
    {
      createIntervalObservation( sourceObservation, intervalDefinition, start, end );
    }
  }

  private void doTestFilter( final String sourceZml, final String expectedZml, final Date start, final Date end, final IntervalDefinition intervalDefinition ) throws Throwable
  {
    try
    {
      // create example (source and expected target)
      final IObservation sourceObservation = readZml( sourceZml );
      final IObservation expectedObservation = readZml( expectedZml );

      final IObservation filteredObservation = createIntervalObservation( sourceObservation, intervalDefinition, start, end );

      doCompareObservations( expectedObservation, filteredObservation );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw e;
    }
  }

  private IObservation createIntervalObservation( final IObservation sourceObservation, final IntervalDefinition intervalDefinition, final Date start, final Date end ) throws Exception
  {
    if( m_useOldFilter )
      return doFilter( sourceObservation, intervalDefinition, start, end );

    return doWorker( sourceObservation, intervalDefinition, start, end );
  }

  private IObservation doFilter( final IObservation sourceObservation, final IntervalDefinition intervalDefinition, final Date start, final Date end ) throws Exception
  {
    final IntervalFilter filter = new IntervalFilter( IntervalFilter.MODE.eSum, intervalDefinition );
    filter.initFilter( null, sourceObservation, null );

    return resolveObservation( filter, start, end );
  }

  private IObservation doWorker( final IObservation sourceObservation, final IntervalDefinition intervalDefinition, final Date start, final Date end ) throws Exception
  {
    final IntervalFilterOperation operation = new IntervalFilterOperation( sourceObservation, intervalDefinition );
    final DateRange range = new DateRange( start, end );
    final IObservation result = operation.execute( range );
    // REMARK: for better comparison to the filter, we also make another copy here
    return resolveObservation( result, start, end );
  }

  private IObservation resolveObservation( final IObservation input, final Date start, final Date end ) throws Exception
  {
    final IRequest request = new ObservationRequest( start, end );
    final String asString = ZmlFactory.writeToString( input, request );

    final InputStream asStream = IOUtils.toInputStream( asString, CharEncoding.UTF_8 );
    final InputSource asSource = new InputSource( asStream );

    return ZmlFactory.parseXML( asSource, null );
  }

  private IObservation readZml( final String resource ) throws SensorException, MalformedURLException
  {
    final URL location = resource == null ? new URL( "file://LEER" ) : getClass().getResource( "resources/" + resource );

    assertNotNull( location );

    return ZmlFactory.parseXML( location );
  }

  private void doCompareObservations( final IObservation expectedObservation, final IObservation actualObservation ) throws Throwable
  {
    // System.out.println( ObservationUtilities.dump( filteredObservation.getValues( null ), "\t" ) );

    try
    {
      final ObservationAssert obsAssert = new ObservationAssert( expectedObservation, actualObservation );
      obsAssert.ignoreHref();

      obsAssert.assertEquals();
    }
    catch( final Throwable e )
    {
      System.out.println( ZmlFactory.writeToString( actualObservation, null ) );
      throw e;
    }
  }
}