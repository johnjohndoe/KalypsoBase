package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
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
  /**
   * Layer parameter selecting the mode how the legend symbol is painted. LEGEND_MODE_ constants for possible values.
   */
  private static final String PARAMETER_LEGEND_MODE = "legendMode"; //$NON-NLS-1$

  /** Possible value for for parameter 'legendMode': only draw the line symbol (default) */
  static final String LEGEND_MODE_LINE = "line"; //$NON-NLS-1$

  /** Possible value for for parameter 'legendMode': only draw the point symbol */
  static final String LEGEND_MODE_POINT = "point"; //$NON-NLS-1$

  /** Possible value for for parameter 'legendMode': draw both (line and point) symbols */
  static final String LEGEND_MODE_BOTH = "line+point"; //$NON-NLS-1$

  private ILegendEntry[] m_legendEntries;

  private final String m_legendMode;

  public AbstractLineLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );

    final String legendMode = getLegendMode( provider );
    m_legendMode = legendMode;
  }

  private String getLegendMode( final ILayerProvider provider )
  {
    if( provider == null )
      return LEGEND_MODE_LINE;

    final IParameterContainer parameterContainer = provider.getParameterContainer();
    return parameterContainer.getParameterValue( PARAMETER_LEGEND_MODE, LEGEND_MODE_LINE );
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( ArrayUtils.isEmpty( m_legendEntries ) )
      m_legendEntries = createLegendEntries();

    return m_legendEntries;
  }

  private ILegendEntry[] createLegendEntries( )
  {
    final ILineStyle ls = getLineStyle();
    final IPointStyle ps = getPointStyle();

    // REMARK: backwards compatibility: no legend entry if no visible style is present.
    if( (ls == null || !ls.isVisible()) && (ps == null || !ps.isVisible()) )
      return new ILegendEntry[0];

    final String legendMode = m_legendMode;

    final LegendEntry le = new LegendEntry( this, getTitle() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        /* Line Symbol */
        if( ls != null && ls.isVisible() && legendMode.contains( LEGEND_MODE_LINE ) )
        {
          final int sizeX = size.x;
          final int sizeY = size.y;

          final Point[] path = new Point[6];
          path[0] = new Point( 0, sizeX / 2 );
          path[1] = new Point( sizeX / 5, sizeY / 2 );
          path[2] = new Point( sizeX / 5 * 2, sizeY / 4 );
          path[3] = new Point( sizeX / 5 * 3, sizeY / 4 * 3 );
          path[4] = new Point( sizeX / 5 * 4, sizeY / 2 );
          path[5] = new Point( sizeX, sizeY / 2 );

          drawLine( gc, path );
        }

        if( ps != null && ps.isVisible() && legendMode.contains( LEGEND_MODE_POINT ) )
        {
          final List<Point> path = new ArrayList<>();
          path.add( new Point( size.x / 2, size.y / 2 ) );

          drawPoints( gc, path );
        }
      }
    };

    return new ILegendEntry[] { le };
  }

  protected void drawLine( final GC gc, final Point... paths )
  {
    final PolylineFigure lf = getPolyLineFigure();
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

  protected final ILineStyle getLineStyle( )
  {
    IStyle lineStyle = getStyleSet().getStyle( "line_" + getIdentifier() ); //$NON-NLS-1$
    if( lineStyle == null )
      lineStyle = getStyleSet().getStyle( "line" );// default style in older .kod's //$NON-NLS-1$
    if( lineStyle == null )
      return getStyle( ILineStyle.class );

    return (ILineStyle)lineStyle;
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
    IStyle pointStyle = getStyleSet().getStyle( "point_" + getIdentifier() ); //$NON-NLS-1$
    if( pointStyle == null )
      pointStyle = getStyleSet().getStyle( "point" );// default style in older .kod's //$NON-NLS-1$
    if( pointStyle == null )
      return getStyle( IPointStyle.class );
    return (IPointStyle)pointStyle;
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
