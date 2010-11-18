package de.openali.odysseus.chart.framework.view.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author burtscher widget in which the layers content is painted
 */
public class PlotCanvas extends Canvas implements PaintListener
{
  private Image m_bufferImg = null;

  private ILayerManager m_layerManager;

 // private EditInfo m_editInfo;

  private final Map<IChartLayer, Image> m_layerImageMap = new HashMap<IChartLayer, Image>();

 // private final Map<IChartLayer, Point> m_layerPanOffsets = new HashMap<IChartLayer, Point>();

 // private final Rectangle m_dragArea = null;

  private final GenericLayerRenderer m_layerRenderer = new GenericLayerRenderer();

  /**
   * @param bufferLayers
   *          if set to true, each layer is buffered on an individual image; set this to true if you plan to offer
   *          panning of single layers in the chart front end
   */
  public PlotCanvas( final Composite parent, final int style )
  {
    super( parent, style );

    addPaintListener( this );
    addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        disposeImages();
      }
    } );
    addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        disposeImages();
      }
    } );
  }

  protected void disposeImages( )
  {
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    for( final Image img : m_layerImageMap.values() )
      img.dispose();
    m_layerImageMap.clear();
  }

  public EditInfo getTooltipInfo( )
  {
    return m_layerRenderer.getTooltipInfo();
  }

  /**
   * invalidate is called when a redraw is needed
   * 
   * @param layers
   *          Array of layers which should be redrawn; if null, only the bufferImage is redrawn
   */
  public void invalidate( final IChartLayer[] layers )
  {
    final IChartLayer[] layersToInvalidate = getLayersToInvalidate( layers );
    for( final IChartLayer layer : layersToInvalidate )
    {
      final Image image = m_layerImageMap.get( layer );
      if( (image != null) && !image.isDisposed() )
        image.dispose();
      m_layerImageMap.remove( layer );
    }

    if( (m_bufferImg != null) && !m_bufferImg.isDisposed() )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    if( !isDisposed() )
    {
      redraw();
    }
  }

  private IChartLayer[] getLayersToInvalidate( final IChartLayer[] layers )
  {
    if( layers != null )
      return layers;

    if( m_layerManager == null )
      return new IChartLayer[0];

    return m_layerManager.getLayers();
  }

//  /**
//   ** Renders the current plot into a newly created image and returns it.<br/>
//   * The caller is responsible to dispose the image.
//   */
//  public Image createImage( final IChartLayer[] layers, final Rectangle screen )
//  {
//    if( layers == null )
//      return null;
//
//    final Image bufferImage = new Image( getDisplay(), screen.width, screen.height );
//
//    final GC buffGc = new GC( bufferImage );
//    try
//    {
//      for( final IChartLayer layer : layers )
//      {
//        if( layer.isVisible() )
//        {
//          ChartUtilities.resetGC( buffGc );
//          if( !m_layerImageMap.containsKey( layer ) )
//            m_layerImageMap.put( layer, createLayerImage( buffGc.getDevice(), screen, layer ) );
//
//          final Image image = m_layerImageMap.get( layer );
//          final Point point = m_layerPanOffsets.get( layer );
//          if( point != null )
//            buffGc.drawImage( image, -point.x, -point.y );
//          else
//            buffGc.drawImage( image, 0, 0 );
//        }
//      }
//    }
//    finally
//    {
//      buffGc.dispose();
//    }
//
//    return bufferImage;
//  }
//
//  private Image createLayerImage( final Device device, final Rectangle screen, final IChartLayer layer )
//  {
//    final ImageData id = new ImageData( screen.width, screen.height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
//
//    id.transparentPixel = 0xfffffe;
//
//    final Image image = new Image( device, id );
//    final GC gc = new GC( image );
//
//    // Hintergrund explizit malen - der wird später transparent gezeichnet
//    final Color transparentColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( device, new RGB( 0xfe, 0xff, 0xff ) );
//    gc.setBackground( transparentColor );
//    gc.fillRectangle( screen );
//
//    try
//    {
//      ChartUtilities.resetGC( gc );
//      final long start = System.currentTimeMillis();
//      layer.paint( gc );
//      final long end = System.currentTimeMillis();
//      Logger.logInfo( Logger.TOPIC_LOG_PLOT, "Time to paint layer (" + layer.getTitle() + "): " + (end - start) + " ms" );
//    }
//    catch( final Exception e )
//    {
//      // catch all in order to protect from bad code in layer implementations
//      e.printStackTrace();
//    }
//    finally
//    {
//      gc.dispose();
//    }
//
//    return image;
//
//  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) Draws all layers if a
   *      PaintEvent is thrown; only exception: if the PaintEvent is thrown by a MouseDrag-Action, a buffered plot image
   *      is used
   */
  @Override
  public void paintControl( final PaintEvent e )
  {
    if( m_layerManager == null )
      return;

    final Rectangle screenArea = getClientArea();
    if( m_bufferImg == null )
    {

      m_bufferImg = new Image( getDisplay(), screenArea.width, screenArea.height );
      final GC tmpGC = new GC( m_bufferImg );
      try
      {
        final IChartLayer[] layers = m_layerManager.getLayers();
        m_layerRenderer.paintPlot( m_layerImageMap, tmpGC, layers );
        // m_bufferImg = createImage( layers /* getNotPannedLayers() */, screenArea );
      }
      finally
      {
        tmpGC.dispose();
      }
    }
    e.gc.drawImage( m_bufferImg, 0, 0 );
    m_layerRenderer.paintDragArea( e.gc );
    m_layerRenderer.paintEditInfo( e.gc );
  }

//  private void paintDragArea( final GC gcw )
//  {
//    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
//    if( m_dragArea != null )
//    {
//      gcw.setLineWidth( 1 );
//      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );
//
//      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
//      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );// new Rectangle( x, y, w, h ) );
//
//      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet),
//      // irgendwann mal wieder �berpr�fen
//      gcw.setAlpha( 50 );
//      gcw.fillRectangle( r.x, r.y, r.width, r.height );
//      gcw.setAlpha( 255 );
//      gcw.setLineStyle( SWT.LINE_DASH );
//      gcw.drawRectangle( r.x, r.y, r.width, r.height );
//    }
//  }
//
//  private void paintEditInfo( final GC gcw )
//  {
//    ChartUtilities.resetGC( gcw );
//    if( m_editInfo == null )
//      return;
//
//    // draw hover shape
//    if( m_editInfo.m_hoverFigure != null )
//      m_editInfo.m_hoverFigure.paint( gcw );
//    // draw edit shape
//    if( m_editInfo.m_editFigure != null )
//      m_editInfo.m_editFigure.paint( gcw );
//
//    // draw tooltip
//    ChartUtilities.resetGC( gcw );
//
//    final Rectangle screen = gcw.getClipping();
//
//    String tooltiptext = m_editInfo.m_text;
//    final Point mousePos = m_editInfo.m_pos;
//    if( (tooltiptext != null) && (mousePos != null) )
//    {
//      tooltiptext = tooltiptext.replace( '\r', ' ' );
//
//      final int TOOLINSET = 3;
//
//      final Font oldFont = gcw.getFont();
//
//      final Font bannerFont = JFaceResources.getTextFont();
//      gcw.setFont( bannerFont );
//
//      gcw.setBackground( getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
//      gcw.setForeground( getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
//      final Point toolsize = gcw.textExtent( tooltiptext );
//
//      /*
//       * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genï¿½gend
//       * Platz ist, dann wird er nach links verschoben. Der Startpunkt soll dabei immer im sichtbaren Bereich liegen.
//       */
//      int toolx = mousePos.x + 3 + TOOLINSET;
//      if( toolx + toolsize.x > screen.width )
//      {
//        toolx = screen.width - 5 - toolsize.x;
//        if( toolx < 5 )
//          toolx = 5;
//      }
//
//      int tooly = mousePos.y + 3 + TOOLINSET + 20;
//      if( (tooly + toolsize.y > screen.height) && ((mousePos.y - 3 - TOOLINSET - toolsize.y - 20) > 0) )
//        tooly = mousePos.y - 3 - TOOLINSET - toolsize.y - 20;
//
//      gcw.setLineWidth( 1 );
//      final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2, toolsize.y + TOOLINSET * 2 );
//      gcw.fillRectangle( toolrect );
//      gcw.drawRectangle( toolrect );
//
//      gcw.drawText( tooltiptext, toolx, tooly, true );
//
//      gcw.setFont( oldFont );
//    }
//  }

  public void setDragArea( final Rectangle dragArea )
  {
    m_layerRenderer.setDragArea( dragArea );
//    m_dragArea = dragArea;
//    redraw();
  }

  public void setLayerManager( final ILayerManager layerManager )
  {
    m_layerManager = layerManager;

    disposeImages();
  }

  /**
   * sets an offset to which the paint buffer is moved in case of a pan action;
   * 
   * @param layers
   *          if null, all layers are moved; else, only mentioned layers are moved
   * @param offset
   *          positive value moves buffer to right / down, negative value to left / up
   */
  public void setPanOffset( final IChartLayer[] layers, final Point offset )
  {
    final IChartLayer[] layerToPan = layers == null ? m_layerManager.getLayers() : layers;
    m_layerRenderer.setPanOffset( layerToPan, offset );
//    for( final IChartLayer iChartLayer : layerToPan )
//      m_layerPanOffsets.put( iChartLayer, offset );

    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    if( offset != null )
      redraw();
  }

  public void setTooltipInfo( final EditInfo hoverInfo )
  {
    m_layerRenderer.setTooltipInfo( hoverInfo );
//    if( hoverInfo == null )
//      m_editInfo = hoverInfo;
//    m_editInfo = hoverInfo;

  }

}
