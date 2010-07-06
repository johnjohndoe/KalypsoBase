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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.event.IMapperEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author burtscher Implementation of IAxisComponent; AxisComponent is a widget displaying the charts' axes; its used
 *         to calculate screen coordinates for normalized values
 */
public class AxisCanvas extends Canvas implements IAxisComponent, PaintListener
{
  /**
   * the corresponding axis
   */
  protected final IAxis m_axis;

  protected Image m_bufferImage = null;

  private Point m_dragInterval;

  private Point m_panOffset = null;

  private IMapperEventListener m_axisListener = new IMapperEventListener()
  {
    @Override
    public void onMapperChanged( IMapper mapper )
    {
      if( m_bufferImage != null )
      {
        m_bufferImage.dispose();
        m_bufferImage = null;
      }
    }
  };

  public AxisCanvas( final IAxis axis, final Composite parent, final int style )
  {
    super( parent, style );
    m_axis = axis;
    addPaintListener( this );

    addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( ControlEvent e )
      {
        if( m_bufferImage != null )
        {
          m_bufferImage.dispose();
          m_bufferImage = null;
        }
      }
    } );

    addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( DisposeEvent e )
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

// protected void handleControlResized( )
// {
// setAxisHeight();
// if( m_bufferImage != null )
// {
// m_bufferImage.dispose();
// m_bufferImage = null;
// }
// }

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
      final IAxisRenderer renderer = m_axis.getRenderer();
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

  private void paintDrag( GC gc, Rectangle bounds2 )
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

  public final Image createBufferImage( final Rectangle bounds )
  {
    final Image bufferImg = new Image( Display.getDefault(), bounds.width, bounds.height );
    final GC buffGc = new GC( bufferImg );
    ChartUtilities.resetGC( buffGc );
    try
    {
      final IAxisRenderer axisRenderer = m_axis.getRenderer();
      if( axisRenderer != null )
      {
        axisRenderer.paint( buffGc, m_axis, bounds );
      }
    }
    finally
    {
      buffGc.dispose();
    }
    return bufferImg;
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
   * does nothing right now as theres an error displaying the drag intervall
   */
  public void setDragInterval( int y1, int y2 )
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

  public void setPanOffsetInterval( Point offset )
  {
    m_panOffset = offset;
    redraw();
  }

  @Override
  public void setSize( Point size )
  {
    super.setSize( size );
    setAxisHeight( new Rectangle( 0, 0, size.x, size.y ) );
  }

  @Override
  public void setSize( int width, int height )
  {
    super.setSize( width, height );
    setAxisHeight( new Rectangle( 0, 0, width, height ) );
  }

  private void setAxisHeight( final Rectangle bounds )
  {

    if( m_axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      m_axis.setScreenHeight( bounds.width );
    }
    else
    {
      m_axis.setScreenHeight( bounds.height );
    }
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  @Override
  public void paintControl( PaintEvent e )
  {
    final Rectangle bounds = getClientArea();
    setAxisHeight( bounds );
// final Rectangle b = new Rectangle( 0, 0, bounds.width, bounds.height );
    if( m_bufferImage == null )
      m_bufferImage = createBufferImage( bounds );
    if( m_panOffset == null )
    {
      e.gc.drawImage( m_bufferImage, bounds.x, bounds.y );
      paintDrag( e.gc, bounds );
    }
    else
      e.gc.drawImage( m_bufferImage, bounds.x + m_panOffset.x, bounds.y + m_panOffset.y );
  }
}
