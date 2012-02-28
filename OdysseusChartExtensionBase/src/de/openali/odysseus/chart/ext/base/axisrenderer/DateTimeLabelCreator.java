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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.kalypso.core.KalypsoCorePlugin;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author kimwerner
 */
public class DateTimeLabelCreator implements ILabelCreator
{
  private final IDateTimeAxisFieldProvider m_dateTimeFieldProvider;

  public DateTimeLabelCreator( final IDateTimeAxisFieldProvider dateTimeFieldProvider )
  {
    m_dateTimeFieldProvider = dateTimeFieldProvider;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number value, final IDataRange<Number> range )
  {
//    final TimeZone kalypsoTZ = KalypsoCorePlugin.getDefault().getTimeZone();
//    final FixedDateTimeZone jodaTZ = new FixedDateTimeZone( kalypsoTZ.getID(), null, kalypsoTZ.getOffset(  value.longValue() ), kalypsoTZ.getOffset(  value.longValue() ) );
//  
    final IDateTimeAxisField axisField = m_dateTimeFieldProvider.getDateTimeAxisField( range );
    final DateTimeZone zone = DateTimeZone.forTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
    final DateTime dateTime = new DateTime( value.longValue(), zone );

    return dateTime.toString( axisField.getFormatString() );
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
}
