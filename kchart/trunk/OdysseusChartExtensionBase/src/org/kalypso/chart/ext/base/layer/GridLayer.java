package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.impl.model.figure.PolylineFigure;
import de.openali.odysseus.chart.framework.impl.model.legend.LegendItem;
import de.openali.odysseus.chart.framework.impl.model.style.LineStyle;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.legend.ILegendItem;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

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

  public enum GridOrientation
  {
    HORIZONTAL,
    VERTICAL,
    BOTH
  }

  private final GridOrientation m_orientation;

  public GridLayer( GridOrientation orientation )
  {
    m_orientation = orientation;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
  public void paint( final GC gc )
  {

    gc.setLineWidth( 5 );

    final ArrayList<Point> path = new ArrayList<Point>();

    IAxis hAxis = null;
    IAxis vAxis = null;

    PolylineFigure figure = getPolylineFigure();

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
      final Number[] vTicks = (Number[]) vAxis.getRegistry().getRenderer( vAxis ).getTicks( vAxis );
      final IDataRange<Number> hRange = hAxis.getNumericRange();
      final int xfrom = hAxis.numericToScreen( hRange.getMin() );
      final int xto = hAxis.numericToScreen( hRange.getMax() );
      if( vTicks != null )
      {
        for( final Number vTick : vTicks )
        {
          path.clear();
          path.add( new Point( xfrom, vAxis.numericToScreen( vTick ) ) );
          path.add( new Point( xto, vAxis.numericToScreen( vTick ) ) );
          figure.setPoints( path.toArray( new Point[] {} ) );
          figure.paint( gc );
        }
      }
    }
    // von unten nach oben zeichnen
    if( m_orientation == GridOrientation.BOTH || m_orientation == GridOrientation.VERTICAL )
    {
      final Number[] hTicks = (Number[]) hAxis.getRegistry().getRenderer( hAxis ).getTicks( hAxis );
      final IDataRange<Number> vRange = vAxis.getNumericRange();
      final int yfrom = vAxis.numericToScreen( vRange.getMin() );
      final int yto = vAxis.numericToScreen( vRange.getMax() );
      if( hTicks != null )
      {
        for( final Number hTick : hTicks )
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

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem()
   */
  @Override
  public ILegendItem getLegendItem( )
  {
    ILegendItem l = null;
    final Image img = new Image( Display.getCurrent(), 20, 20 );
    drawIcon( img );
    final ImageData id = img.getImageData();
    img.dispose();
    l = new LegendItem( null, getId(), id );
    return l;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( )
  {
    return null;
  }

  public void drawIcon( Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    PolylineFigure figure = getPolylineFigure();

    final ArrayList<Point> points = new ArrayList<Point>();
    // Linie von links nach rechts
    points.add( new Point( 0, (int) (height * 0.3) ) );
    points.add( new Point( width, (int) (height * 0.3) ) );
    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( gc );
    points.clear();
    points.add( new Point( 0, (int) (height * 0.7) ) );
    points.add( new Point( width, (int) (height * 0.7) ) );
    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( gc );
    points.clear();
    points.add( new Point( (int) (width * 0.3), 0 ) );
    points.add( new Point( (int) (width * 0.3), height ) );
    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( gc );
    points.clear();
    points.add( new Point( (int) (width * 0.7), 0 ) );
    points.add( new Point( (int) (width * 0.7), height ) );
    figure.setPoints( points.toArray( new Point[] {} ) );
    figure.paint( gc );
    points.clear();

    gc.dispose();
  }

  private PolylineFigure getPolylineFigure( )
  {
    ILineStyle style = getStyles().getStyle( "line", LineStyle.class );
    PolylineFigure figure = new PolylineFigure();
    figure.setStyle( style );
    return figure;

  }
}
