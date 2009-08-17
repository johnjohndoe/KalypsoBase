package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.util.FigureUtilities;

public class PolygonMarker extends AbstractMarker
{
  private final Point[] m_points;

  public PolygonMarker( Point[] points )
  {
    m_points = points;
  }

  public void paint( GC gc, Point p, int width, int height, boolean drawForeground, boolean drawBackground )
  {
    Point[] resized = FigureUtilities.resizeInOrigin( m_points, width, height );
    Point[] inverted = FigureUtilities.invertY( resized );
    Point[] translated = FigureUtilities.translateTo( inverted, p );
    int[] intArray = FigureUtilities.pointArrayToIntArray( translated );
    if( drawBackground )
      gc.fillPolygon( intArray );
    if( drawForeground )
      gc.drawPolygon( intArray );
  }

  public PolygonMarker copy( )
  {
    return new PolygonMarker( m_points );
  }

}
