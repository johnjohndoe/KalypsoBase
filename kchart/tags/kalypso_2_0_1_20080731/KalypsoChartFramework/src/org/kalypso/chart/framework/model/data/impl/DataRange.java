package org.kalypso.chart.framework.model.data.impl;

import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;

/**
 * @author alibu
 */
public class DataRange<T> implements IDataRange<T>
{
  private T m_min;

  private T m_max;

  @SuppressWarnings( { "cast", "unchecked" })
  public DataRange( final T min, final T max ) throws ZeroSizeDataRangeException
  {
    if( min instanceof Number && max instanceof Number )
    {
      final Double minNum = ((Number) min).doubleValue();
      final Double maxNum = ((Number) max).doubleValue();

      // Beide gleich => dataRange automatisch so anpassen, dass der Wert in der Intervallmitte liegt
      if( minNum.compareTo( maxNum ) == 0 )
      {
        final double doubleValue = minNum.doubleValue();
        // falls != 0 werden einfach 10% addiert oder subtrahiert
        if( doubleValue != 0 )
        {
          m_min = (T) ((Number) new Double( doubleValue - doubleValue * 0.1 ));
          m_max = (T) ((Number) new Double( doubleValue + doubleValue * 0.1 ));
        }
        // falls == 0 wird 1 addiert oder subtrahiert
        else
        {
          m_min = (T) ((Number) new Double( doubleValue - 1 ));
          m_max = (T) ((Number) new Double( doubleValue + 1 ));
        }

      }
      if( minNum.compareTo( maxNum ) > 0 )
      {
        m_max = min;
        m_min = max;
      }
      else
      {
        m_min = min;
        m_max = max;
      }
    }
    else if( min instanceof Comparable && max instanceof Comparable && (min.getClass().isInstance( max ) || max.getClass().isInstance( min )) )
    {
      final Comparable<Comparable> minComp = (Comparable<Comparable>) min;
      final Comparable maxComp = (Comparable) max;
      Logger.logInfo( Logger.TOPIC_LOG_DATA, "comparing: " + minComp.toString() + " : " + maxComp.toString() + "(" + minComp.getClass() + "|" + maxComp.getClass() + ")" );
      if( minComp.compareTo( maxComp ) == 0 )
      {
        // kann leider nicht automatisch angepasst werden => Exception
        throw new ZeroSizeDataRangeException();
      }
      if( minComp.compareTo( maxComp ) > 0 )
      {
        m_max = min;
        m_min = max;
      }
      else
      {
        m_min = min;
        m_max = max;
      }
    }
    /*
     * das wäre dann der ungünstigste Fall: nicht vergleichbar und nicht numerisch TODO: überlegen, ob dieser Fall
     * überhaupt zugelassen werden soll; alternativ sollte eine InvalidRangeIntervalObjectsException
     */
    else
    {
      if( min == max )
        throw new ZeroSizeDataRangeException();
      m_min = min;
      m_max = max;
    }
  }

  /**
   * @see org.kalypso.chart.framework.layer.IDataRange#getMin()
   */
  public T getMin( )
  {
    return m_min;
  }

  /**
   * @see org.kalypso.chart.framework.layer.IDataRange#getMax()
   */
  public T getMax( )
  {
    return m_max;
  }
}
