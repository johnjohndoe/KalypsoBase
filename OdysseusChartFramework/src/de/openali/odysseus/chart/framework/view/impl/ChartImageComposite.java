package de.openali.odysseus.chart.framework.view.impl;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.core.runtime.SafeRunnable;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.IAxisRegistryEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.ChartModelEventHandler;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandler;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

/**
 * @author kimwerner
 */
public class ChartImageComposite extends Canvas implements IChartComposite
{
  @Override
  public ChartImageInfo getPlotInfo( )
  {
    return m_paintJob.getPlotInfo();
  }

  private final ChartModelEventHandler m_chartModelEventHandler = new ChartModelEventHandler();

  private final ILayerManagerEventListener m_layerEventListener = new ChartImageLayerManagerEventListener( this );

  private final IAxisRegistryEventListener m_mapperListener = new ChartImageMapperRegistryEventListener( this );

  private IChartModel m_model;

  private Rectangle m_dragArea = null;

  private Point m_panOffset = new Point( 0, 0 );

  private EditInfo m_editInfo = null;

  private final ChartImagePlotHandler m_plotHandler = new ChartImagePlotHandler( this );

  private final ChartPaintJob m_paintJob = new ChartPaintJob( this );

  private boolean m_invalidatePending;

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
        updateClientArea();
        invalidate();
      }
    } );

    updateClientArea();
    setBackground( OdysseusChartFramework.getDefault().getColorRegistry().getResource( parent.getDisplay(), backgroundRGB ) );
    setChartModel( model );
  }

  void updateClientArea( )
  {
    m_paintJob.setClientArea( getClientArea() );
  }

  @Override
  public void dispose( )
  {
    unregisterListener();

    m_plotHandler.dispose();

    m_paintJob.dispose();

    super.dispose();
  }

  @Override
  public IChartModel getChartModel( )
  {
    return m_model;
  }

  @Override
  public void invalidate( )
  {
    m_paintJob.cancel();

    // REMARK: prevent schedule if this composite is not really visible;
    // we just remember that an invalidation should take place on next redraw
    // This is a performance optimization for the case when the chart is not visible
    // if( !isVisible() )
    // {
    // m_invalidatePending = true;
    // return;
    // }

    m_invalidatePending = false;

    m_paintJob.schedule( 50 );
  }

  final void handlePaint( final PaintEvent paintEvent )
  {
    if( m_invalidatePending )
      invalidate();

    final ImageData plotData = m_paintJob.getPlotImageData();
    final Point panOffset = m_panOffset;
    final Rectangle dragArea = m_dragArea;
    final EditInfo editInfo = m_editInfo;

    if( plotData == null )
      return;

    final GC paintGC = paintEvent.gc;

    final Image plotImage = new Image( paintEvent.display, plotData );
    paintGC.drawImage( plotImage, -panOffset.x, -panOffset.y );
    plotImage.dispose();

    paintDragArea( paintGC, dragArea );
    paintEditInfo( paintGC, editInfo );

    final IChartHandlerManager manager = getPlotHandler();
    final IChartHandler[] handlers = manager.getActiveHandlers();
    for( final IChartHandler handler : handlers )
    {
      SafeRunner.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          handler.paintControl( paintEvent );
        }
      } );
    }
  }

  private static void paintDragArea( final GC gcw, final Rectangle dragArea )
  {
    // Wenn ein DragRectangle da ist, dann muss nur das gezeichnet werden
    if( dragArea == null )
      return;

    gcw.setLineWidth( 1 );
    gcw.setForeground( gcw.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

    gcw.setBackground( gcw.getDevice().getSystemColor( SWT.COLOR_BLUE ) );
    final Rectangle r = RectangleUtils.createNormalizedRectangle( dragArea );

    gcw.setAlpha( 50 );
    gcw.fillRectangle( r );
    gcw.setAlpha( 255 );
    gcw.setLineStyle( SWT.LINE_DASH );
    gcw.drawRectangle( r );
  }

  private static final void paintEditInfo( final GC gc, final EditInfo editInfo )
  {
    if( editInfo == null )
      return;

    // draw hover shape
    if( editInfo.getHoverFigure() != null )
      editInfo.getHoverFigure().paint( gc );

    // draw edit shape
    if( editInfo.getEditFigure() != null )
      editInfo.getEditFigure().paint( gc );
  }

  private void registerListener( )
  {
    if( m_model == null )
      return;

    m_model.getLayerManager().addListener( m_layerEventListener );
    m_model.getAxisRegistry().addListener( m_mapperListener );
  }

  public void setChartModel( final IChartModel model )
  {
    setChartModel( m_model, model );
  }

  protected final void setChartModel( final IChartModel oldModel, final IChartModel newModel )
  {
    unregisterListener();

    m_model = newModel;

    registerListener();

    invalidate();

    m_chartModelEventHandler.fireModelChanged( oldModel, newModel );
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
      m_panOffset = new Point( 0, 0 );
    else
      m_panOffset = new Point( end.x - start.x, end.y - start.y );

    redraw();
  }

  private final void unregisterListener( )
  {
    if( m_model == null )
      return;

    m_model.getLayerManager().removeListener( m_layerEventListener );
    m_model.getAxisRegistry().removeListener( m_mapperListener );
  }

  @Override
  public IChartHandlerManager getPlotHandler( )
  {
    return m_plotHandler;
  }

  @Override
  public void addListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.addListener( listener );
  }

  @Override
  public void removeListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.removeListener( listener );
  }
}
