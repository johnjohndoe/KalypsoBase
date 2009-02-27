package org.kalypso.swtchart.chart.axis.renderer;

import java.util.Collection;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.axis.IAxis;

/**
 * @author schlienger
 */
public interface IAxisRenderer<T>
{
  public Collection<T> calcTicks( GCWrapper gc, Device dev, IAxis<T> axis );

  public void paint( GCWrapper gc, Device dev, IAxis<T> axis, Rectangle screenArea );

  public int getAxisWidth( IAxis<T> axis );
}
