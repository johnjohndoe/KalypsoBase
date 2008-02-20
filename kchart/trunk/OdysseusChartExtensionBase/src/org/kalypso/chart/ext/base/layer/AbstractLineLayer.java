package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.impl.model.figure.PointFigure;
import de.openali.odysseus.chart.framework.impl.model.figure.PolylineFigure;
import de.openali.odysseus.chart.framework.impl.model.style.LineStyle;
import de.openali.odysseus.chart.framework.impl.model.style.PointStyle;

/**
 * @author alibu
 */
public abstract class AbstractLineLayer extends AbstractChartLayer
{

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 5, height / 2 ) );
    path.add( new Point( width / 5 * 2, height / 4 ) );
    path.add( new Point( width / 5 * 3, height / 4 * 3 ) );
    path.add( new Point( width / 5 * 4, height / 2 ) );
    path.add( new Point( width, height / 2 ) );
    drawLine( gc, path );
    drawPoints( gc, path );

    gc.dispose();
  }

  protected void drawLine( GC gc, List<Point> path )
  {
    PolylineFigure lf = getPolylineFigure();
    lf.setPoints( path.toArray( new Point[] {} ) );
    lf.paint( gc );
  }

  protected void drawPoints( GC gc, List<Point> path )
  {
    PointFigure pf = getPointFigure();
    for( Point point : path )
    {
      pf.setCenterPosition( point );
      pf.paint( gc );
    }
  }

  private PolylineFigure getPolylineFigure( )
  {
    LineStyle ls = getStyles().getStyle( "line", LineStyle.class );
    PolylineFigure lf = new PolylineFigure();
    lf.setStyle( ls );
    return lf;
  }

  private PointFigure getPointFigure( )
  {
    PointStyle ps = getStyles().getStyle( "point", PointStyle.class );
    PointFigure pf = new PointFigure();
    pf.setStyle( ps );
    return pf;
  }

}
