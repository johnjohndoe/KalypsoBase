package de.openali.diagram.framework.model.data;

/**
 * @author alibu
 * 
 * describes min and max valuse of an (ordered) data set
 * 
 */
public interface IDataRange<T extends Comparable>
{
  public T getMin( );

  public T getMax( );

}
