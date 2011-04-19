package de.openali.odysseus.chart.framework.view.impl;

import java.awt.Insets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.util.img.ChartPainter;
import de.openali.odysseus.chart.framework.util.img.ChartTooltipPainter;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IPlotHandler;

/**
 * @author kimwerner
 */
public class ChartImageComposite extends Canvas implements IChartComposite
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
      return doInvalidateChart();
    }
  }

  private IChartModel m_model;

  private Image m_image = null;

  private Rectangle m_plotRect = null;

  private Rectangle m_dragArea = null;

  private Point m_panOffset = new Point( 0, 0 );

  private EditInfo m_editInfo = null;

  private EditInfo m_tooltipInfo = null;

  private final ChartTooltipPainter m_tooltipPainter = new ChartTooltipPainter();

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

  private final TooltipHandler m_tooltipHandler = new TooltipHandler( this );

  private final InvalidateChartJob m_invalidateChartJob = new InvalidateChartJob( "" );

  private final ChartImagePlotHandler m_plotHandler = new ChartImagePlotHandler( this );

  public ChartImageComposite( final Composite parent, final int style, final IChartModel model, final RGB backgroundRGB )
  {
    super( parent, style | SWT.DOUBLE_BUFFERED );

    addPaintListener( new PaintListener()
    {
      @Override
      public void paintControl( final PaintEvent paintEvent )
      {
        handlePaint( paintEvent );
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

    addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent arg0 )
      {
        invalidate();
      }
    } );

    setBackground( OdysseusChartFramework.getDefault().getColorRegistry().getResource( parent.getDisplay(), backgroundRGB ) );
    setChartModel( model );
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
    if( m_image != null )
      m_image.dispose();
    super.dispose();
  }

  @Override
  public IChartModel getChartModel( )
  {
    return m_model;
  }

  public EditInfo getEditInfo( )
  {
    return m_editInfo;
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IChartComposite#getPlot()
   */
  @Override
  public Canvas getPlot( )
  {
    return this;
  }

  @Override
  public final Rectangle getPlotRect( )
  {
    return m_plotRect;
  }

  @Override
  public EditInfo getTooltipInfo( )
  {
    return m_tooltipInfo;
  }

  @Override
  public void invalidate( )
  {
    if( isDisposed() )
      return;

    // FIXME: why this check? Why not cancel in WAITING state?
    if( m_invalidateChartJob.getState() == Job.SLEEPING )
      m_invalidateChartJob.cancel();

    m_invalidateChartJob.schedule( 50 );
  }

  protected IStatus doInvalidateChart( )
  {
    if( m_image != null && !m_image.isDisposed() )
    {
      m_image.dispose();
      m_image = null;
    }

    if( isDisposed() )
      return Status.OK_STATUS;

    final IChartModel model = getChartModel();

    final IMapperRegistry mapperRegistry = model == null ? null : model.getMapperRegistry();
    if( mapperRegistry == null )
      return Status.OK_STATUS;

    final Rectangle panel = getClientArea();
    final ChartPainter chartPainter = new ChartPainter( model, panel );//,new Insets(25,25,25,25));
    m_plotRect = RectangleUtils.inflateRect( panel, chartPainter.getPlotInsets() );
    m_image = chartPainter.createImage( m_panOffset );

    redraw();

    return Status.OK_STATUS;
  }

  protected void handlePaint( final PaintEvent paintEvent )
  {
    if( m_image == null )
      return;

    final GC gc = paintEvent.gc;
    gc.drawImage( m_image, 0, 0 );// -m_panOffset.x, -m_panOffset.y );
    final Transform newTransform = new Transform( gc.getDevice() );
    gc.getTransform( newTransform );
    newTransform.translate( m_plotRect.x, m_plotRect.y );
    try
    {
      gc.setTransform( newTransform );

      paintDragArea( gc );
      paintEditInfo( gc );
      paintTooltipInfo( gc );
    }
    finally
    {
      newTransform.translate( -m_plotRect.x, -m_plotRect.y );
      gc.setTransform( newTransform );
      newTransform.dispose();
    }
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

      gcw.setAlpha( 50 );
      gcw.fillRectangle( r );
      gcw.setAlpha( 255 );
      gcw.setLineStyle( SWT.LINE_DASH );
      gcw.drawRectangle( r );
    }
  }

  protected final void paintEditInfo( final GC gc )
  {
    if( m_editInfo == null )
      return;
    // draw hover shape
    if( m_editInfo.getHoverFigure() != null )
      m_editInfo.getHoverFigure().paint( gc );
    // draw edit shape
    if( m_editInfo.getEditFigure() != null )
      m_editInfo.getEditFigure().paint( gc );

  }

  protected final void paintTooltipInfo( final GC gc )
  {
    if( m_tooltipInfo == null )
      return;
    m_tooltipPainter.setTooltip( m_tooltipInfo.getText() );
    m_tooltipPainter.paint( gc, m_tooltipInfo.getPosition() );
  }

  @Override
  public final Point plotPoint2screen( final Point plotPoint )
  {
    if( m_plotRect == null )
      return plotPoint;
    return new Point( plotPoint.x + m_plotRect.x, plotPoint.y + m_plotRect.y );
  }

  private void registerListener( )
  {
    if( m_model == null )
      return;
    m_model.getLayerManager().addListener( m_layerEventListener );
    m_model.getMapperRegistry().addListener( m_mapperListener );
  }

  @Override
  public final Point screen2plotPoint( final Point screen )
  {
    if( m_plotRect == null )
      return screen;

    return new Point( screen.x - m_plotRect.x, screen.y - m_plotRect.y );
  }

  public void setChartModel( final IChartModel model )
  {
    if( m_model != null )
      unregisterListener();

    m_model = model;
    registerListener();
    invalidate();
  }

  @Override
  public final void setDragArea( final Rectangle dragArea )
  {
    if( m_dragArea == null && dragArea == null )
      return;
    m_dragArea = dragArea;
    redraw();
  }

  @Override
  public void setEditInfo( final EditInfo editInfo )
  {
    if( m_editInfo == null && editInfo == null )
      return;
    m_editInfo = editInfo;
    redraw();
  }

  @Override
  public final void setPanOffset( final IAxis[] axes, final Point start, final Point end )
  {
    if( start == null || end == null )
    {
      m_panOffset = new Point( 0, 0 );
      return;
    }
    m_panOffset = new Point( end.x - start.x, end.y - start.y );
    invalidate();
  }

  @Override
  public void setTooltipInfo( final EditInfo tooltipInfo )
  {
    if( m_tooltipInfo == null && tooltipInfo == null )
      return;
    m_tooltipInfo = tooltipInfo;
    redraw();
  }

  protected final void unregisterListener( )
  {
    if( m_model == null )
      return;
    m_model.getLayerManager().removeListener( m_layerEventListener );
    m_model.getMapperRegistry().removeListener( m_mapperListener );
  }

  /**
   * @see de.openali.odysseus.chart.framework.view.IChartComposite#getPlotHandler()
   */
  @Override
  public IPlotHandler getPlotHandler( )
  {
    return m_plotHandler;
  }

}
