package de.openali.odysseus.chart.framework.model.mapper.renderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author alibu Interface used to render an IAxis object into an IAxisComponent
 */
public interface IAxisRenderer // extends IMapperEventListener
{
  /**
   * calculates axis values for which label and tick will be shown
   * 
   * @return Collection of tick-values
   */
  Number[] getTicks( IAxis axis, GC gc );

  /**
   * draws the IAxis-Representation into the given GC;
   * 
   * @param screenArea
   *          Rectangle describing the axis size and location within the drawing area
   */
  void paint( GC gc, IAxis axis, Rectangle screenArea );

  /**
   * @return width of the rendered axis (which means vertical extension for horizontal axes or horizontal extension for
   *         vertical axes)
   */
  int getAxisWidth( IAxis axis );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  Object getData( String identifier );

  /**
   * returns unique identifier
   */
  String getId( );

  // public void invalidateTicks( IAxis axis );

  void dispose( );

  String BORDER_SIZE = "de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer_border_size";

}
