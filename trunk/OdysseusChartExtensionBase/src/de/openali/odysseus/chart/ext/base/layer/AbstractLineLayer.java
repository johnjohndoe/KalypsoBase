package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author alibu
 */
public abstract class AbstractLineLayer extends AbstractChartLayer
{
  private ILegendEntry[] m_legendEntries;

  public AbstractLineLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  /**
   * @see de.openali.odysseus.chart.factory.layer.AbstractChartLayer#getLegendEntries()
   */
  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {

    if( ArrayUtils.isEmpty( m_legendEntries ) )
    {
      m_legendEntries = createLegendEntries();
    }
    return m_legendEntries;
  }

  private ILegendEntry[] createLegendEntries( )
  {
    final ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final ILineStyle ls = getLineStyle();
    final IPointStyle ps = getPointStyle();

    if( ls == null || ps == null || ls.isVisible() || ps.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, getTitle() )
      {

        @Override
        @SuppressWarnings("synthetic-access")
        public void paintSymbol( final GC gc, final Point size )
        {
          final int sizeX = size.x;
          final int sizeY = size.y;

          final ArrayList<Point> path = new ArrayList<Point>();
          path.add( new Point( 0, sizeX / 2 ) );
          path.add( new Point( sizeX / 5, sizeY / 2 ) );
          path.add( new Point( sizeX / 5 * 2, sizeY / 4 ) );
          path.add( new Point( sizeX / 5 * 3, sizeY / 4 * 3 ) );
          path.add( new Point( sizeX / 5 * 4, sizeY / 2 ) );
          path.add( new Point( sizeX, sizeY / 2 ) );

          drawIcon( gc, size );
        }

      };
      entries.add( le );

    }

    return entries.toArray( new ILegendEntry[] {} );
  }

  private void drawIcon( final GC gc, final Point size )
  {
    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, size.y / 2 ) );
    path.add( new Point( size.x / 5, size.y / 2 ) );
    path.add( new Point( size.x / 5 * 2, size.y / 4 ) );
    path.add( new Point( size.x / 5 * 3, size.y / 4 * 3 ) );
    path.add( new Point( size.x / 5 * 4, size.y / 2 ) );
    path.add( new Point( size.x, size.y / 2 ) );
    final ILineStyle ls = getLineStyle();
    final PolylineFigure lf = new PolylineFigure();
    lf.setStyle( ls );
    lf.setPoints( path.toArray( new Point[] {} ) );
    lf.paint( gc );
  }

  protected final ILineStyle getLineStyle( )
  {
    IStyle lineStyle = getStyleSet().getStyle( "line_" + getIdentifier() );
    if( lineStyle == null )
      lineStyle = getStyleSet().getStyle( "line" );// default style in older .kod's
    if( lineStyle == null )
      return getStyle( ILineStyle.class );
    return (ILineStyle) lineStyle;
  }

  protected PolylineFigure getPolyLineFigure( )
  {
    final ILineStyle ls = getLineStyle();
    final PolylineFigure lf = new PolylineFigure();
    lf.setStyle( ls );
    return lf;
  }

  protected PointFigure getPointFigure( )
  {
    final IPointStyle ps = getPointStyle();
    final PointFigure pf = new PointFigure();
    pf.setStyle( ps );
    return pf;
  }

  protected final IPointStyle getPointStyle( )
  {
    IStyle pointStyle = getStyleSet().getStyle( "point_" + getIdentifier() );
    if( pointStyle == null )
      pointStyle = getStyleSet().getStyle( "point" );// default style in older .kod's
    if( pointStyle == null )
      return getStyle( IPointStyle.class );
    return (IPointStyle) pointStyle;
  }

  protected final void paint( final GC gc, final Point... points )
  {
    final PolylineFigure lf = getPolyLineFigure();
    lf.setPoints( points );
    lf.paint( gc );
    final PointFigure pf = getPointFigure();
    pf.setPoints( points );
    pf.paint( gc );
  }

}
