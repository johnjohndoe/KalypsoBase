package org.kalypso.chart.framework.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher Implementation of IAxisComponent; AxisComponent is a widget displaying the charts' axes; its used
 *         to calculate screen coordinates for normalized values
 */
public class AxisCanvas<T> extends Canvas implements PaintListener, IAxisComponent, Listener
{
  /**
   * the corresponding axis
   */
  private final IAxis<T> m_axis;

  private Image m_bufferImage = null;

  private IAxisRenderer< ? > m_renderer;

  private Point m_dragInterval;

  private Point m_panOffset = new Point( 0, 0 );

  public AxisCanvas( final IAxis<T> axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_axis = axis;
    addPaintListener( this );
    addListener( SWT.Resize, this );
  }

  protected void recalcTicks( )
  {

  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_bufferImage != null )
    {
      m_bufferImage.dispose();
      m_bufferImage = null;
    }
    super.dispose();
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
   * @return Point p where p.x is the widgets width and p.y is its height; as there is only 1 dimension in which the
   *         axis size needs to be fixed - width for horizontal, height for vertical axes- the variable dimension is set
   *         to 0
   */
  @SuppressWarnings("unchecked")
  @Override
  public Point computeSize( final int wHint, final int hHint, final boolean changed )
  {
    if( m_axis != null )
    {
      final IAxisRenderer renderer = m_axis.getRegistry().getRenderer( m_axis );
      if( renderer != null )
      {
        final int axisWidth = renderer.getAxisWidth( m_axis );
        if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
          return new Point( 0, axisWidth );
        else
          return new Point( axisWidth, 0 );
      }
    }
    return new Point( 0, 0 );
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl( final PaintEvent e )
  {
    GCWrapper gcw = new GCWrapper( e.gc );
    paint( gcw );
    gcw.dispose();
  }

  private void paint( final GCWrapper gc )
  {
    final Rectangle bounds = getClientArea();
    final Rectangle b = new Rectangle( 0, 0, bounds.width, bounds.height );
    m_bufferImage = paintBuffered( gc, b, m_bufferImage );
    paintDrag( gc, b );
  }

  private void paintDrag( GCWrapper gc, Rectangle bounds2 )
  {
    if( m_dragInterval != null )
    {
      Color bg = gc.getBackground();
      gc.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_BLACK ) );

      int x;
      int y;
      int width;
      int height;

      if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        x = Math.min( m_dragInterval.x, m_dragInterval.y );
        y = bounds2.y;
        width = Math.abs( m_dragInterval.x - m_dragInterval.y );
        height = bounds2.height;
      }
      else
      {
        x = bounds2.x;
        y = Math.min( m_dragInterval.x, m_dragInterval.y );
        width = bounds2.width;
        height = Math.abs( m_dragInterval.x - m_dragInterval.y );
      }

      Rectangle rect = new Rectangle( x, y, width, height );
      gc.setAlpha( 100 );
      gc.fillRectangle( rect );
      gc.setAlpha( 255 );
      gc.setBackground( bg );
    }

  }

  /**
   * double-buffered paint method; set to public in order to be used from ouside, e.g. from ChartImageContainer
   */
  public Image paintBuffered( final GCWrapper gcw, final Rectangle screen, final Image bufferImage )
  {
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( Display.getDefault(), screen.width, screen.height );
      final GC buffGc = new GC( usedBufferImage );
      final GCWrapper buffGcw = new GCWrapper( buffGc );

      IAxisRenderer renderer = null;
      try
      {
        renderer = m_axis.getRegistry().getRenderer( m_axis );
        if( renderer != null )
          renderer.paint( buffGcw, m_axis, screen );
      }
      finally
      {
        buffGc.dispose();
        buffGcw.dispose();
      }

    }
    else
      usedBufferImage = bufferImage;

    gcw.drawImage( usedBufferImage, screen.x + m_panOffset.x, screen.y + m_panOffset.y );

    return usedBufferImage;
  }

  /**
   * Uses the widgets' complete extension to alculates the screen value in correspondance to a normalized value
   * 
   * @see org.kalypso.chart.framework.model.mapper.component.IAxisComponent#normalizedToScreen(double)
   */
  public int normalizedToScreen( final double normValue )
  {
    double myNormValue = normValue;
    final int range = getRange();
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
    {
      myNormValue = 1 - myNormValue;
    }
    final int screenValue = (int) (range * myNormValue);
    return screenValue;
  }

  /**
   * Uses the widgets' complete extension to alculates the normalized value in correspondance to a screen value
   * 
   * @see org.kalypso.chart.framework.model.mapper.component.IAxisComponent#screenToNormalized(int)
   */
  public double screenToNormalized( final int screenValue )
  {
    final int range = getRange();
    if( range == 0 )
    {
      return 0;
    }
    final double normValue = (double) screenValue / range;
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
    {
      return 1 - normValue;
    }

    return normValue;
  }

  /**
   * calculates the significant (= not variable) extension of the widget
   */
  private int getRange( )
  {
    final Rectangle bounds = getBounds();
    final int range;
    if( m_axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      range = bounds.width;// horizontal
    }
    else
    {
      range = bounds.height; // vertical
    }
    return range;
  }

  @Override
  public void layout( )
  {
    if( m_bufferImage != null )
    {
      m_bufferImage.dispose();
      m_bufferImage = null;
    }
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( final Event event )
  {

    if( event.type == SWT.Resize )
    {
      if( m_bufferImage != null )
      {
        m_bufferImage.dispose();
        m_bufferImage = null;

        m_renderer = m_axis.getRegistry().getRenderer( m_axis );
        m_renderer.invalidateTicks( m_axis );
      }
      redraw();
    }

  }

  /**
   * does nothing right now as theres an error displaying the drag intervall
   */
  public void setDragInterval( int y1, int y2 )
  {
    if( true )
      return;

    if( y1 == -1 || y2 == -1 )
    {
      m_dragInterval = null;
    }
    else
    {
      m_dragInterval = new Point( y1, y2 );
    }
    redraw();
  }

  public void setPanOffsetInterval( Point offset )
  {
    if( true )
      return;
    m_panOffset = offset;
    redraw();
  }
}
