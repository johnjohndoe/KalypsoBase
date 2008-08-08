package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public interface IMarker
{

  /**
   * assumes that the style is already set
   */
  public void paint( GC gc, Point p, int width, int height, boolean drawForeground, boolean drawBackground );

  public void dispose( );

  public IMarker copy( );
}
