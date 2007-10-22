package org.kalypso.chart.framework.model.data.impl;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.framework.model.data.IDataRange;

public class ComparableDataRange<T> implements IDataRange<T>
{

  private final T[] m_items;

  private T m_max;

  private T m_min;

  private final ComparableComparator m_comp = new ComparableComparator();

  public ComparableDataRange( T[] items )
  {
    m_items = items;
    findMinMax();
  }

  public T getMax( )
  {
    return m_max;
  }

  public T getMin( )
  {
    return m_min;
  }

  private void findMinMax( )
  {
    boolean hasMinAndMax = false;
    for( int i = 0; i < m_items.length; i++ )
    {
      final T item = m_items[i];
      if( item != null )
      {
        // erster von Null verschiedener Wert wird als max und min genutzt
        if( hasMinAndMax )
        {
          if( m_comp.compare( item, m_min ) < 0 )
          {
            m_min = item;
          }
          if( m_comp.compare( item, m_max ) > 0 )
          {
            m_max = item;
          }
        }
        else
        {
          m_min = item;
          m_max = item;
          hasMinAndMax = true;
        }
      }
    }
  }

}
