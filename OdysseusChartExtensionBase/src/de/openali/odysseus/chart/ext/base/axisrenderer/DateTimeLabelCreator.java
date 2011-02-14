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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.chrono.GregorianChronology;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

;

/**
 * @author kimwerner
 */
public class DateTimeLabelCreator extends AbstractLabelCreator implements ILabelCreator
{
  private final TickUnitSource createStandardDateTickUnits( final TimeZone zone )
  {
    if( zone == null )
      throw new IllegalArgumentException( "Null 'zone' argument." ); //$NON-NLS-1$

    final TickUnits units = new TickUnits();

    final DateFormat f1 = new SimpleDateFormat( "dd.MM HH:mm:ss.SSS" ); //$NON-NLS-1$
    final DateFormat f2 = new SimpleDateFormat( "dd.MM HH:mm:ss" ); //$NON-NLS-1$
    final DateFormat f3 = new SimpleDateFormat( "dd.MM HH:mm" ); //$NON-NLS-1$
    final DateFormat f4 = new SimpleDateFormat( "dd.MM HH:mm" ); //$NON-NLS-1$
    final DateFormat f5 = new SimpleDateFormat( "dd.MM" ); //$NON-NLS-1$
    final DateFormat f6 = new SimpleDateFormat( "dd.MM.yy" ); //$NON-NLS-1$
    final DateFormat f7 = new SimpleDateFormat( "yyyy" ); //$NON-NLS-1$

    f1.setTimeZone( zone );
    f2.setTimeZone( zone );
    f3.setTimeZone( zone );
    f4.setTimeZone( zone );
    f5.setTimeZone( zone );
    f6.setTimeZone( zone );
    f7.setTimeZone( zone );

    // milliseconds
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 5, DateTickUnitType.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 10, DateTickUnitType.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 25, DateTickUnitType.MILLISECOND, 5, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 50, DateTickUnitType.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 100, DateTickUnitType.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 250, DateTickUnitType.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnitType.MILLISECOND, 500, DateTickUnitType.MILLISECOND, 50, f1 ) );

    // seconds
    units.add( new DateTickUnit( DateTickUnitType.SECOND, 1, DateTickUnitType.MILLISECOND, 50, f2 ) );
    units.add( new DateTickUnit( DateTickUnitType.SECOND, 5, DateTickUnitType.SECOND, 1, f2 ) );
    units.add( new DateTickUnit( DateTickUnitType.SECOND, 10, DateTickUnitType.SECOND, 1, f2 ) );
    units.add( new DateTickUnit( DateTickUnitType.SECOND, 30, DateTickUnitType.SECOND, 5, f2 ) );

    // minutes
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 1, DateTickUnitType.SECOND, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 2, DateTickUnitType.SECOND, 10, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 5, DateTickUnitType.MINUTE, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 10, DateTickUnitType.MINUTE, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 15, DateTickUnitType.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 20, DateTickUnitType.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.MINUTE, 30, DateTickUnitType.MINUTE, 5, f3 ) );

    // hours
    units.add( new DateTickUnit( DateTickUnitType.HOUR, 1, DateTickUnitType.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.HOUR, 2, DateTickUnitType.MINUTE, 10, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.HOUR, 4, DateTickUnitType.MINUTE, 30, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.HOUR, 6, DateTickUnitType.HOUR, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnitType.HOUR, 12, DateTickUnitType.HOUR, 1, f4 ) );

    // days
    units.add( new DateTickUnit( DateTickUnitType.DAY, 1, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 2, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 3, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 4, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 5, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 6, DateTickUnitType.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 7, DateTickUnitType.DAY, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 10, DateTickUnitType.DAY, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnitType.DAY, 15, DateTickUnitType.DAY, 1, f5 ) );

    // months
    units.add( new DateTickUnit( DateTickUnitType.MONTH, 1, DateTickUnitType.DAY, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnitType.MONTH, 2, DateTickUnitType.DAY, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnitType.MONTH, 3, DateTickUnitType.MONTH, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnitType.MONTH, 4, DateTickUnitType.MONTH, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnitType.MONTH, 6, DateTickUnitType.MONTH, 1, f6 ) );

    // years
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 1, DateTickUnitType.MONTH, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 2, DateTickUnitType.MONTH, 3, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 5, DateTickUnitType.YEAR, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 10, DateTickUnitType.YEAR, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 25, DateTickUnitType.YEAR, 5, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 50, DateTickUnitType.YEAR, 10, f7 ) );
    units.add( new DateTickUnit( DateTickUnitType.YEAR, 100, DateTickUnitType.YEAR, 20, f7 ) );

    return units;
  }

  private final DateTimeFieldType m_field;

  /**
   * @param formatString
   *          z.B. "yyyy-MM-dd\nhh:mm:ss"
   */
  public DateTimeLabelCreator( final DateTimeFieldType tickRaster )
  {
    m_field = tickRaster;
  }

  /**
   * @param value
   *          date in milliseconds
   * @param range
   *          can be null, is not evaluated
   * @see org.kalypso.chart.ext.test.axisrenderer.ILabelCreator#getLabel(java.lang.Number)
   */
  @Override
  public String getLabel( final Number[] ticks, final int i, final IDataRange<Number> range )
  {
    return getLabel( ticks[i], range );
  }

  private DateFormat getFormat( final IDataRange<Number> range )
  {
// if(m_format!=null)
// return m_format;
    // FIXME: make it work
    return new SimpleDateFormat( "" );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number value, final IDataRange<Number> range )
  {
    final DateTimeField field = m_field.getField( GregorianChronology.getInstance() );
    final long dateTime = field.roundFloor( value.longValue() );
    return new DateTime( dateTime ).toString("MM-dd\nhh:mm");
  }

}