package de.openali.odysseus.chart.framework.model.data.impl;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

public class ComparableDataRange<T> implements IDataRange<T>
{
  private final T[] m_items;

  private T m_max;

  private T m_min;

  private final ComparableComparator m_comp = new ComparableComparator();

  public ComparableDataRange( final T[] items )
  {
    m_items = items;
    findMinMax();
  }

  @Override
  public T getMax( )
  {
    return m_max;
  }

  @Override
  public T getMin( )
  {
    return m_min;
  }

  private void findMinMax( )
  {
    boolean hasMinAndMax = false;
    for( final T item : m_items )
    {
      if( item != null )
      {
        // erster von Null verschiedener Wert wird als max und min genutzt
        if( hasMinAndMax )
        {
          try
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
          catch( ClassCastException e )
          {
            e.printStackTrace();
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
