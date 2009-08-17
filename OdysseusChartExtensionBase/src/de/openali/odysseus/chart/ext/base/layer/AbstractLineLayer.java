package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * @author alibu
 */
public abstract class AbstractLineLayer extends AbstractChartLayer
{

  private final ILineStyle m_lineStyle;

  private final IPointStyle m_pointStyle;

  private PointFigure m_pointFigure;

  private PolylineFigure m_polylineFigure;

  public AbstractLineLayer( ILineStyle lineStyle, IPointStyle pointStyle )
  {
    m_lineStyle = lineStyle;
    m_pointStyle = pointStyle;
  }

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
    pf.setPoints( path.toArray( new Point[] {} ) );
    pf.paint( gc );
  }

  protected PolylineFigure getPolylineFigure( )
  {
    if( m_polylineFigure == null )
    {
      ILineStyle ls = getLineStyle();
      m_polylineFigure = new PolylineFigure();
      m_polylineFigure.setStyle( ls );
    }
    return m_polylineFigure;
  }

  protected ILineStyle getLineStyle( )
  {
    if( m_lineStyle == null )
      return StyleUtils.getDefaultLineStyle();
    return m_lineStyle;
  }

  protected PointFigure getPointFigure( )
  {
    if( m_pointFigure == null )
    {
      IPointStyle ps = getPointStyle();
      m_pointFigure = new PointFigure();
      m_pointFigure.setStyle( ps );
    }
    return m_pointFigure;
  }

  protected IPointStyle getPointStyle( )
  {
    if( m_pointStyle == null )
      return StyleUtils.getDefaultPointStyle();
    return m_pointStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {

    ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    ILineStyle ls = getPolylineFigure().getStyle();
    if( ls.isVisible() )
    {

      LegendEntry le = new LegendEntry( this, ls.getTitle() )
      {

        @Override
        public void paintSymbol( GC gc, Point size )
        {
          int sizeX = size.x;
          int sizeY = size.y;

          final ArrayList<Point> path = new ArrayList<Point>();
          path.add( new Point( 0, sizeX / 2 ) );
          path.add( new Point( sizeX / 5, sizeY / 2 ) );
          path.add( new Point( sizeX / 5 * 2, sizeY / 4 ) );
          path.add( new Point( sizeX / 5 * 3, sizeY / 4 * 3 ) );
          path.add( new Point( sizeX / 5 * 4, sizeY / 2 ) );
          path.add( new Point( sizeX, sizeY / 2 ) );

          drawLine( gc, path );
        }

      };
      entries.add( le );

    }

    IPointStyle ps = getPointFigure().getStyle();
    if( ps.isVisible() )
    {

      LegendEntry le = new LegendEntry( this, ps.getTitle() )
      {
        @Override
        public void paintSymbol( GC gc, Point size )
        {
          final ArrayList<Point> path = new ArrayList<Point>();

          path.add( new Point( size.x / 2, size.y / 2 ) );
          drawPoints( gc, path );
        }

      };
      entries.add( le );
    }
    return entries.toArray( new ILegendEntry[] {} );
  }

  public void dispose( )
  {

  }
}
