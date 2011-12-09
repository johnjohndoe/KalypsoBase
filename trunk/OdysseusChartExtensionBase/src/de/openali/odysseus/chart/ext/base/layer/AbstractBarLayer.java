package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;

/**
 * @author alibu
 */
public abstract class AbstractBarLayer extends AbstractChartLayer
{

  private PolygonFigure m_polygonFigure;

  private ILegendEntry[] m_legendEntries;

  public AbstractBarLayer( final ILayerProvider provider, final IAreaStyle areaStyle )
  {
    super( provider, new StyleSet() );
    getStyleSet().addStyle( "area", areaStyle );
  }

  public AbstractBarLayer( ILayerProvider provider, IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( final Image img )
  {
    final PolygonFigure pf = getPolygonFigure();

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
    if( m_polygonFigure == null )
    {
      final IAreaStyle as = getStyleSet().getStyle( "area", IAreaStyle.class );
      m_polygonFigure = new PolygonFigure();
      m_polygonFigure.setStyle( as );
    }
    return m_polygonFigure;
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
    final List<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final PolygonFigure pf = getPolygonFigure();
    if( pf.getStyle().isVisible() )
    {

      final LegendEntry entry = new LegendEntry( this, getTitle() )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          final int height = size.x;
          final int width = size.y;
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
        }
      };

      entries.add( entry );
    }
    return entries.toArray( new ILegendEntry[] {} );
  }

  @Override
  public void dispose( )
  {
  }
}
