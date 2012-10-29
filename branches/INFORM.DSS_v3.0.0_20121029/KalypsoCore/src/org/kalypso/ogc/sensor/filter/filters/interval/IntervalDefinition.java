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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.contribs.java.util.CalendarUtilities;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.filters.IntervallFilterType;

/**
 * @author Dirk Kuch
 */
public class IntervalDefinition
{
  private final int m_calendarField;

  private final int m_amount;

  private final String m_startCalendarField;

  private final int m_startCalendarValue;

  private final double m_defaultValue;

  private final int m_defaultStatus;

  public IntervalDefinition( final int calendarField, final int amount, final double defaultValue, final int defaultStatus )
  {
    this( calendarField, amount, defaultValue, defaultStatus, null, 0 );
  }

  public IntervalDefinition( final int calendarField, final int amount, final double defaultValue, final int defaultStatus, final String startCalendarField, final int startCalendarValue )
  {
    m_calendarField = calendarField;
    m_amount = amount;
    m_defaultValue = defaultValue;
    m_defaultStatus = defaultStatus;
    m_startCalendarField = startCalendarField;
    m_startCalendarValue = startCalendarValue;
  }

  public IntervalDefinition( final IntervallFilterType filter )
  {
    this( CalendarUtilities.getCalendarField( filter.getCalendarField() ), filter.getAmount(), filter.getDefaultValue(), filter.getDefaultStatus(), filter.getStartCalendarfield(), filter.getStartCalendarvalue() );
  }

  public int getCalendarField( )
  {
    return m_calendarField;
  }

  public int getAmount( )
  {
    return m_amount;
  }

  public double getDefaultValue( )
  {
    return m_defaultValue;
  }

  public int getDefaultStatus( )
  {
    return m_defaultStatus;
  }

  /**
   * Adjusts the given start date as defined by the 'start' properties.
   */
  public void adjustStart( final Calendar start )
  {
    if( StringUtils.isBlank( m_startCalendarField ) )
      return;

    final int calendarField = CalendarUtilities.getCalendarField( m_startCalendarField );
    start.set( calendarField, m_startCalendarValue );
  }

  public void setTimestep( final MetadataList metadata )
  {
    final int amount = getAmount();
    final int calendarField = getCalendarField();
    /* Directly update metadata with that timestep */
    MetadataHelper.setTimestep( metadata, calendarField, amount );
  }
}
