package de.belger.swtchart.axis;

import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author gernot
 * 
 */
public interface IAxisRenderer
{
  public Rectangle reduceScreenSize( final GCWrapper gc, final AxisRange axis, final Rectangle screen );

  public void paint( final GCWrapper gc, final AxisRange axis, final Rectangle screen );
}
