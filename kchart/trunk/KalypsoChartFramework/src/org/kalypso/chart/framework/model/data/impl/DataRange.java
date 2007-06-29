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
    if (min instanceof Number && max instanceof Number)
    {
      Double minNum= ( ( Number ) min).doubleValue();
      Double maxNum= ( ( Number ) max).doubleValue();
      
      if (minNum.compareTo(maxNum)==0)
          throw new ZeroSizeDataRangeException();
      if (minNum.compareTo(maxNum)>0)
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
    else 
      if (min instanceof Comparable && max instanceof Comparable
        && ( min.getClass().isInstance( max ) || max.getClass().isInstance( min ))
    )
	{
		Comparable<Comparable> minComp=(Comparable<Comparable>) min;
		Comparable maxComp=(Comparable) max;
		System.out.println("comparing: "+minComp.toString()+" : "+maxComp.toString()+ "("+minComp.getClass()+"|"+maxComp.getClass()+")");
		
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
    /* 
     * das wäre dann der ungünstigste Fall: nicht Vergleichbar und nicht numerisch
     * TODO: überlegen, ob dieser Fall überhaupt zugelassen werden soll; alternativ
     * sollte eine InvalidRangeIntervalObjectsException
     */ 
	else
	{
	  if (min == max)
	    throw new ZeroSizeDataRangeException();
	  m_min=min;
	  m_max=max;
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
