package org.kalypso.chart.framework.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

/**
 * @author burtscher widget in which the layers' content is painted
 */
public class PlotCanvas extends Canvas implements PaintListener, Listener
{
  private Image m_bufferImg = null;

  private final ILayerManager m_manager;

  private EditInfo m_hoverInfo;

  final Map<IChartLayer, Image> m_layerImageMap = new HashMap<IChartLayer, Image>();

  private Rectangle m_dragArea = null;

  /**
   * offset by which panned Layers are moved
   */
  private Point m_panOffset = new Point( 0, 0 );

  private boolean m_isEditing = false;

  /**
   * Layers which are panned
   */
  private HashSet<IChartLayer> m_panLayers = new HashSet<IChartLayer>();

  public PlotCanvas( final ILayerManager manager, final Composite parent, final int style )
  {
    super( parent, style );
    m_manager = manager;

    addPaintListener( this );
    addListener( SWT.Resize, this );

    for( final IChartLayer layer : manager.getLayers() )
    {
      m_layerImageMap.put( layer, null );
    }

    final AbstractLayerManagerEventListener m_almel = new AbstractLayerManagerEventListener()
    {
      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( IChartLayer< ? , ? > layer )
      {
        invalidate( new IChartLayer[] { layer } );
        // redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( IChartLayer< ? , ? > layer )
      {
        Image image = m_layerImageMap.get( layer );
        if( image != null && !image.isDisposed() )
          image.dispose();
        m_layerImageMap.remove( layer );
        invalidate( null );
        // redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerMoved( IChartLayer< ? , ? > layer )
      {
        invalidate( null );
        // redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( IChartLayer< ? , ? > layer )
      {
        invalidate( null );
        // redraw();
      }

      /**
       * @see org.kalypso.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerContentChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( IChartLayer< ? , ? > layer )
      {
        invalidate( new IChartLayer[] { layer } );
      }
    };
    manager.addListener( m_almel );

  }

  /**
   * @param layers
   *            Array of layers which should be redrawn; if null, only the bufferImage is redrawn
   */
  public void invalidate( final IChartLayer[] layers )
  {
    if( layers != null )
    {
      for( final IChartLayer layer : layers )
      {
        final Image image = m_layerImageMap.get( layer );
        if( image != null && !image.isDisposed() )
          image.dispose();
        m_layerImageMap.put( layer, null );
      }
    }
    else
    {
      for( final IChartLayer layer : m_layerImageMap.keySet() )
      {
        final Image image = m_layerImageMap.get( layer );
        if( image != null && !image.isDisposed() )
          image.dispose();
        m_layerImageMap.put( layer, null );
      }
    }
    if( m_bufferImg != null && !m_bufferImg.isDisposed() )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }
    redraw();
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

    if( m_layerImageMap != null )
    {
      for( final Image img : m_layerImageMap.values() )
      {
        if( img != null && !img.isDisposed() )
          img.dispose();
      }
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
    paintDragArea( gcw );
    paintTooltip( gcw );
    gcw.dispose();
  }

  private void paintDragArea( final GCWrapper gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_dragArea != null )
    {
      gcw.setLineWidth( 1 );
      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );
      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet), irgendwann mal wieder überprüfen
      gcw.setAlpha( 50 );
      gcw.fillRectangle( r.x, r.y, r.width, r.height );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( r.x, r.y, r.width, r.height );
    }
  }

  private void paintTooltip( final GCWrapper gcw )
  {
    // Tooltips zeichnen
    if( m_hoverInfo != null )
    {
      final Rectangle shape = m_hoverInfo.shape;
      gcw.drawRectangle( shape.x, shape.y, shape.width, shape.height );
      final Rectangle screen = gcw.getClipping();

      final String tooltiptext = m_hoverInfo.text;
      final Point mousePos = m_hoverInfo.pos;
      if( tooltiptext != null && mousePos != null )
      {
        final int TOOLINSET = 3;

        final Font oldFont = gcw.getFont();

        final Font bannerFont = JFaceResources.getTextFont();
        gcw.setFont( bannerFont );

        gcw.setBackground( getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
        gcw.setForeground( getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
        final Point toolsize = gcw.textExtent( tooltiptext );
        int toolx = mousePos.x + shape.width + 5 + TOOLINSET;
        if( toolx + toolsize.x > screen.width )
          toolx = toolx - toolsize.x;
        final int tooly = mousePos.y - 5 - TOOLINSET - toolsize.y;

        gcw.setLineWidth( 1 );
        final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2, toolsize.y + TOOLINSET * 2 );
        gcw.fillRectangle( toolrect );
        gcw.drawRectangle( toolrect );

        gcw.drawText( tooltiptext, toolx, tooly, true );

        gcw.setFont( oldFont );
      }
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
      try
      {
        final IChartLayer< ? , ? >[] layers = m_manager.getLayers();
        for( final IChartLayer< ? , ? > layer : layers )
          if( layer.isVisible() )
          {
            ChartUtilities.resetGC( buffGcw.m_gc );
            try
            {
              m_layerImageMap.put( layer, paintBufferedLayer( gcw, screen, layer, m_layerImageMap.get( layer ) ) );

              if( m_panLayers != null && m_panLayers.contains( layer ) )
              {
                buffGcw.drawImage( m_layerImageMap.get( layer ), screen.x - m_panOffset.x, screen.y - m_panOffset.y );
              }
              else
              {
                buffGcw.drawImage( m_layerImageMap.get( layer ), 0, 0 );
              }

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
    if( m_panLayers == null )
      gcw.drawImage( usedBufferImage, screen.x - m_panOffset.x, screen.y - m_panOffset.y );
    else
      gcw.drawImage( usedBufferImage, 0, 0 );

    return usedBufferImage;
  }

  private Image paintBufferedLayer( final GCWrapper gcw, final Rectangle screen, final IChartLayer< ? , ? > layer, final Image layerImage )
  {
    final Image usedBufferImage;
    if( layerImage == null )
    {
      final ImageData id = new ImageData( screen.width, screen.height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
      final Color transparentColor = new Color( gcw.getDevice(), new RGB( 0xfe, 0xff, 0xff ) );

      id.transparentPixel = 0xfffffe;

      usedBufferImage = new Image( Display.getDefault(), id );

      final GC buffGc = new GC( usedBufferImage );
      final GCWrapper buffGcw = new GCWrapper( buffGc );

      // Hintergrund explizit malen - der wird später transparent gezeichnet
      buffGcw.setBackground( transparentColor );
      buffGcw.fillRectangle( screen );

      try
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
      catch( final Exception e )
      {
        e.printStackTrace();
      }
      finally
      {

        transparentColor.dispose();
        buffGc.dispose();
        buffGcw.dispose();
      }
    }
    else
      usedBufferImage = layerImage;
    return usedBufferImage;
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent( final Event event )
  {
    if( event.type == SWT.Resize )
      invalidate( m_manager.getLayers() );
  }

  public void setTooltipInfo( final EditInfo hoverInfo )
  {
    m_hoverInfo = hoverInfo;
  }

  public EditInfo getTooltipInfo( )
  {
    return m_hoverInfo;
  }

  public boolean isEditing( )
  {
    return m_isEditing;
  }

  public void setIsEditing( final boolean isEditing )
  {
    m_isEditing = isEditing;
  }

  public boolean isDragging( )
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void setDragArea( final Rectangle dragArea )
  {
    m_dragArea = dragArea;
    redraw();
  }

  /**
   * sets an offset to which the paint buffer is moved in case of a pan action;
   * 
   * @param layers
   *            if null, all layers are moved; else, only mentioned layers are moved
   * 
   * @param offset
   *            positive value moves buffer to right / down, negative value to left / up
   */
  public void setPanOffset( final IChartLayer[] layers, final Point offset )
  {
    m_panOffset = offset;
    if( layers != null )
    {
      m_panLayers = new HashSet<IChartLayer>();
      for( final IChartLayer layer : layers )
      {
        m_panLayers.add( layer );
      }
      invalidate( null );
    }
    else
    {
      m_panLayers = null;
    }
    redraw();
  }

}
