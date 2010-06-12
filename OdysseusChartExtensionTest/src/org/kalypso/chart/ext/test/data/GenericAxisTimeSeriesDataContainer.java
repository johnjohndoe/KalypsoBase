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
package org.kalypso.chart.ext.test.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SimpleTimeZone;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;

/**
 * @author burtscher1
 * 
 * DataContainer zum Testen, ob das generische Achsen-Konzept funktioniert
 * 
 */
public class GenericAxisTimeSeriesDataContainer implements IListDataContainer<Calendar, Number>
{

  private final List<Number> m_targetData;

  private final List<Calendar> m_domainData;

  private final ComparableDataRange<Number> m_targetRange;

  private final ComparableDataRange<Calendar> m_domainRange;

  @SuppressWarnings("cast")
  public GenericAxisTimeSeriesDataContainer( int size, double targetRange )
  {
    m_targetData = new ArrayList<Number>();
    m_domainData = new ArrayList<Calendar>();

    Calendar startCal = Calendar.getInstance();
    startCal.setTimeZone( new SimpleTimeZone( 0, "" ) );
    startCal.set( Calendar.YEAR, 2007 );
    startCal.set( Calendar.MONTH, 0 );
    startCal.set( Calendar.DAY_OF_MONTH, 1 );
    startCal.set( Calendar.HOUR, 12 );
    startCal.set( Calendar.MINUTE, 0 );
    startCal.set( Calendar.SECOND, 0 );
    startCal.set( Calendar.MILLISECOND, 0 );

    for( int i = 0; i < size; i++ )
    {
      Calendar curCal = (Calendar) startCal.clone();
      curCal.setTimeZone( new SimpleTimeZone( 0, "" ) );
      m_domainData.add( curCal );
      m_targetData.add( Math.random() * targetRange );
      // Datum hochz‰hlen
      startCal.add( Calendar.DATE, 1 );
    }

    m_targetRange = new ComparableDataRange<Number>( (Number[]) m_targetData.toArray( new Number[] {} ) );
    m_domainRange = new ComparableDataRange<Calendar>( (Calendar[]) m_domainData.toArray( new Calendar[] {} ) );

    System.out.println();
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataContainer#close()
   */
  @Override
public void close( )
  {

  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataContainer#getDomainRange()
   */
  @Override
public IDataRange<Calendar> getDomainRange( )
  {
    return m_domainRange;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataContainer#getTargetRange()
   */
  @Override
public IDataRange<Number> getTargetRange( )
  {
    return m_targetRange;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataContainer#isOpen()
   */
  @Override
public boolean isOpen( )
  {
    return true;
  }

  /**
   * @see org.kalypso.chart.framework.model.data.IDataContainer#open()
   */
  @Override
public void open( )
  {
    // TODO Auto-generated method stub

  }

  @Override
public List<Number> getTargetValues( )
  {
    return m_targetData;
  }

  @Override
public List<Calendar> getDomainValues( )
  {
    return m_domainData;
  }

}
