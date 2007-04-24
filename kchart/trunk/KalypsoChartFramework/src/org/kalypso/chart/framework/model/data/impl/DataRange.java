package org.kalypso.chart.framework.model.data.impl;

import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.data.IDataRange;

/**
 * @author alibu
 */
public class DataRange<T> implements IDataRange<T>
{
  private T m_min;

  private T m_max;

  public DataRange( T min, T max ) throws ZeroSizeDataRangeException
  {
	if (min instanceof Comparable && max instanceof Comparable)
	{
		Comparable minComp=(Comparable) min;
		Comparable maxComp=(Comparable) max;
	    if (minComp.compareTo(maxComp)==0)
			throw new ZeroSizeDataRangeException();
		if (minComp.compareTo(maxComp)>0)
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
