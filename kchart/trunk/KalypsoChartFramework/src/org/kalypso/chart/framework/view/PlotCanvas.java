package org.kalypso.chart.framework.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author burtscher widget in which the layers' content is paintet
 */
public class PlotCanvas extends Canvas implements PaintListener, Listener
{

  private Image m_bufferImg = null;

  private final ILayerManager m_manager;

  private IChartMouseHandler m_mouseHandler;

  public PlotCanvas( final ILayerManager manager, final Composite parent, final int style )
  {
    super( parent, style );
    m_manager = manager;

    addPaintListener( this );
    addListener( SWT.Resize, this );

    AbstractLayerManagerEventListener m_almel = new AbstractLayerManagerEventListener()
    {
      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( IChartLayer layer )
      {
        redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( IChartLayer layer )
      {
        redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( IChartLayer layer )
      {
        redraw();
      }
    };
    manager.addListener( m_almel );
  }

  public void setMouseHandler( final IChartMouseHandler handler )
  {
    m_mouseHandler = handler;
    addMouseListener( m_mouseHandler );
    addMouseMoveListener( m_mouseHandler );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    removePaintListener( this );
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }
    super.dispose();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) Draws all layers if a
   *      PaintEvent is thrown; only exeption: if the PaintEvent is thrown by a MouseDrag-Action, a buffered plot image
   *      is used
   */
  public void paintControl( final PaintEvent e )
  {
    final GCWrapper gcw = new GCWrapper( e.gc );
    final Rectangle screenArea = getClientArea();
    m_bufferImg = paintBuffered( gcw, screenArea, m_bufferImg );
    // Der MouseHandler soll jetzt noch ein ZoomRechteck zeichnen
    if( m_mouseHandler != null )
      m_mouseHandler.paint( gcw );
    gcw.dispose();
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
      buffGcw.setAntialias( SWT.ON );
      try
      {
        final IChartLayer[] layers = m_manager.getLayers();
        for( final IChartLayer layer : layers )
          if( layer.isVisible() )
          {
            ChartUtilities.resetGC( buffGcw.m_gc );
            try
            {
              layer.paint( buffGcw );
            }
            catch( final SWTException e )
            {
              e.printStackTrace();
            }
          }
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
    // gcw.drawImage( usedBufferImage, screen.x, screen.y, screen.width, screen.height, screen.x, screen.y,
    // screen.width, screen.height );
    gcw.drawImage( usedBufferImage, screen.x, screen.y );

    return usedBufferImage;
  }

  /** Forces a repaint of the complete chart. The buffered image is disposed. */
  @Override
  public void redraw( )
  {
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    if( !isDisposed() )
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
