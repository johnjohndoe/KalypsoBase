package de.openali.diagram.framework.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.kalypso.contribs.eclipse.swt.widgets.SizedComposite;

import de.openali.diagram.framework.exception.ZeroSizeDataRangeException;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.data.IDataRange;
import de.openali.diagram.framework.model.data.impl.DataRange;
import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.layer.ILayerManagerEventListener;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistryEventListener;

/**
 * @author burtscher Chart widget; parent for AxisComponent and Plot also acts as LayerManager and contains the
 *         AxisRegistry;
 */
public class ChartComposite extends Composite implements IMapperRegistryEventListener, ILayerManagerEventListener
{
  
  /** axis pos --> axis placeholder */
  private final Map<POSITION, Composite> m_axisPlaces = new HashMap<POSITION, Composite>();

  /** drawing space for the plot itself */
  private PlotCanvas m_plot;

  private boolean m_autoscale = false;



  private final Color m_bgColor;

  //private final Color m_black;

  private final IDiagramModel m_model;


  public ChartComposite( final Composite parent, final int style, IDiagramModel model, RGB backgroundRGB )
  {
    super( parent, style );
	m_model = model;
	
    m_bgColor = new Color( parent.getDisplay(), backgroundRGB );

    createControl( this);


    m_model.getAxisRegistry().addMapperRegistryEventListener( this );
    m_model.getLayerManager().addLayerManagerEventListener( this ); 
    
    IMapperRegistry ar=m_model.getAxisRegistry();
    IAxis[] axes = ar.getAxes();
    
    //Falls das Model schon gefüllt ist, müssen den vorhandenen Achsen noch Components hinzugefügt werden
    for (IAxis axis : axes) 
    {
    	onMapperAdded(axis);	
	}
    
  }

  
  public IDiagramModel getModel()
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

    final int axisContainerStyle = SWT.NONE;

    // 1.1 - wird ins erste Feld der ersten Zeile gefüllt
    final Label lbl11 = new Label( parent, SWT.NONE );
    lbl11.setSize( 0, 0 );
    lbl11.setVisible( false );

    // 1.2
    final Composite topAxes = new SizedComposite( parent, axisContainerStyle );
    topAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData tad = new GridData( SWT.FILL, SWT.NONE, true, false );
    topAxes.setLayoutData( tad );
    m_axisPlaces.put( POSITION.TOP, topAxes );

    // 1.3 - wird ins letzte Feld der ersten Zeile gefüllt
    final Label lbl13 = new Label( parent, SWT.NONE );
    lbl13.setSize( 0, 0 );
    lbl13.setVisible( false );

    // 2.1
    final Composite leftAxes = new SizedComposite( parent, axisContainerStyle );
    leftAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData lad = new GridData( SWT.NONE, SWT.FILL, false, true );
    leftAxes.setLayoutData( lad );
    m_axisPlaces.put( POSITION.LEFT, leftAxes );

    // 2.2
    m_plot = new PlotCanvas( m_model.getLayerManager(), parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED | SWT.NO_FOCUS );
    m_plot.setLayout( new FillLayout() );
    final GridData pad = new GridData( SWT.FILL, SWT.FILL, true, true );
    m_plot.setLayoutData( pad );
    m_plot.setBackground( m_bgColor );

    // 2.3
    final Composite rightAxes = new SizedComposite( parent, axisContainerStyle );
    rightAxes.setLayout( new FillLayout( SWT.HORIZONTAL ) );
    final GridData rad = new GridData( SWT.NONE, SWT.FILL, false, true );
    rightAxes.setLayoutData( rad );
    m_axisPlaces.put( POSITION.RIGHT, rightAxes );

    // 3.1 - wird ins erste Feld der letzten Zeile gefüllt
    final Label lbl31 = new Label( parent, SWT.NONE );
    lbl31.setSize( 0, 0 );
    lbl31.setVisible( false );

    // 3.2
    final Composite bottomAxes = new SizedComposite( parent, axisContainerStyle );
    bottomAxes.setLayout( new FillLayout( SWT.VERTICAL ) );
    final GridData bad = new GridData( SWT.FILL, SWT.NONE, true, false );
    bottomAxes.setLayoutData( bad );
    m_axisPlaces.put( POSITION.BOTTOM, bottomAxes );

    // 3.3 - wird ins letzte Feld der lezten Zeile gefüllt
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
    m_model.getAxisRegistry().removeMapperRegistryEventListener( this );
    m_model.getLayerManager().removeLayerManagerEventListener( this );

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



  /**
   * @see de.openali.diagram.framework.axis.IMapperRegistryEventListener#onMapperAdded(de.openali.diagram.framework.axis.IAxis) adds an
   *      AxisComponent for any newly added axis and reports Axis and its AxisComponent to the AxisRegistry
   */
  @SuppressWarnings("unchecked")
public void onMapperAdded( final IMapper mapper)
  {
	if (mapper instanceof de.openali.diagram.framework.model.mapper.IAxis)
	{
		IAxis axis=(IAxis) mapper; 
	    final Composite parent = m_axisPlaces.get( axis.getPosition() );
	    final AxisCanvas component = new AxisCanvas( axis, parent, SWT.NONE );
	    component.setBackground(m_bgColor);
	    m_model.getAxisRegistry().setComponent( axis, component );
	    layout();
	}
  }

  /**
   * @see de.openali.diagram.framework.axis.IMapperRegistryEventListener#onMapperRemoved(de.openali.diagram.framework.axis.IAxis) TODO: not
   *      implemented yet (or is it? - right now there's no way to remove an axis, so this should be checked in the
   *      future)
   */
  public void onMapperRemoved( final IMapper mapper)
  {
	if (mapper instanceof de.openali.diagram.framework.model.mapper.IAxis)
	{
	  layout();
	}
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @SuppressWarnings("unchecked")
private void autoscale( IAxis[] axes )
  {
    if( axes == null )
      axes = m_model.getAxisRegistry().getAxes();

    for( final IAxis axis : axes )
    {
      final List<IChartLayer> layers = m_model.getAxis2Layers().get( axis );
      if( layers == null )
        continue;

      final List<IDataRange> ranges = new ArrayList<IDataRange>( layers.size() );

      for( final IChartLayer layer : layers )
      {
    	  if (layer.getVisibility())
    	  {
	        final IDataRange range = getRangeFor( layer, axis );
	        if( range != null )
	          ranges.add( range );
    	  }
      }
      axis.autorange( ranges.toArray( new IDataRange[ranges.size()] ) );
    }
  }

  /**
   * @return DataRange of all domain or value data available in the given layer
   */
  private IDataRange getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    if( axis == layer.getDomainAxis() )
      return layer.getDomainRange();
    else if( axis == layer.getTargetAxis() )
      return layer.getTargetRange();
    else
      return null;
  }



  /**
   * sets autoscaling
   * 
   * @param b
   *          if true, axes are automatically scaled to show the layers full data range
   */
  public void setAutoscale( boolean b )
  {
    m_autoscale = b;

    if( m_autoscale )
      autoscale( null );
  }


  /**
   * maximises the chart view - that means all the available data of all layers is shown
   */
  public void maximize( )
  {
    final IAxis[] axes = getModel().getAxisRegistry().getAxes();
    autoscale( axes );
    redraw();
  }


  public PlotCanvas getPlot( )
  {
    return m_plot;
  }

  /**
   * repaints all paintable elements (which are: Plot and AxisComponents)
   */
  public void redraw( )
  {
    m_plot.redraw();

    final IMapperRegistry mapperRegistry = m_model.getAxisRegistry();
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
  }

  /**
   * resizes the chart according to a given plot size;
   */
  public void setPlotSize( int width, int height )
  {
    Composite l = m_axisPlaces.get( POSITION.LEFT );
    Composite r = m_axisPlaces.get( POSITION.RIGHT );
    Composite t = m_axisPlaces.get( POSITION.TOP );
    Composite b = m_axisPlaces.get( POSITION.BOTTOM );

    // setSize( width, height );
    // TODO: check if border-width must be included
    int chartwidth = width + l.getBounds().width + r.getBounds().width + (2 * l.getBorderWidth()) + (2 * r.getBorderWidth());
    int chartheight = height + b.getBounds().height + t.getBounds().height + (2 * b.getBorderWidth()) + (2 * t.getBorderWidth());

    // getParent().setSize( chartwidth, chartheight );
    setSize( chartwidth, chartheight );
    layout();

    /**
     * Leider funktioniert die obige Vergrößerung nicht problemlos, es muss daher noch etwas handarbeit erfolgen; TODO:
     * überprüfen, ob es da einen besseren Weg geben könnte
     */

    // Vergrössern, falls zu klein
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

public void onLayerAdded(IChartLayer layer) {
	 if( m_autoscale )
	      autoscale( new IAxis[] { layer.getDomainAxis(), layer.getTargetAxis() } );
	    redraw();
}

public void onLayerRemoved(IChartLayer layer) {
	if( m_autoscale )
	      autoscale( null );
	redraw();
}

	public void setHideUnusedAxes(boolean hide)
	{
		m_model.setHideUnusedAxes( hide );
		redraw();
	}



/**
 * maximises the content of the plot to the values inside a dragged rectangle
 */
@SuppressWarnings("unchecked")
public <T_logical extends Comparable> void zoomIn( final Point start, final Point end )
{
  final IMapperRegistry ar = m_model.getAxisRegistry();
  final IAxis[] axes = ar.getAxes();
  for( IAxis axis : axes )
  {
  	T_logical from = null;
  	T_logical to = null;

    switch( axis.getPosition().getOrientation() )
    {
      case HORIZONTAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = (T_logical) axis.screenToLogical( Math.min( start.x, end.x ) );
            to = (T_logical) axis.screenToLogical( Math.max( start.x, end.x ) );
            break;

          case NEGATIVE:
            from = (T_logical) axis.screenToLogical( Math.max( start.x, end.x ) );
            to = (T_logical) axis.screenToLogical( Math.min( start.x, end.x ) );
            break;
        }
        break;

      case VERTICAL:
        switch( axis.getDirection() )
        {
          case POSITIVE:
            from = (T_logical) axis.screenToLogical( Math.max( start.y, end.y ) );
            to = (T_logical) axis.screenToLogical( Math.min( start.y, end.y ) );
            break;

          case NEGATIVE:
            from = (T_logical) axis.screenToLogical( Math.min( start.y, end.y ) );
            to = (T_logical) axis.screenToLogical( Math.max( start.y, end.y ) );
            break;
        }
        break;
    }

    if( from != null && to != null )
    {
      try
	{
		axis.setDataRange( new DataRange(from, to) );
	} catch (ZeroSizeDataRangeException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

      /*
       * zum Neuzeichnen der Achse muss nach die IAxisComponent nach AxisCanvas gecastet werden - sonst gibts
       * keinen Zugriff auf die Paint-Sachen
       */
      AxisCanvas ac = (AxisCanvas) ar.getComponent( axis );
      ac.redraw();
    }
  }
  redraw();
}

}
