package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.util.FigureUtilities;

public class PolygonMarker extends AbstractMarker
{
  private final Point[] m_points;

  public PolygonMarker( final Point[] points )
  {
    m_points = points;
  }

  @Override
  public void paint( final GC gc, final Point p, final int width, final int height, final boolean drawForeground, final boolean drawBackground )
  {
    final Point[] resized = FigureUtilities.resizeInOrigin( m_points, width, height );
    final Point[] inverted = FigureUtilities.invertY( resized );
    final Point[] translated = FigureUtilities.translateTo( inverted, p );
    final int[] intArray = FigureUtilities.pointArrayToIntArray( translated );
    if( drawBackground )
      gc.fillPolygon( intArray );
    if( drawForeground )
      gc.drawPolygon( intArray );
  }

  @Override
  public PolygonMarker copy( )
  {
    return new PolygonMarker( m_points );
  }

}
