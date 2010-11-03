package de.openali.odysseus.chart.framework.view.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.ChartImageFactory;
import de.openali.odysseus.chart.framework.util.img.ChartLegendPainter;
import de.openali.odysseus.chart.framework.util.img.ChartTitlePainter;
import de.openali.odysseus.chart.framework.view.IAxisDragHandler;
import de.openali.odysseus.chart.framework.view.IChartDragHandler;
import de.openali.odysseus.chart.framework.view.TooltipHandler;

/**
 * @author kimwerner
 * @author burtscher Chart widget; parent for AxisComponent and Plot also acts as LayerManager and contains the
 *         AxisRegistry;
 */
public class ChartImageComposite extends Canvas
{

  private final class InvalidateChartJob extends UIJob
  {

    public InvalidateChartJob( final String name )
    {
      super( name );
    }

    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {

      if( (m_plotImage != null) && !m_plotImage.isDisposed() )
      {
        m_plotImage.dispose();
        m_plotImage = null;
      }
      if( (m_axesImage != null) && !m_axesImage.isDisposed() )
      {
        m_axesImage.dispose();
        m_axesImage = null;
      }
      if( (m_titleImage != null) && !m_titleImage.isDisposed() )
      {
        m_titleImage.dispose();
        m_titleImage = null;
      }
      if( (m_legendImage != null) && !m_legendImage.isDisposed() )
      {
        m_legendImage.dispose();
        m_legendImage = null;
      }
      if( isDisposed() )
        return Status.OK_STATUS;

      final IChartModel model = getChartModel();

      final IMapperRegistry mapperRegistry = model == null ? null : model.getMapperRegistry();
      if( mapperRegistry == null )
        return Status.OK_STATUS;

      final Rectangle pane = getClientArea();

      final ChartLegendPainter legendPainter = new ChartLegendPainter( model, pane.width );
      final ChartTitlePainter titlePainter = new ChartTitlePainter( model, pane.width );

      // TODO define legend text style as kod style element
      final ITextStyle legendTextStyle = StyleUtils.getDefaultTextStyle();
      legendTextStyle.setHeight( 7 );
      legendPainter.setTextStyle( legendTextStyle );

      final Point titleSize = titlePainter.getSize();
      final Point legendSize = legendPainter.getSize();

      m_plotRect = ChartImageFactory.calculatePlotSize( mapperRegistry, pane.width, pane.height - titleSize.y - legendSize.y );
      m_plotRect.y += titleSize.y;
      ChartImageFactory.setAxesHeight( mapperRegistry.getAxes(), m_plotRect );
      m_axesImage = ChartImageFactory.createAxesImage( getChartModel().getMapperRegistry(), pane, m_plotRect );

      m_titleImage = titlePainter.paint();
      m_legendImage = legendPainter.createImage();

      final ILayerManager layerManager = model == null ? null : model.getLayerManager();
      if( layerManager == null )
        return Status.OK_STATUS;

      m_plotImage = ChartImageFactory.createPlotImage( getChartModel().getLayerManager().getLayers(), m_plotRect );
      redraw();

      return Status.OK_STATUS;
    }
  }

  protected IChartModel m_model;

  protected Image m_plotImage = null;

  protected Image m_axesImage = null;

  protected FontData m_titlefont = null;

  protected Image m_titleImage = null;

  protected Image m_legendImage = null;

  protected Rectangle m_plotRect = null;

  private Rectangle m_dragArea = null;

  protected Point m_panOffset = new Point( 0, 0 );

  protected EditInfo m_editInfo = null;

  private final ILayerManagerEventListener m_layerEventListener = new ILayerManagerEventListener()
  {
    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onActivLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onActivLayerChanged( final IChartLayer layer )
    {
      // do nothing
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerAdded(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerAdded( final IChartLayer layer )
    {
      invalidate();
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerContentChanged( final IChartLayer layer )
    {
      invalidate();
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerMoved( final IChartLayer layer )
    {
      invalidate();
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerRemoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerRemoved( final IChartLayer layer )
    {
      invalidate();
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer )
    {
      invalidate();
    }
  };

  private final AbstractMapperRegistryEventListener m_mapperListener = new AbstractMapperRegistryEventListener()
  {
    /**
     * @see de.openali.odysseus.chart.framework.axis.IMapperRegistryEventListener#onMapperAdded(de.openali.odysseus.chart.framework.axis.IAxis)
     *      adds an AxisComponent for any newly added axis and reports Axis and its AxisComponent to the AxisRegistry
     */
    @Override
    public void onMapperAdded( final IMapper mapper )
    {
      invalidate();
    }

    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractMapperRegistryEventListener#onMapperRangeChanged(de.openali.odysseus.chart.framework.model.mapper.IMapper)
     */
    @Override
    public void onMapperChanged( final IMapper mapper )
    {

      invalidate();

    }

    /**
     * @see de.openali.odysseus.chart.framework.axis.IMapperRegistryEventListener#onMapperRemoved(de.openali.odysseus.chart.framework.axis.IAxis)
     *      TODO: not implemented yet (or is it? - right now there's no way to remove an axis, so this should be checked
     *      in the future)
     */
    @Override
    public void onMapperRemoved( final IMapper mapper )
    {
      invalidate();
    }
  };

  private final TooltipHandler m_tooltipHandler = null;// new TooltipHandler( this );

  private final InvalidateChartJob m_invalidateChartJob = new InvalidateChartJob( "" );

  public ChartImageComposite( final Composite parent, final int style, final IChartModel model, final RGB backgroundRGB )
  {
    super( parent, style | SWT.DOUBLE_BUFFERED );

    addPaintListener( new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent paintEvent )
      {
        if( m_axesImage == null )
          return;

        final GC gc = paintEvent.gc;

        gc.drawImage( m_axesImage, 0, 0 );

        if( m_titleImage != null )
          gc.drawImage( m_titleImage, 0, 0 );

        if( m_plotRect == null || m_plotImage == null )
          return;

        final Point plotAnchor = new Point( m_plotRect.x - m_panOffset.x, m_plotRect.y - m_panOffset.y );

        if( m_legendImage != null )
        {
          final int h1 = m_plotImage.getImageData().height;
          final int h2 = Math.abs( m_axesImage.getImageData().height - h1 - m_legendImage.getImageData().height );

          final int x = plotAnchor.x; // + m_plotRect.width / 2 - m_legendImage.getImageData().width;
          final int y = plotAnchor.y + h1 + h2;

          gc.drawImage( m_legendImage, x, y );
        }

        gc.setClipping( m_plotRect );
        gc.drawImage( m_plotImage, plotAnchor.x, plotAnchor.y );

        paintDragArea( gc );
        ChartImageFactory.paintEditInfo( gc, m_editInfo );
        gc.setClipping( getBounds() );
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

    addControlListener( new ControlListener()
    {

      @Override
      public void controlMoved( final ControlEvent arg0 )
      {
        // na und?
      }

      @Override
      public void controlResized( final ControlEvent arg0 )
      {
        invalidate();

      }
    } );
    setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( parent.getDisplay(), backgroundRGB ) );
    setChartModel( model );
  }

  public final void addAxisHandler( final IAxisDragHandler handler )
  {

    if( handler != null )
    {
      // TODO
// ac.addMouseListener( handler );
// ac.addMouseMoveListener( handler );
// ac.addKeyListener( handler );
    }

  }

  public final void addPlotHandler( final IChartDragHandler handler )
  {
    if( handler == null )
      setCursor( getDisplay().getSystemCursor( SWT.CURSOR_ARROW ) );
    else
    {
      addMouseListener( handler );
      addMouseMoveListener( handler );
    }
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    unregisterListener();

    if( m_tooltipHandler != null )
      m_tooltipHandler.dispose();
    if( m_plotImage != null )
      m_plotImage.dispose();
    if( m_axesImage != null )
      m_axesImage.dispose();
    if( m_titleImage != null )
      m_titleImage.dispose();
    super.dispose();
  }

  public final AxisCanvas getAxisCanvas( final IAxis axis )
  {
    return null;
  }

  public final EditInfo getChartInfo( )
  {
    return null;// getPlot() == null ? null : getPlot().getTooltipInfo();
  }

  public IChartModel getChartModel( )
  {
    return m_model;
  }

  public EditInfo getEditInfo( )
  {
    return m_editInfo;
  }

  public final Rectangle getPlotRect( )
  {
    return m_plotRect;
  }

  public FontData getTitleFont( )
  {
    return m_titlefont;
  }

  public void invalidate( )
  {
    if( isDisposed() )
      return;
    m_invalidateChartJob.cancel();
    m_invalidateChartJob.schedule( 50 );
  }

  protected void paintDragArea( final GC gcw )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( m_dragArea != null )
    {
      gcw.setLineWidth( 1 );
      gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
      final Rectangle r = RectangleUtils.createNormalizedRectangle( m_dragArea );
      final int x = r.x + getPlotRect().x;
      final int y = r.y + getPlotRect().y;

      // TODO: SWT-Bug mit drawFocus (wird nicht immer gezeichnet),
      // irgendwann mal wieder berprfen
      gcw.setAlpha( 50 );
      gcw.fillRectangle( x, y, r.width, r.height );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( x, y, r.width, r.height );
    }
  }

  private void registerListener( )
  {
    if( m_model == null )
      return;
    m_model.getLayerManager().addListener( m_layerEventListener );
    m_model.getMapperRegistry().addListener( m_mapperListener );
  }

  public final void removeAxisHandler( final IAxisDragHandler handler )
  {

// ac.removeMouseListener( handler );
// ac.removeMouseMoveListener( handler );
// ac.removeKeyListener( handler );

  }

  public final void removePlotHandler( final IChartDragHandler handler )
  {
    if( handler != null )
    {
      removeMouseListener( handler );
      removeMouseMoveListener( handler );
    }
  }

  public void setAxisPanOffset( final Point start, final Point end, final IAxis[] axes )
  {

// for( final IAxis axis : axes )
// {
// final AxisCanvas ac = getAxisCanvas( axis );
// if( ac == null )
// continue;
//
// if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
// {
// ac.setPanOffsetInterval( new Point( end.x - start.x, 0 ) );
// }
// else
// {
// ac.setPanOffsetInterval( new Point( 0, end.y - start.y ) );
// }
// }
  }

  public void setAxisZoomOffset( final Point start, final Point end, final IAxis[] axes )
  {
// int startZ = -1;
// int endZ = -1;
//
// for( final IAxis axis : axes )
// {
// final AxisCanvas ac = getAxisCanvas( axis );
// if( ac == null )
// continue;
// if( start != null && end != null )
// {
// if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
// {
// startZ = start.x;
// endZ = end.x;
// }
// else
// {
// startZ = start.y;
// endZ = end.y;
// }
// }
// ac.setDragInterval( startZ, endZ );
// }
  }

  public final void setChartInfo( final EditInfo editInfo )
  {
// if( getPlot() == null )
// return;
// getPlot().setTooltipInfo( editInfo );
  }

  public void setChartModel( final IChartModel model )
  {
    if( m_model != null )
    {
      unregisterListener();
    }
    m_model = model;
    registerListener();
    invalidate();
  }

  public final void setDragArea( final Rectangle dragArea )
  {
// if( m_dragArea == dragArea )
// return;
    m_dragArea = dragArea;
// if( m_dragArea == null )
// invalidate();
// else
    redraw();
  }

  public void setEditInfo( final EditInfo editInfo )
  {
    m_editInfo = editInfo;
  }

  public final void setPlotPanOffset( final IAxis[] axes, final Point start, final Point end )
  {
    m_panOffset = new Point( end.x - start.x, end.y - start.y );
    invalidate();
// getPlot().setPanOffset( getLayer( axes ), new Point( end.x - start.x, end.y - start.y ) );
  }

  public void setTitlefont( final FontData titlefont )
  {
    m_titlefont = titlefont;
  }

  protected final void unregisterListener( )
  {
    if( m_model == null )
      return;
    m_model.getLayerManager().removeListener( m_layerEventListener );
    m_model.getMapperRegistry().removeListener( m_mapperListener );
  }
}
