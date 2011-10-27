package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;

/**
 * @author alibu
 */
public abstract class AbstractLineLayer extends AbstractChartLayer implements ITooltipChartLayer
{

  private IStyleSet m_styleSet;

  public AbstractLineLayer( final ILayerProvider provider )
  {
    super( provider );
    m_styleSet = new StyleSet();

  }

  public AbstractLineLayer( final ILayerProvider provider, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider );

    m_styleSet = new StyleSet();
    m_styleSet.addStyle( "line", lineStyle );
    m_styleSet.addStyle( "point", pointStyle );
  }

  public AbstractLineLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider );
    m_styleSet = styleSet;

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    final ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final ILineStyle ls = getLineStyle();
    final IPointStyle ps = getPointStyle();

    if( ls == null || ps == null || ls.isVisible() || ps.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, getTitle() )
      {

        @Override
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

  @Override
  public void dispose( )
  {

  }

  public void drawIcon( final GC gc, final Point size )
  {
    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, size.y / 2 ) );
    path.add( new Point( size.x / 5, size.y / 2 ) );
    path.add( new Point( size.x / 5 * 2, size.y / 4 ) );
    path.add( new Point( size.x / 5 * 3, size.y / 4 * 3 ) );
    path.add( new Point( size.x / 5 * 4, size.y / 2 ) );
    path.add( new Point( size.x, size.y / 2 ) );
    paint( gc, path.toArray( new Point[] {} ) );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );
    drawIcon( gc, new Point( width, height ) );
    gc.dispose();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    return null;
  }

  protected ILineStyle getLineStyle( )
  {
    if( getStyleSet().getStyle( "line_" + getIdentifier() ) != null )
      return getStyleSet().getStyle( "line_" + getIdentifier(), ILineStyle.class );
    return getStyleSet().getStyle( "line", ILineStyle.class );// default style in older .kod's
  }

  protected IPointStyle getPointStyle( )
  {
    if( getStyleSet().getStyle( "point_" + getIdentifier() ) != null )
      return getStyleSet().getStyle( "point_" + getIdentifier(), IPointStyle.class );
    return getStyleSet().getStyle( "point", IPointStyle.class );// default style in older .kod's

  }

  protected IStyleSet getStyleSet( )
  {
    return m_styleSet;
  }

  protected void paint( final GC gc, final Point... points )
  {
    final ILineStyle ls = getLineStyle();
    final PolylineFigure lf = new PolylineFigure();
    lf.setStyle( ls );
    lf.setPoints( points );
    lf.paint( gc );

    final IPointStyle ps = getPointStyle();
    final PointFigure pf = new PointFigure();
    pf.setStyle( ps );
    pf.setPoints( points );
    pf.paint( gc );

  }

  public void setStyleSet( IStyleSet styleSet )
  {
    m_styleSet = styleSet;
  }
}
