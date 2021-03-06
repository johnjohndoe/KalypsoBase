package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author alibu visualization of precipitation data as bar chart; The following configuration parameters are needed for
 *         NiederschlagLayer: fixedPoint: start time (e.g. 2006-07-01T00:00:00Z) of any possible bar within the chart;
 *         typically, bars start at 00:00 and end at 23:59, but to get more flexibility, it's possible to make them
 *         "last" more or less than one day (see next parameter) and start them at any desired time / date. barWidth:
 *         width of the chart bars in milliseconds (e.g. 86400000 for one day) The following styled elements are used:
 *         Polygon: used to draw the individual bars
 */
public class GridLayer extends AbstractChartLayer
{

  @Override
  public boolean isVisible( )
  {
    // TODO Auto-generated method stub
    return super.isVisible();
  }

  public enum GridOrientation
  {
    HORIZONTAL,
    VERTICAL,
    BOTH
  }

  private ILegendEntry[] m_legendEntries;

  private final GridOrientation m_orientation;

  public GridLayer( final ILayerProvider provider, final GridOrientation orientation, final ILineStyle gridStyle )
  {
    super( provider, new StyleSet() );

    m_orientation = orientation;
    getStyleSet().addStyle( "line", gridStyle ); //$NON-NLS-1$
  }

  // private final ILineStyle m_gridStyle;

  private ILegendEntry[] createLegendEntries( )
  {

    final ArrayList<ILegendEntry> entries = new ArrayList<>();
    final PolylineFigure pf = getPolylineFigure();
    final ILineStyle ls = pf.getStyle();
    final String title = getTitle();
    if( ls.isVisible() )
    {

      final LegendEntry le = new LegendEntry( this, title )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          final int height = size.x;
          final int width = size.y;

          final ArrayList<Point> points = new ArrayList<>();
          // Linie von links nach rechts
          points.add( new Point( 0, (int)(height * 0.3) ) );
          points.add( new Point( width, (int)(height * 0.3) ) );
          pf.setPoints( points.toArray( new Point[] {} ) );
          pf.paint( gc );
          points.clear();
          points.add( new Point( 0, (int)(height * 0.7) ) );
          points.add( new Point( width, (int)(height * 0.7) ) );
          pf.setPoints( points.toArray( new Point[] {} ) );
          pf.paint( gc );
          points.clear();
          points.add( new Point( (int)(width * 0.3), 0 ) );
          points.add( new Point( (int)(width * 0.3), height ) );
          pf.setPoints( points.toArray( new Point[] {} ) );
          pf.paint( gc );
          points.clear();
          points.add( new Point( (int)(width * 0.7), 0 ) );
          points.add( new Point( (int)(width * 0.7), height ) );
          pf.setPoints( points.toArray( new Point[] {} ) );
          pf.paint( gc );
        }

      };
      entries.add( le );
    }

    return entries.toArray( new ILegendEntry[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    // FIXME: warum wird hier nicht super aufgerufen?
  }

  // FIXME: was soll das? Wird ncith benutzt-....
  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    final PolylineFigure figure = getPolylineFigure();

    final ArrayList<Point> points = new ArrayList<>();
    // Linie von links nach rechts
    if( m_orientation == GridOrientation.BOTH || m_orientation == GridOrientation.HORIZONTAL )
    {
      points.add( new Point( 0, (int)(height * 0.3) ) );
      points.add( new Point( width, (int)(height * 0.3) ) );
      figure.setPoints( points.toArray( new Point[] {} ) );
      figure.paint( gc );
      points.clear();
      points.add( new Point( 0, (int)(height * 0.7) ) );
      points.add( new Point( width, (int)(height * 0.7) ) );
      figure.setPoints( points.toArray( new Point[] {} ) );
      figure.paint( gc );
      points.clear();
    }

    // Linie von oben nach unten
    if( m_orientation == GridOrientation.BOTH || m_orientation == GridOrientation.VERTICAL )
    {
      points.add( new Point( (int)(width * 0.3), 0 ) );
      points.add( new Point( (int)(width * 0.3), height ) );
      figure.setPoints( points.toArray( new Point[] {} ) );
      figure.paint( gc );
      points.clear();
      points.add( new Point( (int)(width * 0.7), 0 ) );
      points.add( new Point( (int)(width * 0.7), height ) );
      figure.setPoints( points.toArray( new Point[] {} ) );
      figure.paint( gc );
      points.clear();
    }
    gc.dispose();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return null;
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

  private PolylineFigure getPolylineFigure( )
  {
    final ILineStyle style = getStyleSet().getStyle( "line", ILineStyle.class ); //$NON-NLS-1$
    final PolylineFigure figure = new PolylineFigure();
    figure.setStyle( style );
    return figure;

  }

// private ILineStyle getGridStyle( )
// {
// if( m_gridStyle == null )
// return StyleUtils.getDefaultLineStyle();
// return m_gridStyle;
// }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return null;
  }

  @SuppressWarnings( { "rawtypes" } )
  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    gc.setLineWidth( 5 );

    final ArrayList<Point> path = new ArrayList<>();

    IAxis hAxis = null;
    IAxis vAxis = null;

    final PolylineFigure figure = getPolylineFigure();

    // Welche ist die horizontale, welche die horizontale Achse?
    if( getDomainAxis().getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      hAxis = getDomainAxis();
      vAxis = getTargetAxis();
    }
    else
    {
      hAxis = getTargetAxis();
      vAxis = getDomainAxis();
    }

    // von links nach rechts zeichnen
    if( m_orientation == GridOrientation.BOTH || m_orientation == GridOrientation.HORIZONTAL )
    {
      final Double[] vTicks = vAxis.getRenderer().getTicks( vAxis, gc );
      final IDataRange<Double> hRange = hAxis.getNumericRange();
      final int xfrom = hAxis.numericToScreen( hRange.getMin() );
      final int xto = hAxis.numericToScreen( hRange.getMax() );
      if( vTicks != null )
        for( final Double vTick : vTicks )
        {
          path.clear();
          path.add( new Point( xfrom, vAxis.numericToScreen( vTick ) ) );
          path.add( new Point( xto, vAxis.numericToScreen( vTick ) ) );
          figure.setPoints( path.toArray( new Point[] {} ) );
          figure.paint( gc );
        }
    }
    // von unten nach oben zeichnen
    if( m_orientation == GridOrientation.BOTH || m_orientation == GridOrientation.VERTICAL )
    {
      final Double[] hTicks = hAxis.getRenderer().getTicks( hAxis, gc );
      final IDataRange<Double> vRange = vAxis.getNumericRange();
      final int yfrom = vAxis.numericToScreen( vRange.getMin() );
      final int yto = vAxis.numericToScreen( vRange.getMax() );
      if( hTicks != null )
        for( final Double hTick : hTicks )
        {
          path.clear();
          path.add( new Point( hAxis.numericToScreen( hTick ), yfrom ) );
          path.add( new Point( hAxis.numericToScreen( hTick ), yto ) );
          figure.setPoints( path.toArray( new Point[] {} ) );
          figure.paint( gc );
        }
    }
  }
}
