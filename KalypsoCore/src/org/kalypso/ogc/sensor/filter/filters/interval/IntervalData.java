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

import org.joda.time.Interval;

/**
 * @author doemming
 */
public class IntervalData
{
  private final Interval m_interval;

  private final double[] m_values;

  private final int[] m_stati;

  private final String m_source;

  public IntervalData( final Interval interval, final double[] values, final int[] stati, final String source )
  {
    m_interval = interval;
    m_values = values;
    m_stati = stati;
    m_source = source;
  }

  public Interval getInterval( )
  {
    return m_interval;
  }

  public double[] getValues( )
  {
    return m_values;
  }

  public int[] getStati( )
  {
    return m_stati;
  }

  public String getSource( )
  {
    return m_source;
  }

  public IntervalData plus( final IntervalData sourceData, final double factor )
  {
    final double[] sourceValues = sourceData.getValues();
    final double[] newValues = new double[sourceValues.length];

    for( int i = 0; i < newValues.length; i++ )
      newValues[i] = m_values[i] + factor * sourceValues[i];

    final int[] sourceStati = sourceData.getStati();
    final int[] newStati = new int[sourceStati.length];
    for( int i = 0; i < newStati.length; i++ )
      newStati[i] = m_stati[i] | sourceStati[i];

    final String newSource = IntervalSourceHandler.mergeSourceReference( m_source, sourceData.getSource() );

    return new IntervalData( m_interval, newValues, newStati, newSource );
  }
}