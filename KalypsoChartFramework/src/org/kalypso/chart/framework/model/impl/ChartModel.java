package org.kalypso.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.event.IChartModelEventListener;
import org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import org.kalypso.chart.framework.model.event.impl.ChartModelEventHandler;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.layer.impl.LayerManager;
import org.kalypso.chart.framework.model.mapper.AxisAdjustment;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.registry.impl.MapperRegistry;
import org.kalypso.chart.framework.util.ChartUtilities;

public class ChartModel implements IChartModel
{
  private boolean m_hideUnusedAxes = false;

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  /** axis --> List of layers */
  private final Map<IAxis< ? >, List<IChartLayer< ? , ? >>> m_axis2Layers = new HashMap<IAxis< ? >, List<IChartLayer< ? , ? >>>();

  private final ILayerManager m_manager = new LayerManager();

  /**
   * if set to true, all axes are sized automatically to fit all data into a layer
   */
  private boolean m_autoscale = false;

  private final ChartModelEventHandler m_eventHandler;

  private String m_id = "";

  private String m_title = "";

  private String m_description = "";

  public ChartModel( )
  {
    final AbstractLayerManagerEventListener m_layerMana = new AbstractLayerManagerEventListener()
    {
      /*
       * (non-Javadoc)
       * 
       * @see org.kalypso.chart.framework.model.IChartModel#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( final IChartLayer< ? , ? > layer )
      {
        updateAxisLayerMap( layer, true );
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.kalypso.chart.framework.model.IChartModel#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerRemoved( final IChartLayer< ? , ? > layer )
      {
        updateAxisLayerMap( layer, false );
      }

    };
    m_manager.addListener( m_layerMana );

    m_eventHandler = new ChartModelEventHandler();
  }

  /**
   * adds layers to or removes layers from the chart
   * 
   * @param bAdding
   *            if true, the layer will be added; if false, the layer will be removed
   */
  protected void updateAxisLayerMap( final IChartLayer< ? , ? > layer, final boolean bAdding )
  {
    List<IChartLayer< ? , ? >> domList = m_axis2Layers.get( layer.getDomainAxis() );
    List<IChartLayer< ? , ? >> valList = m_axis2Layers.get( layer.getTargetAxis() );

    if( bAdding )
    {
      // mapping for domain axis
      if( domList == null )
      {
        domList = new ArrayList<IChartLayer< ? , ? >>();
        m_axis2Layers.put( layer.getDomainAxis(), domList );
      }
      domList.add( layer );

      // mapping for value axis
      if( valList == null )
      {
        valList = new ArrayList<IChartLayer< ? , ? >>();
        m_axis2Layers.put( layer.getTargetAxis(), valList );
      }
      valList.add( layer );

      // axis-components must be visible

      final IAxisComponent domainComp = m_mapperRegistry.getComponent( layer.getDomainAxis() );
      if( domainComp != null )
        domainComp.setVisible( true );
      final IAxisComponent valueComp = m_mapperRegistry.getComponent( layer.getTargetAxis() );
      if( valueComp != null )
        valueComp.setVisible( true );
      // m_mapperRegistry.getComponent( layer.getValueAxis() ).setVisible( true );

    }
    else
    {
      // remove domain mapping
      if( domList != null )
        domList.remove( layer );

      // remove value mapping
      if( valList != null )
        valList.remove( layer );

      // eventually hide axes
      if( m_hideUnusedAxes )
      {
        if( domList == null || domList.size() == 0 )
          m_mapperRegistry.getComponent( layer.getDomainAxis() ).setVisible( false );
        if( valList == null || valList.size() == 0 )
          m_mapperRegistry.getComponent( layer.getTargetAxis() ).setVisible( false );
      }
    }

    if( m_autoscale )
      autoscale( new IAxis[] { layer.getDomainAxis(), layer.getTargetAxis() } );

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.IChartModel#getAxisRegistry()
   */
  public IMapperRegistry getMapperRegistry( )
  {
    return m_mapperRegistry;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.IChartModel#getLayerManager()
   */
  public ILayerManager getLayerManager( )
  {
    return m_manager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.IChartModel#clear()
   */
  public void clear( )
  {
    m_axis2Layers.clear();
    m_manager.clear();
    m_mapperRegistry.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.IChartModel#getAxis2Layers()
   */
  public Map<IAxis< ? >, List<IChartLayer< ? , ? >>> getAxis2Layers( )
  {
    return m_axis2Layers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
   */
  public void setHideUnusedAxes( final boolean b )
  {
    m_hideUnusedAxes = b;

    final IAxis< ? >[] axes = m_mapperRegistry.getAxes();
    for( final IAxis< ? > axis : axes )
    {
      final List<IChartLayer< ? , ? >> list = m_axis2Layers.get( axis );
      if( list == null || list.size() == 0 )
      {
        final IAxisComponent comp = m_mapperRegistry.getComponent( axis );
        comp.setVisible( !m_hideUnusedAxes );
      }
    }
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @SuppressWarnings("unchecked")
  public void autoscale( IAxis[] axes )
  {
    if( axes == null )
      axes = getMapperRegistry().getAxes();

    for( final IAxis axis : axes )
    {
      final List<IChartLayer< ? , ? >> layers = getAxis2Layers().get( axis );
      if( layers == null )
        continue;

      final List<IDataRange> ranges = new ArrayList<IDataRange>( layers.size() );

      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() )
        {
          final IDataRange range = getRangeFor( layer, axis );
          if( range != null )
            ranges.add( range );
        }
      }
      IDataRange mergedDataRange = ChartUtilities.mergeDataRanges( ranges.toArray( new IDataRange[ranges.size()] ), axis.getDataOperator().getComparator() );

      if( mergedDataRange == null )
      {
        // if mergedDataRange is null, we keep the old range - if there is any
        if( axis.getLogicalRange() != null )
          continue;
        else
          // otherwise, we use a default range
          mergedDataRange = axis.getDataOperator().getDefaultRange();
      }

      // first: set logical range; we have to do this right now as we get logical ranges out of layers / data containers
      axis.setLogicalRange( mergedDataRange );
      // now check if axis has a preferred Adjustment
      final AxisAdjustment adj = axis.getPreferredAdjustment();
      if( adj != null )
      {
        final double adjBefore = adj.getBefore();
        final double adjRange = adj.getRange();
        final double adjAfter = adj.getAfter();

        final IDataRange<Number> numericRange = axis.getNumericRange();
        final double rangeMax = numericRange.getMax().doubleValue();
        final double rangeMin = numericRange.getMin().doubleValue();
        final double rangeSize = rangeMax - rangeMin;

        final double newMin = rangeMin - rangeSize * (adjBefore / adjRange);
        final double newMax = rangeMax + rangeSize * (adjAfter / adjRange);

        axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { newMin, newMax } ) );

      }
    }

  }

  /**
   * @return DataRange of all domain or target data available in the given layer
   */
  @SuppressWarnings("unchecked")
  private IDataRange getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    final IDataContainer dc = layer.getDataContainer();
    if( dc == null )
      return null;
    else
    {
      if( axis == layer.getDomainAxis() )
        return dc.getDomainRange();
      else if( axis == layer.getTargetAxis() )
        return dc.getTargetRange();
      // letzer Fall: Achse hat nix mit Layer zu tun
      else
        return null;
    }

  }

  /**
   * sets autoscaling
   * 
   * @param b
   *            if true, axes are automatically scaled to show the layers full data range
   */
  public void setAutoscale( final boolean b )
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
    final IAxis< ? >[] axes = getMapperRegistry().getAxes();
    autoscale( axes );
    // ModelChangedEvent werfen, damit Composite das Model neu zeichnet
  }

  /**
   * @see org.kalypso.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  public void addListener( final IChartModelEventListener listener )
  {
    m_eventHandler.addListener( listener );
  }

  /**
   * @see org.kalypso.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  public void removeListener( final IChartModelEventListener listener )
  {
    m_eventHandler.removeListener( listener );
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#getId()
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#setId()
   */
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#setDescription(java.lang.String)
   */
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#setTitle(java.lang.String)
   */
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * Maximizes the content of the plot to the values inside a dragged rectangle
   */
  @SuppressWarnings("unchecked")
  public <T_logical> void zoomIn( final Point start, final Point end )
  {
    final IMapperRegistry ar = getMapperRegistry();
    final IAxis[] axes = ar.getAxes();
    for( final IAxis axis : axes )
    {
      Number from = null;
      Number to = null;

      switch( axis.getPosition().getOrientation() )
      {
        case HORIZONTAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.min( start.x, end.x ) );
              to = axis.screenToNumeric( Math.max( start.x, end.x ) );
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.max( start.x, end.x ) );
              to = axis.screenToNumeric( Math.min( start.x, end.x ) );
              break;
          }
          break;

        case VERTICAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.max( start.y, end.y ) );
              to = axis.screenToNumeric( Math.min( start.y, end.y ) );
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.min( start.y, end.y ) );
              to = axis.screenToNumeric( Math.max( start.y, end.y ) );
              break;
          }
          break;
      }

      if( from != null && to != null )
      {
        axis.setNumericRange( new ComparableDataRange( new Number[] { from, to } ) );
      }
    }
    m_eventHandler.fireModelChanged();
  }

  /**
   * minimizes the content of the plot to the values inside a dragged rectangle
   */
  @SuppressWarnings("unchecked")
  public <T_logical> void zoomOut( final Point start, final Point end )
  {
    final IMapperRegistry ar = getMapperRegistry();
    final IAxis[] axes = ar.getAxes();
    for( final IAxis axis : axes )
    {
      double from = Double.NaN;
      double to = Double.NaN;

      switch( axis.getPosition().getOrientation() )
      {
        case HORIZONTAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.min( start.x, end.x ) ).doubleValue();
              to = axis.screenToNumeric( Math.max( start.x, end.x ) ).doubleValue();
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.max( start.x, end.x ) ).doubleValue();
              to = axis.screenToNumeric( Math.min( start.x, end.x ) ).doubleValue();
              break;
          }
          break;

        case VERTICAL:
          switch( axis.getDirection() )
          {
            case POSITIVE:
              from = axis.screenToNumeric( Math.max( start.y, end.y ) ).doubleValue();
              to = axis.screenToNumeric( Math.min( start.y, end.y ) ).doubleValue();
              break;

            case NEGATIVE:
              from = axis.screenToNumeric( Math.min( start.y, end.y ) ).doubleValue();
              to = axis.screenToNumeric( Math.max( start.y, end.y ) ).doubleValue();
              break;
          }
          break;
      }

      if( from != Double.NaN && to != Double.NaN )
      {
        final IDataRange<Number> numericRange = axis.getNumericRange();

        final double oldmin = numericRange.getMin().doubleValue();
        final double oldmax = numericRange.getMax().doubleValue();
        final double oldrange = Math.abs( oldmin - oldmax );
        final double mouserange = Math.abs( from - to );
        final double newrange = (oldrange / mouserange) * oldrange;

        final double newFrom = oldmin - ((Math.abs( from - oldmin ) / oldrange) * newrange);
        final double newTo = oldmax + ((Math.abs( to - oldmax ) / oldrange) * newrange);

        axis.setNumericRange( new ComparableDataRange( new Number[] { new Double( newFrom ), new Double( newTo ) } ) );
      }
    }
    // m_eventHandler.fireModelChanged();
  }

  public void panTo( final Point start, final Point end )

  {
    final IAxis< ? >[] axes = getMapperRegistry().getAxes();
    for( final IAxis< ? > axis : axes )
    {
      double newmin = 0;
      double newmax = 0;

      Number nowNum;
      Number startNum;
      if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      {
        nowNum = axis.screenToNumeric( end.x );
        startNum = axis.screenToNumeric( start.x );
      }
      else
      {
        nowNum = axis.screenToNumeric( end.y );
        startNum = axis.screenToNumeric( start.y );
      }
      final double diff = startNum.doubleValue() - nowNum.doubleValue();
      final IDataRange<Number> initRange = axis.getNumericRange();
      newmin = initRange.getMin().doubleValue() + diff;
      newmax = initRange.getMax().doubleValue() + diff;

      final IDataRange<Number> newRange = new ComparableDataRange<Number>( new Number[] { new Double( newmin ), new Double( newmax ) } );
      axis.setNumericRange( newRange );

    }
    // m_eventHandler.fireModelChanged();
  }

}
