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
package de.openali.odysseus.chart.ext.base.axisrenderer;

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
  private  DateTimeFieldType m_field;

  private  String m_formatString;

  /**
   * @param formatString
   *          z.B. "yyyy-MM-dd\nhh:mm:ss"
   */
  public DateTimeLabelCreator( final DateTimeFieldType tickRaster, final String formatString )
  {
    m_field = tickRaster;
    m_formatString = formatString;
  }

 

  public DateTimeFieldType getField( )
  {
    return m_field;
  }

  public String getFormatString( )
  {
    return m_formatString;
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
    return new DateTime( dateTime ).toString( m_formatString );
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

  public void setField( final DateTimeFieldType field )
  {
    m_field = field;
  }

 

  public void setFormatString( final String formatString )
  {
    m_formatString = formatString;
  }

}