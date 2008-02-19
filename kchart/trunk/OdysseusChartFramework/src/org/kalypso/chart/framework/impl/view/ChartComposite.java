package org.kalypso.chart.framework.impl.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.kalypso.chart.framework.impl.model.event.AbstractChartModelEventListener;
import org.kalypso.chart.framework.impl.model.event.AbstractMapperRegistryEventListener;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.contribs.eclipse.swt.widgets.SizedComposite;

/**
 * @author burtscher Chart widget; parent for AxisComponent and Plot also acts as LayerManager and contains the
 *         AxisRegistry;
 */
public class ChartComposite extends Canvas
{

  /** axis pos --> axis placeholder */
  private final Map<POSITION, Composite> m_axisPlaces = new HashMap<POSITION, Composite>();

  /** drawing space for the plot itself */
  private PlotCanvas m_plot;

  private final Color m_bgColor;

  // private final Color m_black;

  private final IChartModel m_model;

  private final AbstractMapperRegistryEventListener m_mapperListener;

  private final AbstractChartModelEventListener m_chartModelListener;

  public ChartComposite( final Composite parent, final int style, IChartModel model, RGB backgroundRGB )
  {
    super( parent, style | SWT.DOUBLE_BUFFERED | SWT.NO_REDRAW_RESIZE );
    m_model = model;

    m_bgColor = new Color( parent.getDisplay(), backgroundRGB );

    super.setBackground( m_bgColor );

    createControl( this );

    m_mapperListener = new AbstractMapperRegistryEventListener()
    {
      /**
       * @see org.kalypso.chart.framework.axis.IMapperRegistryEventListener#onMapperAdded(org.kalypso.chart.framework.axis.IAxis)
       *      adds an AxisComponent for any newly added axis and reports Axis and its AxisComponent to the AxisRegistry
       */
      @Override
      public void onMapperAdded( final IMapper< ? , ? > mapper )
      {
        if( mapper instanceof org.kalypso.chart.framework.model.mapper.IAxis )
        {
          final IAxis< ? > axis = (IAxis< ? >) mapper;
          final Composite parent = m_axisPlaces.get( axis.getPosition() );
          final AxisCanvas component = new AxisCanvas( axis, parent, SWT.NONE );
          component.setBackground( m_bgColor );
          m_model.getMapperRegistry().setComponent( axis, component );
          layout();
        }
      }

      /**
       * @see org.kalypso.chart.framework.axis.IMapperRegistryEventListener#onMapperRemoved(org.kalypso.chart.framework.axis.IAxis)
       *      TODO: not implemented yet (or is it? - right now there's no way to remove an axis, so this should be
       *      checked in the future)
       */
      @Override
      public void onMapperRemoved( final IMapper mapper )
      {
        if( mapper instanceof org.kalypso.chart.framework.model.mapper.IAxis )
        {
          layout();
        }
      }

      /**
       * @see org.kalypso.chart.framework.impl.model.event.AbstractMapperRegistryEventListener#onMapperRangeChanged(org.kalypso.chart.framework.model.mapper.IMapper)
       */
      @Override
      public void onMapperRangeChanged( IMapper mapper )
      {

        layout();
        if( mapper instanceof IAxis )
        {
          IAxis axis = (IAxis) mapper;

          IChartLayer[] changedLayers = (IChartLayer[]) getModel().getAxis2Layers().get( axis ).toArray( new IChartLayer[] {} );
          m_plot.invalidate( changedLayers );

          AxisCanvas ac = (AxisCanvas) m_model.getMapperRegistry().getComponent( axis );
          ac.layout();
        }
        redraw();
      }

    };

    m_model.getMapperRegistry().addListener( m_mapperListener );

    m_chartModelListener = new AbstractChartModelEventListener()
    {
      /**
       * @see org.kalypso.chart.framework.model.event.IChartModelEventListener#onAutoscale()
       */
      @Override
      public void onModelChanged( )
      {
        layout();
        redraw();
      }
    };

    m_model.addListener( m_chartModelListener );

    final IMapperRegistry ar = m_model.getMapperRegistry();
    final IAxis[] axes = ar.getAxes();

    // Falls das Model schon gef�llt ist, m�ssen den vorhandenen Achsen noch Components hinzugef�gt werden
    for( final IAxis axis : axes )
    {
      m_mapperListener.onMapperAdded( axis );
    }
  }

  public IChartModel getModel( )
  {
    return m_model;
  }

  /**
   * creates components for all 4 AxisComponent (TOP, RIGHT, BOTTOM, TOP) and Plot
   */
  private final void createControl( final Composite parent )
  {
    final GridLayout gridLayout = new GridLayout( 3, false );
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    super.setLayout( gridLayout );
    setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final int axisContainerStyle = SWT.DOUBLE_BUFFERED;

    // 1.1 - wird ins erste Feld der ersten Zeile gef�llt
    final Label lbl11 = new Label( parent, SWT.SHADOW_ETCHED_IN );
    lbl11.setSize( 0, 0 );
    lbl11.setVisible( false );

    // 1.2
    final Composite topAxes = new SizedComposite( parent, axisContainerStyle );
    topAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData tad = new GridData( SWT.FILL, SWT.NONE, true, false );
    topAxes.setLayoutData( tad );
    topAxes.setBackground( m_bgColor );
    m_axisPlaces.put( POSITION.TOP, topAxes );

    // 1.3 - wird ins letzte Feld der ersten Zeile gef�llt
    final Label lbl13 = new Label( parent, SWT.NONE );
    lbl13.setSize( 0, 0 );
    lbl13.setVisible( false );

    // 2.1
    final Composite leftAxes = new SizedComposite( parent, axisContainerStyle );
    leftAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData lad = new GridData( SWT.NONE, SWT.FILL, false, true );
    leftAxes.setLayoutData( lad );
    leftAxes.setBackground( m_bgColor );
    m_axisPlaces.put( POSITION.LEFT, leftAxes );

    // 2.2
    // m_plot = new PlotCanvas( m_model.getLayerManager(), parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED |
    // SWT.NO_FOCUS );
    m_plot = new PlotCanvas( m_model.getLayerManager(), parent, SWT.LEFT_TO_RIGHT );
    m_plot.setLayout( new FillLayout() );
    final GridData pad = new GridData( SWT.FILL, SWT.FILL, true, true );
    m_plot.setLayoutData( pad );
    m_plot.setBackground( m_bgColor );

    // 2.3
    final Composite rightAxes = new SizedComposite( parent, axisContainerStyle );
    rightAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData rad = new GridData( SWT.NONE, SWT.FILL, false, true );
    rightAxes.setLayoutData( rad );
    rightAxes.setBackground( m_bgColor );
    m_axisPlaces.put( POSITION.RIGHT, rightAxes );

    // 3.1 - wird ins erste Feld der letzten Zeile gef�llt
    final Label lbl31 = new Label( parent, SWT.NONE );
    lbl31.setSize( 0, 0 );
    lbl31.setVisible( false );

    // 3.2
    final Composite bottomAxes = new SizedComposite( parent, axisContainerStyle );
    bottomAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData bad = new GridData( SWT.FILL, SWT.NONE, true, false );
    bottomAxes.setLayoutData( bad );
    leftAxes.setBackground( m_bgColor );
    m_axisPlaces.put( POSITION.BOTTOM, bottomAxes );

    // 3.3 - wird ins letzte Feld der lezten Zeile gef�llt
    final Label lbl33 = new Label( parent, SWT.NONE );
    lbl33.setSize( 0, 0 );
    lbl33.setVisible( false );
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    m_model.getMapperRegistry().removeListener( m_mapperListener );

    m_model.clear();

    m_axisPlaces.clear();

    super.dispose();

    m_bgColor.dispose();
  }

  /**
   * No Layout can be set on this chart. It manages its children and the layout on its own.
   *
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( final Layout layout )
  {
    checkWidget();
  }

  public PlotCanvas getPlot( )
  {
    return m_plot;
  }

  /**
   * repaints all paintable elements (which are: Plot and AxisComponents)
   */
  @Override
  public void redraw( )
  {
    // layout();

    final IMapperRegistry mapperRegistry = m_model.getMapperRegistry();
    final IAxis[] axes = mapperRegistry.getAxes();
    for( final IAxis axis : axes )
    {
      /*
       * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisComponent gecastet werden - sonst gibts keinen
       * Zugriff auf die Paint-Sachen
       */
      final AxisCanvas ac = (AxisCanvas) mapperRegistry.getComponent( axis );
      ac.redraw();
    }
    m_plot.redraw();
  }

  /**
   * resizes the chart according to a given plot size;
   */
  public void setPlotSize( int width, int height )
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

  public void setHideUnusedAxes( boolean hide )
  {
    m_model.setHideUnusedAxes( hide );
    redraw();
  }

}
