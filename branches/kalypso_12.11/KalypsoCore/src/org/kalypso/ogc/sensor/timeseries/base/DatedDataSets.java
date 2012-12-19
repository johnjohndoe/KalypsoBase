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
package org.kalypso.ogc.sensor.timeseries.base;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * @author Dirk Kuch
 */
public class DatedDataSets
{
  public static final Comparator<DatedDataSets> COMPARATOR = new Comparator<DatedDataSets>()
  {
    @Override
    public int compare( final DatedDataSets v1, final DatedDataSets v2 )
    {
      return v1.getDate().compareTo( v2.getDate() );
    }
  };

  private final Date m_date;

  Map<IAxis, TupleModelDataSet> m_dataSets = new LinkedHashMap<>();

  public DatedDataSets( final Date date, final TupleModelDataSet... dataSets )
  {
    m_date = date;

    for( final TupleModelDataSet dataSet : dataSets )
    {
      m_dataSets.put( dataSet.getValueAxis(), dataSet );
    }

  }

  public Date getDate( )
  {
    return m_date;
  }

  /**
   * @return data sets for the different value axes of one tuple model
   */
  public TupleModelDataSet[] getDataSets( )
  {
    return m_dataSets.values().toArray( new TupleModelDataSet[] {} );
  }

  public TupleModelDataSet getDataSet( final IAxis valueAxis )
  {
    final Set<Entry<IAxis, TupleModelDataSet>> entries = m_dataSets.entrySet();
    for( final Entry<IAxis, TupleModelDataSet> entry : entries )
    {
      if( AxisUtils.isEqual( entry.getKey(), valueAxis ) )
        return entry.getValue();
    }

    return null;
  }
}
