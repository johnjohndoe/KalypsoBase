package de.openali.diagram.framework.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.diagram.framework.model.mapper.component.IAxisComponent;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.diagram.framework.util.ChartUtilities;

/**
 * @author burtscher 
 * Implementation of IAxisComponent; AxisComponent is a widget displaying the charts' axes; its used
 *         to calculate screen coordinates for normalized values
 */
public class AxisCanvas<T extends Comparable> extends Canvas implements PaintListener, IAxisComponent<T>, Listener
{
  /**
   * the corresponding axis
   */
  private final IAxis<T> m_axis;
  private Image m_bufferImage=null;


  public AxisCanvas( final IAxis<T> axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_axis = axis;
    addPaintListener( this );
    addListener(SWT.Resize, this);
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
  public Point computeSize( int wHint, int hHint, boolean changed )
  {
    if( m_axis != null )
    {
      IAxisRenderer renderer = m_axis.getRegistry().getRenderer(m_axis);
      if( renderer != null )
      {
        int axisWidth = renderer.getAxisWidth( m_axis );
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
    paint( new GCWrapper( e.gc ) );
  }

  private void paint( final GCWrapper gc )
  {
    final Rectangle bounds = getClientArea();
    final Rectangle b = new Rectangle( 0,0, bounds.width , bounds.height );
    m_bufferImage=paintBuffered(gc, b, m_bufferImage);
  }


/**
 * double-buffered paint method; set to public in order to be used from ouside, e.g. from ChartImageContainer
 */
public Image paintBuffered( GCWrapper gcw, Rectangle screen, Image bufferImage )
{
  final Image usedBufferImage;
  if( bufferImage == null)
  {
    usedBufferImage = new Image( Display.getDefault(), screen.width, screen.height );
    GC buffGc = new GC( usedBufferImage );
    final GCWrapper buffGcw = new GCWrapper( buffGc );
    
    IAxisRenderer renderer=null;
    try
    {
        renderer = m_axis.getRegistry().getRenderer(m_axis);
        if( renderer != null )
        	renderer.paint( buffGcw, m_axis, screen );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      buffGc.dispose();
      buffGcw.dispose();
    }
  }
  else
    usedBufferImage = bufferImage;
  
  
  // muss so sein, wenn mann den layerClip immer setzt, kommts beim dragRect zeichnen zu
  // selstsamen effekten
  gcw.drawImage(usedBufferImage, screen.x, screen.y);
  
  return usedBufferImage;
}









  /**
   * Uses the widgets' complete extension to alculates the screen value in correspondance to a normalized value
   * 
   * @see de.openali.diagram.framework.model.mapper.component.IAxisComponent#normalizedToScreen(double)
   */
  public int normalizedToScreen( double normValue )
  {
    final int range = getRange();
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
      normValue = 1 - normValue;
    final int screenValue = (int) (range * normValue);
    return screenValue;
  }

  /**
   * Uses the widgets' complete extension to alculates the normalized value in correspondance to a screen value
   * 
   * @see de.openali.diagram.framework.model.mapper.component.IAxisComponent#screenToNormalized(int)
   */
  public double screenToNormalized( int screenValue )
  {
    final int range = getRange();
    if( range == 0 )
      return 0;
    final double normValue = (double) screenValue / range;
    if( ChartUtilities.isInverseScreenCoords( m_axis ) )
      return 1 - normValue;

    return normValue;
  }

  /**
   * calculates the significant (= not variable) extension of the widget
   */
  private int getRange( )
  {
    final Rectangle bounds = getBounds();
    final int range;
    if( m_axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      range = bounds.width;// horizontal
    else
      range = bounds.height; // vertical
    return range;
  }
  
  /** Forces a repaint of the axis. The buffered image is disposed. */
  @Override
  public void redraw( )
  {
	  
    if( m_bufferImage != null )
    {
      m_bufferImage.dispose();
      m_bufferImage = null;
    }
    super.redraw();
  }
  
  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( final Event event )
  {
    if( event.type == SWT.Resize )
      redraw();
  }
  

}
