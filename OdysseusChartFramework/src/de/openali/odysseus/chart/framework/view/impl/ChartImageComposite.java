package de.openali.odysseus.chart.framework.view.impl;

import java.awt.Insets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.IMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.ChartModelEventHandler;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.util.img.ChartPainter;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandler;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

/**
 * @author kimwerner
 */
public class ChartImageComposite extends Canvas implements IChartComposite
{
  protected final InvalidateChartImageCompositeJob m_invalidateChartJob = new InvalidateChartImageCompositeJob( this );

  private final ChartModelEventHandler m_chartModelEventHandler = new ChartModelEventHandler();

  private final ILayerManagerEventListener m_layerEventListener = new ChartImageLayerManagerEventListener( this );

  private final IMapperRegistryEventListener m_mapperListener = new ChartImageMapperRegistryEventListener( this );

  private IChartModel m_model;

  private Image m_image = null;

  private Rectangle m_plotRect = null;

  private Rectangle m_dragArea = null;

  private Point m_panOffset = new Point( 0, 0 );

  private EditInfo m_editInfo = null;

  private final Insets m_insets;

  private final ChartImagePlotHandler m_plotHandler = new ChartImagePlotHandler( this );

  public ChartImageComposite( final Composite parent, final int style, final IChartModel model, final RGB backgroundRGB )
  {
    this( parent, style, model, backgroundRGB, new Insets( 3, 3, 3, 3 ) );
  }

  public ChartImageComposite( final Composite parent, final int style, final IChartModel model, final RGB backgroundRGB, final Insets insets )
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

    m_insets = insets;

    setBackground( OdysseusChartFramework.getDefault().getColorRegistry().getResource( parent.getDisplay(), backgroundRGB ) );
    setChartModel( model );
  }

  @Override
  public void dispose( )
  {
    unregisterListener();

    if( m_image != null )
      m_image.dispose();

    m_plotHandler.dispose();

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

  @Override
  public final Rectangle getPlotRect( )
  {
    return m_plotRect;
  }

  @Override
  public void invalidate( )
  {
    final UIJob job = new UIJob( "invalidating" )
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        if( isDisposed() )
          return Status.CANCEL_STATUS;

        m_invalidateChartJob.reschedule( getClientArea() );
        return Status.OK_STATUS;
      }
    };

    job.setSystem( true );
    job.setUser( false );
    job.schedule();
  }

  protected IStatus doInvalidateChart( final Rectangle panel )
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

    final ChartPainter chartPainter = new ChartPainter( model, panel, m_insets );// ,new Insets(25,25,25,25));
    m_plotRect = RectangleUtils.inflateRect( panel, chartPainter.getPlotInsets() );
    m_image = chartPainter.createImage( m_panOffset );

    new UIJob( "null" )
    {

      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        redraw();

        return Status.OK_STATUS;
      }
    }.schedule();

    return Status.OK_STATUS;
  }

  protected synchronized void handlePaint( final PaintEvent paintEvent )
  {
    if( m_image == null )
      return;

    final GC gc = paintEvent.gc;
    gc.drawImage( m_image, 0, 0 );// -m_panOffset.x, -m_panOffset.y );

    final Transform oldTransform = new Transform( gc.getDevice() );
    final Transform newTransform = new Transform( gc.getDevice() );

    try
    {
      gc.getTransform( oldTransform );
      gc.getTransform( newTransform );

      // FIXME: makes no sense: why is there a transformation for the drag area and edit info?
      // Same for handlers: why should they paint with an active transformation?
      newTransform.translate( m_plotRect.x, m_plotRect.y );
      gc.setTransform( newTransform );

      paintDragArea( gc );
      paintEditInfo( gc );

      final IChartHandlerManager manager = getPlotHandler();
      final IChartHandler[] handlers = manager.getActiveHandlers();
      for( final IChartHandler handler : handlers )
        handler.paintControl( paintEvent );
    }
    finally
    {
      gc.setTransform( oldTransform );

      oldTransform.dispose();
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

  private void registerListener( )
  {
    if( m_model == null )
      return;
    m_model.getLayerManager().addListener( m_layerEventListener );
    m_model.getMapperRegistry().addListener( m_mapperListener );

  }

  public void setChartModel( final IChartModel model )
  {
    setChartModel( m_model, model );
  }

  protected final void setChartModel( final IChartModel oldModel, final IChartModel newModel )
  {
    if( m_model != null )
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
    {
      m_panOffset = new Point( 0, 0 );
      return;
    }
    m_panOffset = new Point( end.x - start.x, end.y - start.y );
    invalidate();
  }

  protected final void unregisterListener( )
  {
    if( m_model == null )
      return;

    m_model.getLayerManager().removeListener( m_layerEventListener );
    m_model.getMapperRegistry().removeListener( m_mapperListener );
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
