package org.kalypso.chart.framework.model.data;

/**
 * @author alibu describes min and max valuse of an (ordered) data set
 */
public interface IDataRange<T>
{
  public T getMin( );

  public T getMax( );

}
