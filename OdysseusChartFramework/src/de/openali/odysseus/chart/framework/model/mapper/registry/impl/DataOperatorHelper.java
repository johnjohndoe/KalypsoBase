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
package de.openali.odysseus.chart.framework.model.mapper.registry.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.BooleanDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.CalendarDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.DateDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.DummyDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.NumberDataOperator;

/**
 * all data operators for data types which have a unambiguous relation to real numbers should be stored here, so all
 * mappers can access them.
 *
 * @author burtscher1
 */
public class DataOperatorHelper
{
  private final Map<Class< ? >, IDataOperator< ? >> m_dataOperators = new HashMap<>();

  @SuppressWarnings( "unchecked" )
  public DataOperatorHelper( )
  {
    m_dataOperators.put( Number.class, new NumberDataOperator( new NumberComparator() ) );
    m_dataOperators.put( Boolean.class, new BooleanDataOperator( new ComparableComparator() ) );
    m_dataOperators.put( Calendar.class, new CalendarDataOperator( new ComparableComparator(), "dd.MM.yyyy HH:mm" ) ); //$NON-NLS-1$
    m_dataOperators.put( Date.class, new DateDataOperator( new ComparableComparator(), "dd.MM.yyyy HH:mm" ) ); //$NON-NLS-1$
  }

  public <T> IDataOperator<T> getDataOperator( final Class<T> clazz )
  {
    for( final Class< ? > c : m_dataOperators.keySet() )
    {
      if( c.isAssignableFrom( clazz ) )
        return (IDataOperator<T>)m_dataOperators.get( c );
    }

    return new DummyDataOperator<>();
  }
}