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
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
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
    DeviceData data = new DeviceData();

    data.tracking = true;

    Display d = new Display( data );

    Sleak sleak = new Sleak();

    sleak.open();

    m_shell = new Shell( d );
    m_shell.setSize( 500, 400 );
    m_shell.setLayout( new FillLayout() );

    Canvas c = new Canvas( m_shell, SWT.FILL );
    c.addPaintListener( this );

    m_shell.open();

    while( !m_shell.isDisposed() )
      d.readAndDispatch();
  }

  public static void main( String... args )
  {
    new StyleTest();

  }

  public void paintControl( PaintEvent e )
  {
    GC gc = e.gc;
    gc.setAntialias( SWT.ON );

    int pointSize = 20;

    // Polygon
    Point[] points0 = createPolygonPoints( pointSize );
    PolygonFigure pf = new PolygonFigure();
    IAreaStyle as = StyleUtils.getDefaultAreaStyle();
    as.setFill( new ColorFill( new RGB( 255, 0, 0 ) ) );
    pf.setStyle( as );
    pf.setPoints( points0 );
    pf.paint( gc );

    // bars
    Point[][] bars = createBars( 20 );
    PolygonFigure bf = new PolygonFigure();
    IAreaStyle asBar = StyleUtils.getDefaultAreaStyle();
    asBar.setFill( new ColorFill( new RGB( 255, 0, 0 ) ) );
    asBar.setAlpha( 100 );
    bf.setStyle( asBar );
    for( Point[] bar : bars )
    {
      bf.setPoints( bar );
      bf.paint( gc );
    }

    Point[] points1 = createPoints( pointSize );

    // Polyline
    PolylineFigure lf = new PolylineFigure();
    ILineStyle ls = new LineStyle( 3, new RGB( 0, 255, 0 ), 100, 0, null, LINEJOIN.MITER, LINECAP.ROUND, 5, true );
    lf.setStyle( ls );
    lf.setPoints( points1 );

    IMarker m1 = new OvalMarker();
    // TODO: hier sollte natürlich eine URL reingefüllt werden
    IMarker m2 = new ImageMarker( null );
    Point[] markerPoints = getCrossMarker();

    IMarker m3 = new PolygonMarker( markerPoints );

    // Point
    IPointStyle ps = StyleUtils.getDefaultPointStyle();
    PointFigure pf2 = new PointFigure();

    // Text
    ITextStyle ts = StyleUtils.getDefaultTextStyle();
    ts.setFamily( "Ani" );
    TextFigure tf = new TextFigure();

    for( int i = 0; i < points1.length; i++ )
    {
      Point p = points1[i];
      ps.setWidth( i * 2 );
      ps.setHeight( i * 3 );
      int alpha = (255 / points1.length) * (i + 1);
      ps.setAlpha( alpha );
      int mod = i % 3;
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

    Point[] points2 = createPoints( pointSize );
    lf.setPoints( points2 );
    ls = new LineStyle( 1, new RGB( 0, 0, 255 ), 100, 5, new float[] { 10, 2, 3, 4, 2 }, LINEJOIN.BEVEL, LINECAP.FLAT, 5, true );
    lf.setStyle( ls );
    lf.paint( gc );

  }

  private Point[] getCrossMarker( )
  {
    Point[] cross = new Point[12];
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

  private Point[] createPoints( int size )
  {
    int width = m_shell.getBounds().width;
    int height = m_shell.getBounds().height;

    Point[] points = new Point[size];
    for( int i = 0; i < points.length; i++ )
      points[i] = new Point( (i * (int) ((float) width / (float) size)), (int) (Math.random() * height * 0.9) );
    return points;
  }

  private Point[][] createBars( int count )
  {
    int barWidth = m_shell.getBounds().width / count;
    Point[][] bars = new Point[count][];
    for( int i = 0; i < count; i++ )
    {
      Point[] bar = new Point[4];
      int startX = i * barWidth;
      int startY = (int) (0.95 * m_shell.getBounds().height);
      int endY = startY - (int) (Math.random() * 0.95 * m_shell.getBounds().height);
      int endX = startX + barWidth;
      bar[0] = new Point( startX, startY );
      bar[1] = new Point( startX, endY );
      bar[2] = new Point( endX, endY );
      bar[3] = new Point( endX, startY );
      bars[i] = bar;

    }
    return bars;
  }

  private Point[] createPolygonPoints( int size )
  {
    int width = m_shell.getBounds().width;
    int height = m_shell.getBounds().height;

    Point[] points = new Point[size];
    for( int i = 0; i < points.length; i++ )
    {
      int x = (i * (width / size));
      int y = 10;

      if( !((i == 0) || (i == size - 1)) )
        y = (int) (Math.random() * height * 0.9);

      points[i] = new Point( x, y );
    }
    return points;
  }
}
