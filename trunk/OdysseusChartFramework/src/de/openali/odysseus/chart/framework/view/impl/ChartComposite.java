package de.openali.odysseus.chart.framework.view.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.swt.widgets.SizedComposite;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * @author burtscher Chart widget; parent for AxisComponent and Plot also acts as LayerManager and contains the
 *         AxisRegistry;
 */
public class ChartComposite extends Canvas
{
  /** axis pos --> axis placeholder */
  protected final Map<POSITION, Composite> m_axisPlaces = new HashMap<POSITION, Composite>();

  protected PlotCanvas m_plot;

  protected IChartModel m_model;

  private final class InvalidateChartJob extends UIJob
  {
    public InvalidateChartJob( String name )
    {
      super( name );
    }

// protected final Set<IChartLayer> m_layer = Collections.synchronizedSet( new HashSet<IChartLayer>() );
//
// public void addLayer( final IChartLayer[] layers )
// {
// m_layer.addAll( Arrays.asList( layers ) );
// }

    /**
     * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread( IProgressMonitor monitor )
    {
      // TODO: only invalidate if necessary
      // final IChartLayer[] layers = m_layer.toArray( new IChartLayer[] {} );
      m_plot.invalidate( null );
      // m_layer.removeAll( Arrays.asList( layers ) );
      return Status.OK_STATUS;
    }
  }

  private final InvalidateChartJob m_invalidateChartJob = new InvalidateChartJob( "" );

  private final ILayerManagerEventListener m_layerEventListener = new ILayerManagerEventListener()
  {
// TODO: eigener Job für invalidate, mit cancel wenn zu viele events
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

      invalidatePlotCanvas( new IChartLayer[] { layer } );
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerContentChanged( final IChartLayer layer )
    {
      invalidatePlotCanvas( new IChartLayer[] { layer } );
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerMoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerMoved( final IChartLayer layer )
    {
      invalidatePlotCanvas( new IChartLayer[] { layer } );
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerRemoved(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerRemoved( final IChartLayer layer )
    {
      invalidatePlotCanvas( new IChartLayer[] { layer } );
    }

    /**
     * @see de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer )
    {
      invalidatePlotCanvas( new IChartLayer[] { layer } );
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
      if( mapper instanceof IAxis )
        addAxisInternal( (IAxis) mapper );
    }

    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractMapperRegistryEventListener#onMapperRangeChanged(de.openali.odysseus.chart.framework.model.mapper.IMapper)
     */
    @Override
    public void onMapperChanged( final IMapper mapper )
    {
      if( isDisposed() )
        return;

      if( mapper instanceof IAxis )
      {
        final IAxis axis = (IAxis) mapper;
        final AxisCanvas ac = getAxisCanvas( axis );
        if( ac != null )
        {
          if( axis.isVisible() )
            ac.redraw();
          else
            removeAxisInternal( axis );
        }
        else
        {
          if( axis.isVisible() )
            addAxisInternal( (IAxis) mapper );
          else
            return;// do nothing;
        }
        final List<IChartLayer> list = m_model.getAxis2Layers().get( axis );
        if( list != null )
          m_plot.invalidate( list.toArray( new IChartLayer[] {} ) );
      }
    }

    /**
     * @see de.openali.odysseus.chart.framework.axis.IMapperRegistryEventListener#onMapperRemoved(de.openali.odysseus.chart.framework.axis.IAxis)
     *      TODO: not implemented yet (or is it? - right now there's no way to remove an axis, so this should be checked
     *      in the future)
     */
    @Override
    public void onMapperRemoved( final IMapper mapper )
    {
      if( mapper instanceof de.openali.odysseus.chart.framework.model.mapper.IAxis )
      {
        removeAxisInternal( (IAxis) mapper );
      }
    }
  };

  public ChartComposite( final Composite parent, final int style, final IChartModel model, final RGB backgroundRGB )
  {
    super( parent, style | SWT.DOUBLE_BUFFERED );// | SWT.NO_REDRAW_RESIZE );
    addDisposeListener( new DisposeListener()
    {

      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        if( m_model != null )
        {
          unregisterListener();
        }
      }
    } );
    setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( parent.getDisplay(), backgroundRGB ) );
    createControl();
    setChartModel( model );
  }

  protected final AxisCanvas addAxisInternal( final IAxis axis )
  {
    final Composite parent = m_axisPlaces.get( axis.getPosition() );
    final AxisCanvas ac = new AxisCanvas( axis, parent, SWT.DOUBLE_BUFFERED );
    ac.setBackground( getBackground() );
    return ac;
  }

  /**
   * creates components for all 4 AxisComponent (TOP, RIGHT, BOTTOM, TOP) and Plot
   */
  private final void createControl( )
  {
    final GridLayout gridLayout = new GridLayout( 3, false );

    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;

    gridLayout.marginBottom = 5;
    gridLayout.marginLeft = 5;
    gridLayout.marginTop = 5;
    gridLayout.marginRight = 5;

    setLayout( gridLayout );
    setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    // 1.1 - first field of first row
    final Label lbl11 = new Label( this, SWT.NONE );
    lbl11.setSize( 0, 0 );
    lbl11.setVisible( false );

    // 1.2
    final Composite topAxes = new SizedComposite( this, SWT.DOUBLE_BUFFERED );
    topAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData tad = new GridData( SWT.FILL, SWT.NONE, true, false );
    // tad.exclude = true;
    topAxes.setLayoutData( tad );
    topAxes.setBackground( getBackground() );
    m_axisPlaces.put( POSITION.TOP, topAxes );

    // 1.3 - last field of first row
    final Label lbl13 = new Label( this, SWT.NONE );
    lbl13.setSize( 0, 0 );
    lbl13.setVisible( false );

    // 2.1 - first field
    final Composite leftAxes = new SizedComposite( this, SWT.DOUBLE_BUFFERED );
    leftAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData lad = new GridData( SWT.NONE, SWT.FILL, false, true );
    // lad.exclude = true;
    leftAxes.setLayoutData( lad );
    leftAxes.setBackground( getBackground() );
    // );
    m_axisPlaces.put( POSITION.LEFT, leftAxes );

    // 2.2
    m_plot = new PlotCanvas( this, SWT.LEFT_TO_RIGHT | SWT.DOUBLE_BUFFERED );
    m_plot.setLayout( new FillLayout() );
    final GridData pad = new GridData( SWT.FILL, SWT.FILL, true, true );
    // pad.exclude = true;
    m_plot.setLayoutData( pad );
    m_plot.setBackground( getBackground() );

    // 2.3
    final Composite rightAxes = new SizedComposite( this, SWT.DOUBLE_BUFFERED );
    rightAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData rad = new GridData( SWT.NONE, SWT.FILL, false, true );
    // rad.exclude = true;
    rightAxes.setLayoutData( rad );
    rightAxes.setBackground( getBackground() );
    m_axisPlaces.put( POSITION.RIGHT, rightAxes );

    // 3.1 - wird ins erste Feld der letzten Zeile gef�llt
    final Label lbl31 = new Label( this, SWT.NONE );
    lbl31.setSize( 0, 0 );
    lbl31.setVisible( false );

    // 3.2
    final Composite bottomAxes = new SizedComposite( this, SWT.DOUBLE_BUFFERED );
    bottomAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData bad = new GridData( SWT.FILL, SWT.NONE, true, false );
    // bad.exclude = false;
    bottomAxes.setLayoutData( bad );
    leftAxes.setBackground( getBackground() );
    // );
    m_axisPlaces.put( POSITION.BOTTOM, bottomAxes );

    // 3.3 - wird ins letzte Feld der lezten Zeile gef�llt
    final Label lbl33 = new Label( this, SWT.NONE );
    lbl33.setSize( 0, 0 );
    lbl33.setVisible( false );
  }

  // TODO: don't allow others to deal with the private plotCanvas, change Model and wait for events
  public PlotCanvas getPlot( )
  {
    return m_plot;
  }

  public IChartModel getChartModel( )
  {
    return m_model;
  }

  public final AxisCanvas getAxisCanvas( final IAxis axis )
  {
    final Composite axisPlace = m_axisPlaces.get( axis.getPosition() );
    if( axisPlace == null )
      return null;
    for( final AxisCanvas ac : getAxisCanvas( axis.getPosition() ) )
    {
      if( ac.getAxis() == axis )
        return ac;
    }
    return null;
  }

  public final AxisCanvas[] getAxisCanvas( final POSITION position )
  {
    final Composite axisPlace = m_axisPlaces.get( position );
    if( axisPlace == null || axisPlace.getChildren().length == 0 )
      return new AxisCanvas[] {};
    final List<AxisCanvas> acList = new ArrayList<AxisCanvas>();
    for( final Control comp : axisPlace.getChildren() )
    {
      if( comp instanceof AxisCanvas )
        acList.add( (AxisCanvas) comp );
    }
    return acList.toArray( new AxisCanvas[] {} );
  }

  private final void registerListener( )
  {
    m_model.getLayerManager().addListener( m_layerEventListener );
    m_model.getMapperRegistry().addListener( m_mapperListener );
  }

  protected final void removeAxisInternal( final IAxis axis )
  {
    final AxisCanvas ac = getAxisCanvas( axis );
    if( ac != null )
      ac.dispose();
  }

  private final void addAllAxis( )
  {
    final IMapperRegistry mr = m_model.getMapperRegistry();
    if( mr == null )
      return;
    final IAxis[] axes = mr == null ? new IAxis[] {} : mr.getAxes();
    for( final IAxis axis : axes )
      addAxisInternal( axis );
  }

  private final void removeAllAxis( )
  {
    final IMapperRegistry mr = m_model.getMapperRegistry();
    if( mr == null )
      return;
    final IAxis[] axes = mr == null ? new IAxis[] {} : mr.getAxes();
    for( final IAxis axis : axes )
      removeAxisInternal( axis );
  }

  public void setChartModel( final IChartModel model )
  {
    if( m_model != null )
    {
      unregisterListener();
      removeAllAxis();
      m_plot.setLayerManager( null );
    }

    m_model = model;

    if( m_model != null )
    {
      registerListener();
      addAllAxis();

      m_plot.setLayerManager( m_model.getLayerManager() );
      invalidatePlotCanvas( null );
    }
    layout( true, true );

  }

  /**
   * resizes the chart according to a given plot size; only used in ChartImageFactory, so remove this from here into a
   * helper class
   */
  public void setPlotSize( final int width, final int height )
  {
    final Composite l = m_axisPlaces.get( POSITION.LEFT );
    final Composite r = m_axisPlaces.get( POSITION.RIGHT );
    final Composite t = m_axisPlaces.get( POSITION.TOP );
    final Composite b = m_axisPlaces.get( POSITION.BOTTOM );

    // setSize( width, height );
    // TODO: check if border-width must be included
    final int chartwidth = width + l.getBounds().width + r.getBounds().width + (2 * l.getBorderWidth()) + (2 * r.getBorderWidth());
    final int chartheight = height + b.getBounds().height + t.getBounds().height + (2 * b.getBorderWidth()) + (2 * t.getBorderWidth());

    // getParent().setSize( chartwidth, chartheight );
    setSize( chartwidth, chartheight );
    layout();

    /**
     * Leider funktioniert die obige Vergr��erung nicht problemlos, es muss daher noch etwas handarbeit erfolgen; TODO:
     * �berpr�fen, ob es da einen besseren Weg geben k�nnte
     */

    // Vergr�ssern, falls zu klein
    while( m_plot.getBounds().width < width )
      setSize( getBounds().width + 1, getBounds().height );
    while( m_plot.getBounds().height < height )
      setSize( getBounds().width, getBounds().height + 1 );
    // Verkleinern, falls zu gross
    while( m_plot.getBounds().width > width )
      setSize( getBounds().width - 1, getBounds().height );
    while( m_plot.getBounds().height > height )
      setSize( getBounds().width, getBounds().height - 1 );

  }

  protected final void unregisterListener( )
  {
    m_model.getLayerManager().removeListener( m_layerEventListener );
    m_model.getMapperRegistry().removeListener( m_mapperListener );
  }

  private final IChartLayer[] getLayers( final IChartLayer[] layers )
  {
    if( layers != null )
      return layers;
    final ILayerManager layerManager = m_model == null ? null : m_model.getLayerManager();

    return layerManager == null ? new IChartLayer[] {} : m_model.getLayerManager().getLayers();
  }

  final void invalidatePlotCanvas( final IChartLayer[] layers )
  {
    if( isDisposed() )
      return;

    m_invalidateChartJob.cancel();
    // TODO: invalidate only layers with changes
    // m_invalidateChartJob.addLayer( getLayers( layers ) );
    m_invalidateChartJob.schedule( 100 );
  }
}
