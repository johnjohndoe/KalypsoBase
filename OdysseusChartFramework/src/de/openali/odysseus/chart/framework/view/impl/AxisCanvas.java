package de.openali.odysseus.chart.framework.view.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author burtscher Implementation of IAxisComponent; AxisComponent is a widget displaying the charts' axes; its used
 *         to calculate screen coordinates for normalized values
 */
public class AxisCanvas extends Canvas implements IAxisComponent
{
  /**
   * the corresponding axis
   */
  protected final IAxis m_axis;

  private Image m_bufferImage = null;

  protected IAxisRenderer m_renderer;

  private Point m_dragInterval;

  private final Point m_panOffset = new Point( 0, 0 );

  private final IMapperEventListener m_axisListener = new IMapperEventListener()
  {
    @Override
    public void onMapperChanged( final IMapper mapper )
    {
      final Object data = getLayoutData();
      if( data != null && data instanceof GridData )
      {
        ((GridData) data).exclude = !m_axis.isVisible();
      }
      handleControlResized();
    }
  };

  public AxisCanvas( final IAxis axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_axis = axis;
    addPaintListener( new PaintListener()
    {

      @Override
      public void paintControl( final PaintEvent e )
      {
        paint( e.gc );
      }
    } );

    addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        handleControlResized();
      }
    } );

    addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    axis.addListener( m_axisListener );
  }

  public IAxis getAxis( )
  {
    return m_axis;
  }

  protected void handleControlResized( )
  {
    setAxisHeight();
    if( m_bufferImage != null )
    {
      m_bufferImage.dispose();
      m_bufferImage = null;

      m_renderer = m_axis.getRegistry().getRenderer( m_axis );
      m_renderer.invalidateTicks( m_axis );
    }
    redraw();
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
    m_axis.removeListener( m_axisListener );

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
        {
          return new Point( 0, axisWidth );
        }
        else
        {
          return new Point( axisWidth, 0 );
        }
      }
    }
    return new Point( 1, 1 );
  }

// /**
// * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
// */
// public void paintControl( final PaintEvent e )
// {
// paint( e.gc );
// }

  protected void paint( final GC gc )
  {
    setAxisHeight();
    final Rectangle bounds = getClientArea();
    final Rectangle b = new Rectangle( 0, 0, bounds.width, bounds.height );
    m_bufferImage = paintBuffered( gc, b, m_bufferImage );
    paintDrag( gc, b );
  }

  private void paintDrag( final GC gc, final Rectangle bounds2 )
  {
    if( m_dragInterval != null )
    {
      final Color bg = gc.getBackground();
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

      final Rectangle rect = new Rectangle( x, y, width, height );
      gc.setAlpha( 100 );
      gc.fillRectangle( rect );
      gc.setAlpha( 255 );
      gc.setBackground( bg );
    }

  }

  /**
   * double-buffered paint method; set to public in order to be used from ouside, e.g. from ChartImageContainer
   */
  public Image paintBuffered( final GC gcw, final Rectangle screen, final Image bufferImage )
  {
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( Display.getDefault(), screen.width, screen.height );
      final GC buffGc = new GC( usedBufferImage );

      IAxisRenderer renderer = null;
      try
      {
        renderer = m_axis.getRegistry().getRenderer( m_axis );
        if( renderer != null )
        {
          renderer.paint( buffGc, m_axis, screen );
        }
      }
      finally
      {
        buffGc.dispose();
      }
    }
    else
    {
      usedBufferImage = bufferImage;
    }

    gcw.drawImage( usedBufferImage, screen.x + m_panOffset.x, screen.y + m_panOffset.y );

    return usedBufferImage;
  }

  @Override
  public void layout( )
  {
    setAxisHeight();
    if( m_bufferImage != null )
    {
      m_bufferImage.dispose();
      m_bufferImage = null;
    }
  }

  /**
   * does nothing right now as theres an error displaying the drag intervall
   */
  public void setDragInterval( final int y1, final int y2 )
  {
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

  public void setPanOffsetInterval( @SuppressWarnings("unused") final Point offset )
  {
    // TODO: check callers
// if( true )
// {
// return;
// }
//
// m_panOffset = offset;
// redraw();
  }

  @Override
  public void setSize( final Point size )
  {
    super.setSize( size );
    setAxisHeight();
  }

  @Override
  public void setSize( final int width, final int height )
  {
    super.setSize( width, height );
    setAxisHeight();
  }

  private void setAxisHeight( )
  {

    if( m_axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      m_axis.setScreenHeight( getBounds().width );
    }
    else
    {
      m_axis.setScreenHeight( getBounds().height );
    }
  }
}
