package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;

public interface IMarker
{
  IMarker copy( );

  void dispose( );

  /**
   * assumes that the style is already set
   */
  void paint( GC gc, int x, int y, int width, int height, boolean drawForeground, boolean drawBackground );
}
