package de.openali.odysseus.chart.framework.view.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
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
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author burtscher widget in which the layers content is painted
 */
public class PlotCanvas extends Canvas implements PaintListener
{
  private Image m_bufferImg = null;

  private Image m_panImg = null;

  private ILayerManager m_layerManager;

  private EditInfo m_editInfo;

  final Map<IChartLayer, Image> m_layerImageMap = new HashMap<IChartLayer, Image>();

  private Rectangle m_dragArea = null;

  /**
   * offset by which panned Layers are moved
   */
  private Point m_panOffset = null;

// private boolean m_isEditing = false;

  /**
   * Layers which are panned
   */
  private final List<IChartLayer> m_panLayers = new ArrayList<IChartLayer>();

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
      public void controlResized( ControlEvent e )
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
    if( m_panImg != null )
    {
      m_panImg.dispose();
      m_panImg = null;
    }
    for( Image img : m_layerImageMap.values() )
      img.dispose();
    m_layerImageMap.clear();
  }

  public EditInfo getTooltipInfo( )
  {
    return m_editInfo;
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
      redraw();
  }

  private IChartLayer[] getLayersToInvalidate( final IChartLayer[] layers )
  {
    if( layers != null )
      return layers;

    if( m_layerManager == null )
      return new IChartLayer[0];

    return m_layerManager.getLayers();
  }

  public boolean isDragging( )
  {
    // TODO: Check callers
    return false;
  }

//  public boolean isEditing( )
//  {
//    return m_isEditing;
//  }

  /**
   ** Renders the current plot into a newly created image and returns it.<br/>
   * The caller is responsible to dispose the image.
   */
  public Image createImage( final IChartLayer[] layers, final Rectangle screen )
  {
    if( layers == null )
      return null;

    final Image bufferImage = new Image( Display.getDefault(), screen.width, screen.height );

    final GC buffGc = new GC( bufferImage );
    try
    {
      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() && !m_panLayers.contains( layer ) )
        {
          ChartUtilities.resetGC( buffGc );
          if( !m_layerImageMap.containsKey( layer ) )
            m_layerImageMap.put( layer, createLayerImage( buffGc.getDevice(), screen, layer ) );

          final Image image = m_layerImageMap.get( layer );
          buffGc.drawImage( image, 0, 0 );
        }
      }
    }
    finally
    {
      buffGc.dispose();
    }

    return bufferImage;
  }

  private Image createLayerImage( final Device device, final Rectangle screen, final IChartLayer layer )
  {
    final ImageData id = new ImageData( screen.width, screen.height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );

    id.transparentPixel = 0xfffffe;

    final Image image = new Image( Display.getDefault(), id );
    final GC gc = new GC( image );

    // Hintergrund explizit malen - der wird spÃ¤ter transparent gezeichnet
    final Color transparentColor = OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( device, new RGB( 0xfe, 0xff, 0xff ) );
    gc.setBackground( transparentColor );
    gc.fillRectangle( screen );

    try
    {
      ChartUtilities.resetGC( gc );
      final long start = System.currentTimeMillis();
      layer.paint( gc );
      final long end = System.currentTimeMillis();
      Logger.logInfo( Logger.TOPIC_LOG_PLOT, "Time to paint layer (" + layer.getTitle() + "): " + (end - start) + " ms" );
    }
    catch( final Exception e )
    {
      // catch all in order to protect from bad code in layer implementations
      e.printStackTrace();
    }
    finally
    {
      gc.dispose();
    }

    return image;

  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent) Draws all layers if a
   *      PaintEvent is thrown; only exeption: if the PaintEvent is thrown by a MouseDrag-Action, a buffered plot image
   *      is used
   */
  @Override
  public void paintControl( final PaintEvent e )
  {
    if( m_layerManager == null )
      return;

    final Rectangle screenArea = getClientArea();
    if( m_bufferImg == null )
      m_bufferImg = createImage( m_layerManager.getLayers(), screenArea );
    e.gc.drawImage( m_bufferImg, 0, 0 );
    paintPannedLayer( e.gc );
    paintDragArea( e.gc );
    paintEditInfo( e.gc );
  }

  private void paintPannedLayer( final GC gcw )
  {
    if( m_panOffset == null )
      return;

    if( m_panImg == null )
      m_panImg = createImage( m_panLayers.toArray( new IChartLayer[] {} ), getClientArea() );

    for( final IChartLayer layer : m_panLayers )
    {
      final Image layerImage = m_layerImageMap.get( layer );
      gcw.drawImage( layerImage, -m_panOffset.x, -m_panOffset.y );
    }
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
      // irgendwann mal wieder �berpr�fen
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
    if( m_editInfo == null )
      return;

    // draw hover shape
    if( m_editInfo.m_hoverFigure != null )
      m_editInfo.m_hoverFigure.paint( gcw );
    // draw edit shape
    if( m_editInfo.m_editFigure != null )
      m_editInfo.m_editFigure.paint( gcw );

    // draw tooltip
    ChartUtilities.resetGC( gcw );

    final Rectangle screen = gcw.getClipping();

    String tooltiptext = m_editInfo.m_text;
    final Point mousePos = m_editInfo.m_pos;
    if( (tooltiptext != null) && (mousePos != null) )
    {
      tooltiptext = tooltiptext.replace( '\r', ' ' );

      final int TOOLINSET = 3;

      final Font oldFont = gcw.getFont();

      final Font bannerFont = JFaceResources.getTextFont();
      gcw.setFont( bannerFont );

      gcw.setBackground( getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
      gcw.setForeground( getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
      final Point toolsize = gcw.textExtent( tooltiptext );

      /*
       * Positionieren der Tooltip-Box: der ideale Platz liegt rechts unter dem Mauszeiger. Wenn rechts nicht genï¿½gend
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

  public void setDragArea( final Rectangle dragArea )
  {
    m_dragArea = dragArea;
    redraw();
  }

//  public void setIsEditing( final boolean isEditing )
//  {
//    m_isEditing = isEditing;
//  }

  public void setLayerManager( ILayerManager layerManager )
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
  public void setPanOffset( IChartLayer[] layers, final Point offset )
  {
    // start pan
    if( m_panOffset == null )
    {
      for( IChartLayer layer : layers == null ? m_layerManager.getLayers() : layers )
        m_panLayers.add( layer );

      if( m_bufferImg != null )
      {
        m_bufferImg.dispose();
        m_bufferImg = null;
      }
    }

    // end pan
    if( offset == null )
    {
      m_panLayers.clear();
      if( m_panImg != null )
      {
        m_panImg.dispose();
        m_panImg = null;
      }
      if( m_bufferImg != null )
      {
        m_bufferImg.dispose();
        m_bufferImg = null;
      }
    }

    m_panOffset = offset;

    redraw();
  }

  public void setTooltipInfo( final EditInfo hoverInfo )
  {
    if( hoverInfo == null )
      m_editInfo = hoverInfo;
    m_editInfo = hoverInfo;

  }

}
