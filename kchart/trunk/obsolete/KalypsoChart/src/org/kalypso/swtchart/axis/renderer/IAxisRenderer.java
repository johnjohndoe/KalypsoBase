package org.kalypso.swtchart.axis.renderer;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.axis.IAxis;

/**
 * @author schlienger
 */
public interface IAxisRenderer<T>
{
  public void paint( GCWrapper gc, Device dev, IAxis<T> axis, Rectangle screenArea );
}
