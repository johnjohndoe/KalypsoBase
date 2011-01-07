package de.openali.odysseus.chart.framework.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.TextFigure;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.ImageMarker;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.OvalMarker;
import de.openali.odysseus.chart.framework.model.style.impl.PolygonMarker;
import de.openali.odysseus.chart.framework.util.Sleak;
import de.openali.odysseus.chart.framework.util.StyleUtils;

/**
 * TODO: auslagern in externes Test-Plugin
 * 
 * @author burtscher1
 */
public class StyleTest implements PaintListener
{

  private final Shell m_shell;

  public StyleTest( )
  {
    final DeviceData data = new DeviceData();

    data.tracking = true;

    final Display d = new Display( data );

    final Sleak sleak = new Sleak();

    sleak.open();

    m_shell = new Shell( d );
    m_shell.setSize( 500, 400 );
    m_shell.setLayout( new FillLayout() );

    final Canvas c = new Canvas( m_shell, SWT.FILL );
    c.addPaintListener( this );

    m_shell.open();

    while( !m_shell.isDisposed() )
      d.readAndDispatch();
  }

  public static void main( final String... args )
  {
    new StyleTest();

  }

  @Override
  public void paintControl( final PaintEvent e )
  {
    final GC gc = e.gc;
    gc.setAntialias( SWT.ON );

    final int pointSize = 20;

    // Polygon
    final Point[] points0 = createPolygonPoints( pointSize );
    final PolygonFigure pf = new PolygonFigure();
    final IAreaStyle as = StyleUtils.getDefaultAreaStyle();
    as.setFill( new ColorFill( new RGB( 255, 0, 0 ) ) );
    pf.setStyle( as );
    pf.setPoints( points0 );
    pf.paint( gc );

    // bars
    final Point[][] bars = createBars( 20 );
    final PolygonFigure bf = new PolygonFigure();
    final IAreaStyle asBar = StyleUtils.getDefaultAreaStyle();
    asBar.setFill( new ColorFill( new RGB( 255, 0, 0 ) ) );
    asBar.setAlpha( 100 );
    bf.setStyle( asBar );
    for( final Point[] bar : bars )
    {
      bf.setPoints( bar );
      bf.paint( gc );
    }

    final Point[] points1 = createPoints( pointSize );

    // Polyline
    final PolylineFigure lf = new PolylineFigure();
    ILineStyle ls = new LineStyle( 3, new RGB( 0, 255, 0 ), 100, 0, null, LINEJOIN.MITER, LINECAP.ROUND, 5, true );
    lf.setStyle( ls );
    lf.setPoints( points1 );

    final IMarker m1 = new OvalMarker();
    // TODO: hier sollte natürlich eine URL reingefüllt werden
    final IMarker m2 = new ImageMarker( null );
    final Point[] markerPoints = getCrossMarker();

    final IMarker m3 = new PolygonMarker( markerPoints );

    // Point
    final IPointStyle ps = StyleUtils.getDefaultPointStyle();
    final PointFigure pf2 = new PointFigure();

    // Text
    final ITextStyle ts = StyleUtils.getDefaultTextStyle();
    ts.setFamily( "Ani" );
    final TextFigure tf = new TextFigure();

    for( int i = 0; i < points1.length; i++ )
    {
      final Point p = points1[i];
      ps.setWidth( i * 2 );
      ps.setHeight( i * 3 );
      final int alpha = (255 / points1.length) * (i + 1);
      ps.setAlpha( alpha );
      final int mod = i % 3;
      if( mod == 0 )
        ps.setMarker( m1 );
      else if( mod == 1 )
        ps.setMarker( m2 );
      else
        ps.setMarker( m3 );

      pf2.setStyle( ps );
      pf2.setPoints( new Point[] { p } );
      pf2.paint( gc );

      ts.setAlpha( alpha );
      tf.setStyle( ts );
      tf.setPoints( new Point[] { new Point( p.x, p.y + 20 ) } );
      tf.setText( "Text " + i );
      tf.paint( gc );

    }
    lf.paint( gc );

    final Point[] points2 = createPoints( pointSize );
    lf.setPoints( points2 );
    ls = new LineStyle( 1, new RGB( 0, 0, 255 ), 100, 5, new float[] { 10, 2, 3, 4, 2 }, LINEJOIN.BEVEL, LINECAP.FLAT, 5, true );
    lf.setStyle( ls );
    lf.paint( gc );

  }

  private Point[] getCrossMarker( )
  {
    final Point[] cross = new Point[12];
    cross[0] = new Point( 1, 3 );
    cross[1] = new Point( 2, 3 );
    cross[2] = new Point( 2, 2 );
    cross[3] = new Point( 3, 2 );
    cross[4] = new Point( 3, 1 );
    cross[5] = new Point( 2, 1 );
    cross[6] = new Point( 2, 0 );
    cross[7] = new Point( 1, 0 );
    cross[8] = new Point( 1, 1 );
    cross[9] = new Point( 0, 1 );
    cross[10] = new Point( 0, 2 );
    cross[11] = new Point( 1, 2 );
    return cross;
  }

  private Point[] createPoints( final int size )
  {
    final int width = m_shell.getBounds().width;
    final int height = m_shell.getBounds().height;

    final Point[] points = new Point[size];
    for( int i = 0; i < points.length; i++ )
      points[i] = new Point( (i * (int) ((float) width / (float) size)), (int) (Math.random() * height * 0.9) );
    return points;
  }

  private Point[][] createBars( final int count )
  {
    final int barWidth = m_shell.getBounds().width / count;
    final Point[][] bars = new Point[count][];
    for( int i = 0; i < count; i++ )
    {
      final Point[] bar = new Point[4];
      final int startX = i * barWidth;
      final int startY = (int) (0.95 * m_shell.getBounds().height);
      final int endY = startY - (int) (Math.random() * 0.95 * m_shell.getBounds().height);
      final int endX = startX + barWidth;
      bar[0] = new Point( startX, startY );
      bar[1] = new Point( startX, endY );
      bar[2] = new Point( endX, endY );
      bar[3] = new Point( endX, startY );
      bars[i] = bar;

    }
    return bars;
  }

  private Point[] createPolygonPoints( final int size )
  {
    final int width = m_shell.getBounds().width;
    final int height = m_shell.getBounds().height;

    final Point[] points = new Point[size];
    for( int i = 0; i < points.length; i++ )
    {
      final int x = (i * (width / size));
      int y = 10;

      if( !((i == 0) || (i == size - 1)) )
        y = (int) (Math.random() * height * 0.9);

      points[i] = new Point( x, y );
    }
    return points;
  }
}
