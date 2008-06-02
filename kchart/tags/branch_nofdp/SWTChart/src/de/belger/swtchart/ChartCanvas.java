package de.belger.swtchart;

import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.belger.swtchart.axis.AxisManager;
import de.belger.swtchart.axis.AxisRange;
import de.belger.swtchart.axis.IAxisRenderer;
import de.belger.swtchart.layer.IChartLayer;

/**
 * A 2D-Chart Widget.
 * <dl>
 * <dt><b>Styles: </b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events: </b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Painting is delegated to {@link de.belger.swtchart.layer.IChartLayer}s and
 * {@link de.belger.swtchart.axis.IAxisRenderer}s.
 * </p>
 * <p>
 * This class is not intended to be subclassed
 * </p>
 * 
 * @author gernot
 */
public class ChartCanvas extends Canvas implements PaintListener, DisposeListener
{
  private Collection<IChartCanvasListener> m_listeners = new LinkedList<IChartCanvasListener>();

  private final List<IChartLayer> m_layers = new LinkedList<IChartLayer>();

  private final AxisManager m_axisMap = new AxisManager();

  /** Keeps track, which layers are currently visible */
  private Map<IChartLayer, Boolean> m_layervisible = new HashMap<IChartLayer, Boolean>();

  /** In non null, this Rectangle will be drawn as an focusRect */
  private Rectangle m_dragRectangle = null;

  /**
   * If non null, the chart will be moved by this offset (only inner chart, not the axises)
   */
  private transient Point m_drawOffset = null;

  /** If non null, a hovering element is drawn */
  private transient EditInfo m_hoverInfo = null;

  /** If non null, an editing element is drawn */
  private transient EditInfo m_editInfo = null;

  /** Insets between chart and canvas borders */
  private final Insets m_insets;

  private Cursor m_cursor;

  private Image m_bufferImg;

  /** if non null, logicalRanges will resized to fit * */
  private Double m_fixAspectRatio;

  private Comparator<IChartLayer> m_layerComparator = new Comparator<IChartLayer>()
  {
    public int compare( IChartLayer o1, IChartLayer o2 )
    {
      if( o1.getZOrder() > o2.getZOrder() )
        return -1;
      if( o1.getZOrder() < o2.getZOrder() )
        return 1;
      return 0;
    }
  };

  public ChartCanvas( final Composite parent, final int style, final Insets insets )
  {
    super( parent, style | SWT.NO_BACKGROUND | SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED );

    m_insets = insets;
    addPaintListener( this );
    addDisposeListener( this );
    addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( ControlEvent e )
      {
        repaint();
      }
    } );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }
  }

  /**
   * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
   */
  public void widgetDisposed( final DisposeEvent e )
  {
    m_listeners.clear();
    m_layers.clear();
    m_axisMap.clear();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl( final PaintEvent e )
  {
    final GCWrapper paintgc = new GCWrapper( e.gc );

    final Rectangle screenArea = getClientArea();

    m_bufferImg = paintChart( e.display, paintgc, screenArea, m_bufferImg );
  }

  /**
   * Create an image of his chart
   * 
   * @return the returned image must be disposed by the caller
   */
  public Image paintImage( final Device device, final Rectangle screen )
  {
    final Image buffer = new Image( device, screen );

    final GCWrapper gc = new GCWrapper( new GC( buffer ) );

    try
    {
      paintChart( device, gc, screen, null );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      gc.dispose();
    }

    return buffer;
  }

  private void reduceByRatio( final Rectangle data, final Double ratio )
  {

    if( (data.width <= 0) || (data.height <= 0) || (ratio == null) || (ratio == 0) )
      return;

    Rectangle2D logRect = null;
    AxisRange valueRange = null;
    for( IChartLayer layer : m_layers )
    {
      {
        if( logRect == null )
        {
          logRect = layer.getBounds();
          valueRange = layer.getValueRange();
        }
        else if( layer.getValueRange() == valueRange )
          logRect.add( layer.getBounds() );
      }
    }
    if( logRect == null )
      return;
    // logRect.add(logRect.getMaxX() *ratio ,logRect.getMinY());
    final double dxLog = logRect.getWidth();
    final double dyLog = logRect.getHeight();
    final double dxScr = data.width;
    final double dyScr = data.height;
    final double dX = dxLog / dxScr;
    final double dY = dyLog / dyScr;
    if( (dX / dY) < ratio )
    {
      final int newW = (int) (dxLog / (dY * ratio));// ((logRect.getWidth() * data.height) / (logRect.getHeight() *
      // ratio));
      data.x = data.x + (data.width - newW) / 2;
      data.width = newW;
    }
    else
    {
      final int newH = (int) (ratio * dyLog / dX);
      data.y = data.y + (data.height - newH) / 2;
      data.height = newH;
    }
  }

  private Image paintChart( final Device device, final GCWrapper gc, final Rectangle screen, final Image bufferImage )
  {
    final Rectangle screenArea = new Rectangle( screen.x + m_insets.left, screen.y + m_insets.top, screen.width - m_insets.left - m_insets.right, screen.height - m_insets.top - m_insets.bottom );

    // make place for axises
    final Rectangle dataArea = m_axisMap.reduceByAxisSize( gc, screenArea );

    reduceByRatio( dataArea, m_fixAspectRatio );

    final Rectangle layerArea = new Rectangle( dataArea.x, dataArea.y, dataArea.width, dataArea.height );

    if( m_drawOffset != null )
    {
      layerArea.x += m_drawOffset.x;
      layerArea.y += m_drawOffset.y;
    }

    m_axisMap.setScreenArea( layerArea );

    final Rectangle layerClip = createLayerClip( screen, screenArea, dataArea );
    final Image usedBufferImage;
    if( bufferImage == null )
    {
      usedBufferImage = new Image( device, screen.width, screen.height );

      final GCWrapper buffGc = new GCWrapper( new GC( usedBufferImage ) );

      try
      {
        // leave next line out to have the nice 'outside axis' effect.
        buffGc.setClipping( layerClip );

        // Paint layers ordered by Z-Order (see IChartLayer.getZOrder())
        for( final ListIterator<IChartLayer> lIt = m_layers.listIterator( m_layers.size() ); lIt.hasPrevious(); )
        {
          final IChartLayer layer = lIt.previous();
          if( m_layervisible.get( layer ).booleanValue() )
            layer.paint( buffGc );
        }

        buffGc.setClipping( screen );

        // again set screenArea, if not, axis will also be panned
        m_axisMap.setScreenArea( dataArea );
        m_axisMap.paintAxises( buffGc, dataArea );

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

    // muss so sein, wenn mann den layerClip immer setzt, kommts beim dragRect zeichnen zu
    // selstsamen effekten
    gc.drawImage( usedBufferImage, screen.x, screen.y, screen.width, screen.height, screen.x, screen.y, screen.width, screen.height );

    if( m_editInfo != null )
      m_editInfo.layer.paintDrag( gc, m_editInfo.pos, m_editInfo.data );

    if( m_hoverInfo != null )
    {
      final Rectangle shape = m_hoverInfo.shape;
      gc.drawFocus( shape.x, shape.y, shape.width, shape.height );

      final String tooltiptext = m_hoverInfo.text;
      final Point mousePos = m_hoverInfo.pos;
      if( tooltiptext != null )
      {
        final int TOOLINSET = 3;

        final Font oldFont = gc.getFont();

        final Font bannerFont = JFaceResources.getTextFont();
        gc.setFont( bannerFont );

        gc.setBackground( getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
        gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
        final Point toolsize = gc.textExtent( tooltiptext );
        // final int toolx = shape.x + shape.width + 5 + TOOLINSET;
        // final int tooly = shape.y - 5 - TOOLINSET - toolsize.y;
        int toolx = mousePos.x + shape.width + 5 + TOOLINSET;
        if( toolx + toolsize.x > screen.width )
          toolx = toolx - toolsize.x;
        final int tooly = mousePos.y - 5 - TOOLINSET - toolsize.y;

        gc.setLineWidth( 1 );
        final Rectangle toolrect = new Rectangle( toolx - TOOLINSET, tooly - TOOLINSET, toolsize.x + TOOLINSET * 2, toolsize.y + TOOLINSET * 2 );
        gc.fillRectangle( toolrect );
        gc.drawRectangle( toolrect );

        gc.drawText( tooltiptext, toolx, tooly, true );

        gc.setFont( oldFont );
      }
    }

    if( m_dragRectangle != null )
    {
      gc.setLineWidth( 1 );
      gc.drawFocus( m_dragRectangle.x, m_dragRectangle.y, m_dragRectangle.width, m_dragRectangle.height );
    }

    return usedBufferImage;
  }

  /** Forces a repaint of the complete chart. The buffered image is disposed. */
  public void repaint( )
  {
    if( m_bufferImg != null )
    {
      m_bufferImg.dispose();
      m_bufferImg = null;
    }

    redraw();
  }

  /**
   * Ein kleiner Hack, um die Clip-Region für die Layer auszurechnen. Ist eigentlich gleich dataArea, nur dort wo keine
   * Achsen sind, der ganze screen.
   */
  private Rectangle createLayerClip( final Rectangle screen, final Rectangle screenArea, final Rectangle dataArea )
  {
    final int left = dataArea.x == screenArea.x ? screen.x : dataArea.x;
    final int top = dataArea.y == screenArea.y ? screen.y : dataArea.y;
    final int right = (dataArea.x + dataArea.width) == (screenArea.x + screenArea.width) ? screen.x + screen.width : dataArea.x + dataArea.width;
    final int bottom = (dataArea.y + dataArea.height) == (screenArea.y + screenArea.height) ? screen.y + screen.height : dataArea.y + dataArea.height;

    return new Rectangle( left, top, right - left, bottom - top );
  }

  public void maximize( )
  {
    // finde alle ranges
    m_axisMap.maxLogicalRanges();
    repaint();
  }

  public void addLayer( final IChartLayer layer, final boolean visible )
  {
    m_layers.add( layer );
    // sort by its z-Order
    Collections.sort( m_layers, m_layerComparator );
    m_layervisible.put( layer, visible );
    m_axisMap.addRange( layer.getDomainRange(), layer );
    m_axisMap.addRange( layer.getValueRange(), layer );

    fireLayersChanged();
  }

  public IChartLayer getLayer( final String layerId )
  {
    for( IChartLayer layer : m_layers )
    {
      if( layer.getId().equals( layerId ) )
        return layer;
    }
    return null;
  }

  public void removeLayer( final IChartLayer layer )
  {
    m_layers.remove( layer );
    m_layervisible.remove( layer );
    m_axisMap.removeRange( layer.getDomainRange(), layer );
    m_axisMap.removeRange( layer.getValueRange(), layer );

    fireLayersChanged();
  }

  public void clearLayers( )
  {
    m_layers.clear();
    m_axisMap.clearLayers();

    fireLayersChanged();

    repaint();
  }

  public void clear( )
  {
    m_layers.clear();
    m_axisMap.clear();

    fireLayersChanged();

    repaint();
  }

  public void setAxisRenderer( final AxisRange range, final IAxisRenderer renderer )
  {
    m_axisMap.setRenderer( range, renderer );
  }

  public void setDragArea( final Rectangle rectangle )
  {
    if( m_dragRectangle == rectangle )
      return;

    final Rectangle redrawRect = m_dragRectangle == null ? rectangle : m_dragRectangle;
    if( rectangle != null )
      redrawRect.add( rectangle );

    m_dragRectangle = rectangle;

    redraw( redrawRect.x - 2, redrawRect.y - 2, redrawRect.width + 4, redrawRect.height + 4, true );
  }

  public void setDrawOffset( final Point offset )
  {
    m_drawOffset = offset;

    repaint();
  }

  public boolean isHovering( )
  {
    return m_hoverInfo != null;
  }

  public EditInfo getHoverInfo( )
  {
    return m_hoverInfo;
  }

  public void setHoverInfo( final EditInfo info )
  {
    m_hoverInfo = info;

    if( info == null )
      super.setCursor( m_cursor );
    else
      super.setCursor( getDisplay().getSystemCursor( SWT.CURSOR_HAND ) );

    redraw();
  }

  public boolean isEditing( )
  {
    return m_editInfo != null;
  }

  public EditInfo getEditInfo( )
  {
    return m_editInfo;
  }

  public void setEditInfo( final EditInfo info )
  {
    m_editInfo = info;

    if( info == null )
      super.setCursor( m_cursor );
    else
      super.setCursor( getDisplay().getSystemCursor( SWT.CURSOR_HAND ) );

    redraw();
  }

  public Collection<IChartLayer> getLayers( )
  {
    return Collections.unmodifiableCollection( m_layers );
  }

  public void addLayerListener( final IChartCanvasListener l )
  {
    m_listeners.add( l );
  }

  public void removeLayerListener( final IChartCanvasListener l )
  {
    m_listeners.remove( l );
  }

  private void fireLayersChanged( )
  {
    for( final IChartCanvasListener l : m_listeners )
      l.onLayersChanged();
  }

  public boolean isVisible( final IChartLayer l )
  {
    return m_layervisible.get( l );
  }

  public void setVisible( final IChartLayer layer, final boolean visible )
  {
    final boolean oldvisible = m_layervisible.get( layer );
    if( visible == oldvisible )
      return;

    m_layervisible.put( layer, visible );
    fireLayersChanged();

    repaint();
  }

  /**
   * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
   */
  @Override
  public void setCursor( final Cursor cursor )
  {
    m_cursor = cursor;

    if( isHovering() || isEditing() )
      return;

    super.setCursor( cursor );
  }

  /**
   * @return true, if drag-rect is not null
   */
  public boolean isDragging( )
  {
    return m_dragRectangle != null || m_drawOffset != null;
  }

  public Double getFixAspectRatio( )
  {
    return m_fixAspectRatio;
  }

  public void setFixAspectRatio( Double fixAspectRatio )
  {
    m_fixAspectRatio = fixAspectRatio;
  }
}
