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

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.Interval;
import org.kalypso.ogc.sensor.TupleModelDataSet;

/**
 * @author doemming, Dirk Kuch
 */
public class IntervalData implements IIntervalProvider
{
  private final Interval m_interval;

  private final TupleModelDataSet[] m_datasets;

  public IntervalData( final Interval interval, final TupleModelDataSet[] datasets )
  {
    m_interval = interval;
    m_datasets = datasets;
  }

  @Override
  public Interval getInterval( )
  {
    return m_interval;
  }

  public TupleModelDataSet[] getDataSets( )
  {
    return m_datasets;
  }

  public IntervalData plus( final IntervalData sourceData )
  {
    final TupleModelDataSet[] dataSets = sourceData.getDataSets();
    final TupleModelDataSet[] newDataSets = new TupleModelDataSet[ArrayUtils.getLength( dataSets )];

    for( int index = 0; index < newDataSets.length; index++ )
    {
      final TupleModelDataSet base = m_datasets[index];
      final TupleModelDataSet other = dataSets[index];

      final double value = ((Number) base.getValue()).doubleValue() + ((Number) other.getValue()).doubleValue();
      final int status = base.getStatus() | other.getStatus();
      final String source = IntervalSourceHandler.mergeSourceReference( base.getSource(), other.getSource() );

      newDataSets[index] = new TupleModelDataSet( base.getValueAxis(), value, status, source );
    }

    return new IntervalData( m_interval, newDataSets );
  }
}