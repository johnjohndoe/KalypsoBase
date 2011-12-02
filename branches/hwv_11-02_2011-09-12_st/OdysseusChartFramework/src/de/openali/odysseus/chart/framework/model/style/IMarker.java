package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public interface IMarker
{
  IMarker copy( );

  void dispose( );

  /**
   * assumes that the style is already set
   */
  void paint( GC gc, Point p, int width, int height, boolean drawForeground, boolean drawBackground );
}
