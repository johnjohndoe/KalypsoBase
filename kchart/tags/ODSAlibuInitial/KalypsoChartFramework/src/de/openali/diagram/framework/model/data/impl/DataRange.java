package de.openali.diagram.framework.model.data.impl;

import de.openali.diagram.framework.exception.ZeroSizeDataRangeException;
import de.openali.diagram.framework.model.data.IDataRange;

/**
 * @author alibu
 */
public class DataRange<T extends Comparable> implements IDataRange<T>
{
  private final T m_min;

  private final T m_max;

  public DataRange( T min, T max ) throws ZeroSizeDataRangeException
  {
    if (min.compareTo(max)==0)
		throw new ZeroSizeDataRangeException();
	if (min.compareTo(max)>0)
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

  /**
   * @see de.openali.diagram.framework.layer.IDataRange#getMin()
   */
  public T getMin( )
  {
    return m_min;
  }

  /**
   * @see de.openali.diagram.framework.layer.IDataRange#getMax()
   */
  public T getMax( )
  {
    return m_max;
  }
}
