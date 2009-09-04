package de.openali.odysseus.chart.framework.view.impl;

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
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author burtscher widget in which the layers content is painted
 */
public class PlotCanvas extends Canvas implements PaintListener, Listener
{
  private Image m_bufferImg = null;

  private final ILayerManager m_manager;

  private EditInfo m_editInfo;

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

  private final boolean m_bufferLayers;

  public PlotCanvas( final ILayerManager manager, final Composite parent, final int style )
  {
    this( manager, parent, style, true );
  }

  /**
   * @param bufferLayers
   *          if set to true, each layer is buffered on an individual image; set this to true if you plan to offer
   *          panning of single layers in the chart front end
   */
  public PlotCanvas( final ILayerManager manager, final Composite parent, final int style, final boolean bufferLayers )
  {
    super( parent, style );
    m_manager = manager;
    m_bufferLayers = bufferLayers;

    addPaintListener( this );
    addListener( SWT.Resize, this );

    if( m_bufferLayers )
      for( IChartLayer layer : manager.getLayers() )
        m_layerImageMap.put( layer, null );

    final AbstractLayerManagerEventListener m_almel = new AbstractLayerManagerEventListener()
    {
      /**
       * @see de.openali.odysseus.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerAdded(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( IChartLayer layer )
      {
        invalidate( new IChartLayer[] { layer } );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerRemoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( IChartLayer layer )
      {
        Image image = m_layerImageMap.get( layer );
        if( (image != null) && !image.isDisposed() )
          image.dispose();
        m_layerImageMap.remove( layer );
        invalidate( null );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerRemoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerMoved( IChartLayer layer )
      {
        invalidate( null );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( IChartLayer layer )
      {
        invalidate( null );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.layer.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( IChartLayer layer )
      {
        invalidate( new IChartLayer[] { layer } );
      }
    };
    manager.addListener( m_almel );
  }

  /**
   * invalidate is called when a redraw is needed
   * 
   * @param layers
   *          Array of layers which should be redrawn; if null, only the bufferImage is redrawn
   */
  public void invalidate( IChartLayer[] layers )
  {
    if( (layers != null) && m_bufferLayers )
      for( IChartLayer layer : layers )
      {
        Image image = m_layerImageMap.get( layer );
        if( (image != null) && !image.isDisposed() )
          image.dispose();
        m_layerImageMap.put( layer, null );
      }
    if( (m_bufferImg != null) && !m_bufferImg.isDisposed() )
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
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    if( m_layerImageMap != null )
      for( Image img : m_layerImageMap.values() )
        if( img != null )
          img.dispose();

    m_manager.dispose();

    super.dispose();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) Draws all layers if a
   *      PaintEvent is thrown; only exeption: if the PaintEvent is thrown by a MouseDrag-Action, a buffered plot image
   *      is used
   */
  public void paintControl( final PaintEvent e )
  {
    GC gc = e.gc;
    final Rectangle screenArea = getClientArea();
    m_bufferImg = paintBuffered( gc, screenArea, m_bufferImg );
    paintDragArea( gc );
    paintEditInfo( gc );
  }

  private void paintDragArea( final GC gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_dragArea != null )
    {
      gcw.setLineWidth( 1 );
      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );
      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet),
      // irgendwann mal wieder ï¿½berprï¿½fen
      gcw.setAlpha( 50 );
      gcw.fillRectangle( r.x, r.y, r.width, r.height );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( r.x, r.y, r.width, r.height );
    }
  }

  private void paintEditInfo( final GC gcw )
  {
    ChartUtilities.resetGC( gcw );

    if( m_editInfo != null )
    {
      // draw hover shape
      if( m_editInfo.m_hoverFigure != null )
        m_editInfo.m_hoverFigure.paint( gcw );
      // draw edit shape
      if( m_editInfo.m_editFigure != null )
        m_editInfo.m_editFigure.paint( gcw );

      // draw tooltip
      ChartUtilities.resetGC( gcw );

      final Rectangle screen = gcw.getClipping();

      final String tooltiptext = m_editInfo.m_text;
      final Point mousePos = m_editInfo.m_pos;
      if( (tooltiptext != null) && (mousePos != null) )
      {
        final int TOOLINSET = 3;

        final Font oldFont = gcw.getFont();

        final Font bannerFont = JFaceResources.getTextFont();
        gcw.setFont( bannerFont );

        gcw.setBackground( getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
        gcw.setForeground( getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
        final Point toolsize = gcw.textExtent( tooltiptext );

        /*
         * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genügend
         * Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich liegen.
         */

        int toolx = mousePos.x + 3 + TOOLINSET;
        if( toolx + toolsize.x > screen.width )
        {
          toolx = screen.width - 5 - toolsize.x;
          if( toolx < 5 )
            toolx = 5;
        }

        int tooly = mousePos.y + 3 + TOOLINSET + 20;
        if( (tooly + toolsize.y > screen.height) && ((mousePos.y - 3 - TOOLINSET - toolsize.y - 20) > 0) )
          tooly = mousePos.y - 3 - TOOLINSET - toolsize.y - 20;

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
  public Image paintBuffered( final GC gcw, final Rectangle screen, final Image bufferImage )
  {
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( Display.getDefault(), screen.width, screen.height );

      final GC buffGc = new GC( usedBufferImage );
      try
      {
        final IChartLayer[] layers = m_manager.getLayers();
        for( final IChartLayer layer : layers )
          if( layer.isVisible() )
          {
            ChartUtilities.resetGC( buffGc );
            try
            {
              if( m_bufferLayers )
              {
                m_layerImageMap.put( layer, paintBufferedLayer( gcw, screen, layer, m_layerImageMap.get( layer ) ) );

                if( (m_panLayers != null) && m_panLayers.contains( layer ) )
                  buffGc.drawImage( m_layerImageMap.get( layer ), screen.x - m_panOffset.x, screen.y - m_panOffset.y );
                else
                  buffGc.drawImage( m_layerImageMap.get( layer ), 0, 0 );
              }
              else
                layer.paint( buffGc );

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
      }
    }
    else
      usedBufferImage = bufferImage;

    // muss so sein, wenn mann den layerClip immer setzt, kommts beim
    // dragRect zeichnen zu
    // selstsamen effekten
    if( m_panLayers == null )
      gcw.drawImage( usedBufferImage, screen.x - m_panOffset.x, screen.y - m_panOffset.y );
    else
      gcw.drawImage( usedBufferImage, 0, 0 );

    return usedBufferImage;
  }

  private Image paintBufferedLayer( GC gcw, Rectangle screen, IChartLayer layer, Image layerImage )
  {
    final Image usedBufferImage;
    if( layerImage == null )
    {
      ImageData id = new ImageData( screen.width, screen.height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
      Color transparentColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gcw.getDevice(), new RGB( 0xfe, 0xff, 0xff ) );

      id.transparentPixel = 0xfffffe;

      usedBufferImage = new Image( Display.getDefault(), id );

      final GC buffGc = new GC( usedBufferImage );

      // Hintergrund explizit malen - der wird spï¿½ter transparent
      // gezeichnet
      buffGc.setBackground( transparentColor );
      buffGc.fillRectangle( screen );

      try
      {
        ChartUtilities.resetGC( buffGc );
        try
        {
          long start = System.currentTimeMillis();
          layer.paint( buffGc );
          long end = System.currentTimeMillis();
          Logger.logInfo( Logger.TOPIC_LOG_PLOT, "Time to paint layer (" + layer.getTitle() + "): " + (end - start) + " ms" );
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
        buffGc.dispose();
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
    if( hoverInfo == null )
      m_editInfo = hoverInfo;
    m_editInfo = hoverInfo;

  }

  public EditInfo getTooltipInfo( )
  {
    return m_editInfo;
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
   *          if null, all layers are moved; else, only mentioned layers are moved
   * @param offset
   *          positive value moves buffer to right / down, negative value to left / up
   */
  public void setPanOffset( IChartLayer[] layers, final Point offset )
  {
    m_panOffset = offset;
    if( layers != null )
    {
      m_panLayers = new HashSet<IChartLayer>();
      for( IChartLayer layer : layers )
        m_panLayers.add( layer );
      invalidate( null );
    }
    else
      m_panLayers = null;
    redraw();
  }

}
