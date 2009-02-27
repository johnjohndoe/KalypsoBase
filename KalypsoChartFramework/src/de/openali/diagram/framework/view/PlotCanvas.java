package de.openali.diagram.framework.view;

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
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.layer.ILayerManager;
import de.openali.diagram.framework.util.ChartUtilities;

/**
 * @author burtscher 
 * widget in which the layers' content is paintet
 */
public class PlotCanvas extends Canvas implements PaintListener, Listener
{

	
  private Image m_bufferImg = null;
  private ILayerManager m_manager;

  public PlotCanvas( final ILayerManager manager, final Composite parent, final int style)
  {
    super( parent, style );
  	m_manager = manager;

    addPaintListener( this );
    addListener( SWT.Resize, this );
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
    GCWrapper gcw = new GCWrapper( e.gc );
    final Rectangle screenArea = getClientArea();
    m_bufferImg = paintBuffered( gcw, screenArea, m_bufferImg );
    gcw.dispose();
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
      buffGcw.setAntialias(SWT.ON);
      try
      {
        final IChartLayer[] layers = m_manager.getLayers();
        for( final IChartLayer layer : layers )
        {
          if( layer.getVisibility() )
          {
            ChartUtilities.resetGC( buffGcw.m_gc);
            try
            {
            	layer.paint( buffGcw );
            }
            catch (SWTException e)
            {
            	e.printStackTrace();
            }
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
   // gcw.drawImage( usedBufferImage, screen.x, screen.y, screen.width, screen.height, screen.x, screen.y, screen.width, screen.height );
    gcw.drawImage(usedBufferImage, screen.x, screen.y);
    
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
