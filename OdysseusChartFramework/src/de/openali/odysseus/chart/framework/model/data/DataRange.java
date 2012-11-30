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
package de.openali.odysseus.chart.framework.model.data;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Gernot Belger
 */
public class DataRange<T> implements IDataRange<T>
{
  private final T m_min;

  private final T m_max;

  public DataRange( final T min, final T max )
  {
    m_min = min;
    m_max = max;
  }

  @Override
  public T getMin( )
  {
    return m_min;
  }

  @Override
  public T getMax( )
  {
    return m_max;
  }

  @Override
  public String toString( )
  {
    return String.format( "[%s,%s]", m_min, m_max ); //$NON-NLS-1$
  }

  @Override
  public int hashCode( )
  {
    return new HashCodeBuilder( 17, 7 ).append( m_min ).append( m_max ).toHashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
    {
      return false;
    }
    if( obj == this )
    {
      return true;
    }
    if( obj.getClass() != getClass() )
    {
      return false;
    }

    final DataRange< ? > other = (DataRange< ? >) obj;

    return new EqualsBuilder().append( m_min, other.m_min ).append( m_min, other.m_min ).isEquals();
  }

  // FIXME: dieser ganze ausgleichskram gehört nicht in diese Klasse! -> das sollte dort (vielleich mit Hilfer einer
  // Hilfsklasse) and der Stelle passieren wo das Problem entsteht!
  @Deprecated
  public static <T> DataRange<T> create( final T min, final T max )
  {
    if( min instanceof Number && max instanceof Number )
    {
      final Double minNum = ((Number) min).doubleValue();
      final Double maxNum = ((Number) max).doubleValue();

      // Beide gleich => dataRange automatisch so anpassen, dass der Wert
      // in der Intervallmitte liegt
      if( minNum.compareTo( maxNum ) == 0 )
      {
        final double doubleValue = minNum.doubleValue();
        // falls != 0 werden einfach 10% addiert oder subtrahiert
        if( doubleValue != 0 )
        {
          final T minExpanded = (T) new Double( doubleValue - doubleValue * 0.1 );
          final T maxExpanded = (T) new Double( doubleValue + doubleValue * 0.1 );
          return new DataRange<>( minExpanded, maxExpanded );
        }
        // falls == 0 wird 1 addiert oder subtrahiert
        else
        {
          final T min_1 = (T) new Double( doubleValue - 1 );
          final T max_1 = (T) new Double( doubleValue + 1 );
          return new DataRange<>( min_1, max_1 );
        }
      }

      if( minNum.compareTo( maxNum ) > 0 )
        return new DataRange<>( max, min );
      else
        return new DataRange<>( min, max );

    }
    else if( min instanceof Comparable && max instanceof Comparable && (min.getClass().isInstance( max ) || max.getClass().isInstance( min )) )
    {
      // FIXME: this is nonsense! REMOVE
      final Comparable<Comparable< ? >> minComp = (Comparable<Comparable< ? >>) min;
      final Comparable< ? > maxComp = (Comparable< ? >) max;
      if( minComp.compareTo( maxComp ) == 0 )
      {
        // kann leider nicht automatisch angepasst werden; das muss
        // jemand anders abfangen
      }

      if( minComp.compareTo( maxComp ) > 0 )
        return new DataRange<>( max, min );
      else
        return new DataRange<>( min, max );

    }
    /*
     * das wäre dann der ungünstigste Fall: nicht vergleichbar und nicht numerisch TODO: überlegen, ob dieser Fall
     * überhaupt zugelassen werden soll; alternativ sollte eine InvalidRangeIntervalObjectsException
     */
    else
    {
      return new DataRange<>( min, max );
    }
  }

  @SafeVarargs
  @Deprecated
  public static <T> DataRange<T> createFromComparable( final T... items )
  {
    final ComparableComparator comp = new ComparableComparator();

    T min = null;
    T max = null;

    boolean hasMinAndMax = false;
    for( final T item : items )
    {
      if( item != null )
      {
        // erster von Null verschiedener Wert wird als max und min genutzt
        if( hasMinAndMax )
        {
          try
          {
            if( comp.compare( item, min ) < 0 )
            {
              min = item;
            }
            if( comp.compare( item, max ) > 0 )
            {
              max = item;
            }
          }
          catch( final ClassCastException e )
          {
            e.printStackTrace();
          }
        }
        else
        {
          min = item;
          max = item;
          hasMinAndMax = true;
        }
      }
    }

    return new DataRange<>( min, max );
  }
}