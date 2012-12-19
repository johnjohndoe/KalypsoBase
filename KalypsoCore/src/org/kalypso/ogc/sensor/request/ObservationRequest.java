/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.request;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.kalypso.commons.time.PeriodUtils;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.zml.request.CalandarField;
import org.kalypso.zml.request.Request;
import org.kalypso.zml.request.Request.Timestep;

/**
 * Contains the request information for an observation.
 * 
 * @author schlienger
 */
public class ObservationRequest implements IRequest
{
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  private final DateRange m_dateRange;

  private final String m_name;

  private final String[] m_axisTypes;

  private final String[] m_axisTypesWithStatus;

  private final Period m_timestep;

  public ObservationRequest( )
  {
    this( null );
  }

  public ObservationRequest( final Date from, final Date to )
  {
    this( new DateRange( from, to ) );
  }

  public ObservationRequest( final DateRange dr )
  {
    this( dr, null, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY );
  }

  public ObservationRequest( final String name, final String[] axisTypes, final String[] axisTypesWithStatus )
  {
    this( null, name, axisTypes, axisTypesWithStatus );
  }

  public ObservationRequest( final DateRange dr, final String name, final String[] axisTypes, final String[] axisTypesWithStatus )
  {
    this( dr, name, axisTypes, axisTypesWithStatus, null );
  }

  public ObservationRequest( final DateRange dr, final String name, final String[] axisTypes, final String[] axisTypesWithStatus, final Period timestep )
  {
    m_dateRange = dr;
    m_name = name;
    m_axisTypes = axisTypes;
    m_axisTypesWithStatus = axisTypesWithStatus;
    m_timestep = timestep;
  }

  @Override
  public DateRange getDateRange( )
  {
    return m_dateRange;
  }

  @Override
  public String getName( )
  {
    return m_name;
  }

  @Override
  public String[] getAxisTypes( )
  {
    return m_axisTypes;
  }

  @Override
  public String[] getAxisTypesWithStatus( )
  {
    return m_axisTypesWithStatus;
  }

  @Override
  public String toString( )
  {
    final StringBuffer bf = new StringBuffer();

    if( m_dateRange != null )
      bf.append( Messages.getString( "org.kalypso.ogc.sensor.request.ObservationRequest.0" ) ).append( m_dateRange.toString() ).append( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    if( m_name != null )
      bf.append( Messages.getString( "org.kalypso.ogc.sensor.request.ObservationRequest.2" ) ).append( m_name ).append( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    if( m_axisTypes.length > 0 )
      bf.append( Messages.getString( "org.kalypso.ogc.sensor.request.ObservationRequest.4" ) ).append( StringUtils.join( m_axisTypes, ',' ) ).append( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    if( m_axisTypesWithStatus.length > 0 )
      bf.append( Messages.getString( "org.kalypso.ogc.sensor.request.ObservationRequest.6" ) ).append( StringUtils.join( m_axisTypesWithStatus, ',' ) ).append( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    return bf.toString();
  }

  public static ObservationRequest createWith( final Request requestType )
  {
    if( requestType == null )
      return new ObservationRequest();

    final DateRange dr = toDateRange( requestType );

    final String[] axisTypes = toAxisTypes( requestType );

    final String[] axisTypesWithStatus = toAxisTypesWithStatus( requestType );

    final String name = toName( requestType );

    final Period timestep = toTimestep( requestType.getTimestep() );

    return new ObservationRequest( dr, name, axisTypes, axisTypesWithStatus, timestep );
  }

  private static Period toTimestep( final Timestep timestep )
  {
    if( timestep == null )
      return null;

    final int amount = timestep.getAmount();

    final CalandarField field = timestep.getField();
    final int calendarField = CalendarUtilities.getCalendarField( field.name() );

    return PeriodUtils.getPeriod( calendarField, amount );
  }

  private static String toName( final Request requestType )
  {
    if( requestType.getName() == null )
      return Messages.getString( "org.kalypso.ogc.sensor.request.ObservationRequest.8" ); //$NON-NLS-1$

    return requestType.getName();
  }

  private static String[] toAxisTypesWithStatus( final Request requestType )
  {
    if( requestType.getStatusAxes() == null )
      return new String[0];

    return StringUtils.split( requestType.getStatusAxes(), ',' );
  }

  private static String[] toAxisTypes( final Request requestType )
  {
    if( requestType.getAxes() == null )
      return EMPTY_STRING_ARRAY;

    return StringUtils.split( requestType.getAxes(), ',' );
  }

  private static DateRange toDateRange( final Request requestType )
  {
    final Calendar dateFrom = requestType.getDateFrom();
    final Calendar dateTo = requestType.getDateTo();

    final Date from = dateFrom == null ? null : dateFrom.getTime();
    final Date to = dateTo == null ? null : dateTo.getTime();

    if( from == null && to == null )
      return null;

    return new DateRange( from, to );
  }

  public Period getTimestep( )
  {
    return m_timestep;
  }
}