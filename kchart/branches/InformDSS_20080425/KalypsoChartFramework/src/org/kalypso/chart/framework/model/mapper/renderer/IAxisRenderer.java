package org.kalypso.chart.framework.model.mapper.renderer;

import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu Interface used to render an IAxis object into an IAxisComponent
 */
public interface IAxisRenderer<T_logical>
{
  /**
   * calculates axis values for which label and tick will be shown
   * 
   * @return Collection of tick-values
   */
  public T_logical[] getTicks( IAxis<T_logical> axis );

  /**
   * draws the IAxis-Representation into the given GCWrapper;
   * 
   * @param screenArea
   *            Rectangle describing the axis size and location within the drawing area
   */
  public void paint( GCWrapper gc, IAxis<T_logical> axis, Rectangle screenArea );

  /**
   * @return width of the rendered axis (which means vertical extension for horizontal axes or horizontal extension for
   *         vertical axes)
   */
  public int getAxisWidth( IAxis<T_logical> axis );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  public Object getData( String identifier );

  /**
   * returns unique identifier
   */
  public String getId( );

  public void invalidateTicks( IAxis axis );

  public void dispose( );

}
