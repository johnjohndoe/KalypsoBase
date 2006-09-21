package org.kalypso.swtchart.chart.axis.renderer;

import java.util.Collection;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;

/**
 * @author schlienger
 * @author burtscher
 * 
 * Interface used to render an IAxis object into an IAxisComponent
 * 
 */
public interface IAxisRenderer<T>
{
  /**
   * calculates axis values for which label and tick will be shown
   * @return Collection of tick-values
   */
  public Collection<T> calcTicks( GCWrapper gc, Device dev, IAxis<T> axis );

  /**
   * draws the IAxis-Representation into the given GCWrapper;
   * @param screenArea Rectangle describing the axis size and location within the drawing area  
   */
  public void paint( GCWrapper gc, Device dev, IAxis<T> axis, Rectangle screenArea );

  /**
   * @return width of the rendered axis (which means vertical extension for horizontal axes 
   * or horizontal extension for vertical axes)
   */
  public int getAxisWidth( IAxis<T> axis );
}
