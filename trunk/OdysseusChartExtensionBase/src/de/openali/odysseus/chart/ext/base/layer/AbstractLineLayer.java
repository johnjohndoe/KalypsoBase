package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.TextFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author alibu
 */
public abstract class AbstractLineLayer extends AbstractChartLayer implements ITooltipChartLayer
{
  private PointFigure m_pointFigure;

  private PolylineFigure m_polylineFigure;

  private TextFigure m_textFigure;

  public AbstractLineLayer( final ILayerProvider provider, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider );

    getPolylineFigure().setStyle( lineStyle );
    getPointFigure().setStyle( pointStyle );
  }

  public AbstractLineLayer( final ILayerProvider provider, final StyleSet styleSet )
  {
    super( provider );

    final StyleSetVisitor visitor = new StyleSetVisitor();
    final ILineStyle ls = visitor.visit( styleSet, ILineStyle.class, 0 );
    final IPointStyle ps = visitor.visit( styleSet, IPointStyle.class, 0 );
    final ITextStyle ts = visitor.visit( styleSet, ITextStyle.class, 0 );
    if( ls != null )
      getPolylineFigure().setStyle( ls );
    if( ps != null )
      getPointFigure().setStyle( ps );
    if( ts != null )
      getTextFigure().setStyle( ts );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getLegendEntries(org.eclipse.swt.graphics.Point)
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    final ArrayList<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final ILineStyle ls = getPolylineFigure().getStyle();
    if( ls.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, ls.getTitle() )
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

          drawLine( gc, path );
        }

      };
      entries.add( le );

    }

    final IPointStyle ps = getPointFigure().getStyle();
    if( ps.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, ps.getTitle() )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
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

  @Override
  public void dispose( )
  {

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

  protected void drawText( final GC gc, final String text, final Point leftTopPoint )
  {
    final TextFigure tf = getTextFigure();
    tf.setText( text );
    tf.setPoints( new Point[] { leftTopPoint } );
    tf.paint( gc );
  }

  protected void drawLine( final GC gc, final List<Point> path )
  {
    drawLine( gc, path.toArray( new Point[] {} ) );
  }

  protected void drawLine( final GC gc, final Point... paths )
  {
    final PolylineFigure lf = getPolylineFigure();
    lf.setPoints( paths );
    lf.paint( gc );
  }

  protected void drawPoints( final GC gc, final List<Point> path )
  {
    drawPoints( gc, path.toArray( new Point[] {} ) );
  }

  protected void drawPoints( final GC gc, final Point... paths )
  {
    final PointFigure pf = getPointFigure();
    pf.setPoints( paths );
    pf.paint( gc );
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
    return getPolylineFigure().getStyle();
  }

  protected PointFigure getPointFigure( )
  {
    if( m_pointFigure == null )
      m_pointFigure = new PointFigure();
    return m_pointFigure;
  }

  protected IStyle getPointStyle( )
  {
    return getPointFigure().getStyle();

  }

  protected PolylineFigure getPolylineFigure( )
  {
    if( m_polylineFigure == null )
      m_polylineFigure = new PolylineFigure();
    return m_polylineFigure;
  }

  public TextFigure getTextFigure( )
  {
    if( m_textFigure == null )
      m_textFigure = new TextFigure();
    return m_textFigure;
  }

  public ITextStyle getTextStyle( )
  {
    return getTextFigure().getStyle();
  }

  public void setPointFigure( final PointFigure pointFigure )
  {
    m_pointFigure = pointFigure;
  }

  public void setPolylineFigure( final PolylineFigure polylineFigure )
  {
    m_polylineFigure = polylineFigure;
  }

  public void setTextFigure( final TextFigure textFigure )
  {
    m_textFigure = textFigure;
  }
}
