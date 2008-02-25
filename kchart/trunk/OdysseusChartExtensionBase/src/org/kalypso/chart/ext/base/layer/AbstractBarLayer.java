package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.impl.model.figure.PolygonFigure;
import de.openali.odysseus.chart.framework.impl.model.style.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public abstract class AbstractBarLayer extends AbstractChartLayer
{

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img )
  {

    PolygonFigure pf = getPolygonFigure();

    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    ArrayList<Point> path = new ArrayList<Point>();
    path.add( new Point( 0, height ) );
    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 2, height / 2 ) );
    path.add( new Point( width / 2, height ) );
    pf.setPoints( path.toArray( new Point[] {} ) );
    pf.paint( gc );

    path = new ArrayList<Point>();
    path.add( new Point( width / 2, height ) );
    path.add( new Point( width / 2, 0 ) );
    path.add( new Point( width, 0 ) );
    path.add( new Point( width, height ) );
    pf.setPoints( path.toArray( new Point[] {} ) );
    pf.paint( gc );

    gc.dispose();

  }

  protected PolygonFigure getPolygonFigure( )
  {
    IAreaStyle as = getStyles().getStyle( "area", AreaStyle.class );
    PolygonFigure pf = new PolygonFigure();
    pf.setStyle( as );
    return pf;
  }
}
