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

import org.joda.time.DateTimeFieldType;

/**
 * @author kimwerner
 */
public class DateTimeAxisField implements IDateTimeAxisField
{
 
  private final int[] m_rollovers;

  private final int[] m_beginners;

  private final String m_formatString;

  private  final DateTimeFieldType m_fieldType;
  
  public DateTimeAxisField( final DateTimeFieldType fieldType, final String formatString, final int[] rollovers, final int[] beginners )
  {
    super();
    m_rollovers = rollovers;
    m_beginners = beginners;
    m_formatString = formatString;
    m_fieldType=fieldType;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField#getBeginners()
   */
  @Override
  public int[] getBeginners( )
  {
   return m_beginners;
  }

  

  @Override
  public DateTimeFieldType getFieldType( )
  {
    return m_fieldType;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField#getFormatString()
   */
  @Override
  public String getFormatString( )
  {
    return m_formatString;
  }



  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField#getRollovers()
   */
  @Override
  public int[] getRollovers( )
  {
    return m_rollovers;
  }

}
