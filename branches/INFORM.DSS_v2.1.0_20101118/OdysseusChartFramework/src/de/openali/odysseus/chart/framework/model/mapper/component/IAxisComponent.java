package de.openali.odysseus.chart.framework.model.mapper.component;

import org.eclipse.swt.graphics.Point;

/**
 * Widget to display chart axes; it also helps to calculate screen values from normalized values
 * 
 * @author burtscher
 */
public interface IAxisComponent
{

  /**
   * disposes the widget
   */
  public void dispose( );

  /**
   * sets the components' visibilty
   */
  public void setVisible( boolean b );

  public Point computeSize( final int wHint, final int hHint, final boolean changed );

}
