package de.openali.odysseus.chart.framework.model.data.impl;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

public class ComparableDataRange<T> implements IDataRange<T>
{
  private T m_max;

  private T m_min;

  private final ComparableComparator m_comp = new ComparableComparator();

  public ComparableDataRange( final T[] items )
  {
    // FIXME: ugly, use an static constructor instead
    findMinMax( items );
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

  private void findMinMax( final T[] items )
  {
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
            if( m_comp.compare( item, m_min ) < 0 )
            {
              m_min = item;
            }
            if( m_comp.compare( item, m_max ) > 0 )
            {
              m_max = item;
            }
          }
          catch( final ClassCastException e )
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
