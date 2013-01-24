package de.openali.odysseus.chart.framework.model.data;

/**
 * @author alibu describes min and max valuse of an (ordered) data set
 */
public interface IDataRange<T>
{
  T getMin( );

  T getMax( );

}
