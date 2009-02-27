package de.openali.diagram.framework.model.mapper.component;


/**
 * @author burtscher
 * 
 * Widget to display chart axes; it also helps 
 * to calculate screen values from normalized values 
 * 
 */
public interface IAxisComponent<T>
{
  /**
   *  converts a double value into the corresponding screen value  
   */
  public int normalizedToScreen( double d );

  /**
   * converts a screen value into a normalized value
   */
  public double screenToNormalized( int value );

  /**
   * disposes the widget
   */
  public void dispose( );

  /**
   * sets the components' visibilty
   */
  public void setVisible( boolean b );


}
