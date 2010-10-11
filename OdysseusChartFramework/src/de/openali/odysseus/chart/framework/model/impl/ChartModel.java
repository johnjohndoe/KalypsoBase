package de.openali.odysseus.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.impl.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

public class ChartModel implements IChartModel
{
  private boolean m_hideUnusedAxes = true;

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  // /** axis --> List of layers */
  private final Map<IAxis, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis, List<IChartLayer>>();

  private final ILayerManager m_manager = new LayerManager();

  /**
   * if set to true, all axes are sized automatically to fit all data into a layer
   */
  private final boolean m_autoscale = false;

  private String m_id = "";

  private String m_title = "";

  private String m_description = "";

  public ChartModel( )
  {
    final AbstractLayerManagerEventListener layerManager = new AbstractLayerManagerEventListener()
    {
      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( IChartLayer layer )
      {
        if( isHideUnusedAxes() )
          return;
        hideUnusedAxis( layer.getCoordinateMapper().getTargetAxis() );
      }

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onActivLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onActivLayerChanged( final IChartLayer layer )
      {
        if( !layer.isActive() )
          return;
        for( final IChartLayer cl : getLayerManager().getLayers() )
        {
          if( cl != layer && cl.isActive() )
            cl.setActive( false );
        }
      }

      /*
       * (non-Javadoc)
       * @see
       * org.kalypso.chart.framework.model.IChartModel#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerAdded( final IChartLayer layer )
      {
        updateAxisLayerMap( layer, true );
      }

      /*
       * (non-Javadoc)
       * @see
       * org.kalypso.chart.framework.model.IChartModel#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer
       * )
       */
      @Override
      public void onLayerRemoved( final IChartLayer layer )
      {
        updateAxisLayerMap( layer, false );
      }

    };

    m_manager.addListener( layerManager );
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @Override
  @SuppressWarnings("unchecked")
  public void autoscale( IAxis[] axes )
  {
    if( axes == null )
    {
      axes = getMapperRegistry().getAxes();
    }

    for( final IAxis axis : axes )
    {
      final List<IChartLayer> layers = getAxis2Layers().get( axis );
      if( layers == null )
      {
        continue;
      }

      final List<IDataRange<Number>> ranges = new ArrayList<IDataRange<Number>>( layers.size() );

      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() )
        {
          final IDataRange<Number> range = getRangeFor( layer, axis );
          if( range != null )
          {
            ranges.add( range );
          }
        }
      }
      IDataRange<Number> mergedDataRange = ChartUtilities.mergeDataRanges( ranges.toArray( new IDataRange[ranges.size()] ) );

      if( mergedDataRange == null )
      {
        // if mergedDataRange is null, we keep the old range - if there
        // is any
        if( axis.getNumericRange() != null )
        {
          continue;
        }
        else
        {
          // otherwise, we use a default range
          mergedDataRange = new ComparableDataRange<Number>( new Number[] { 0, 1 } );
        }
      }

      // now check if axis has a preferred Adjustment
      final IAxisAdjustment adj = axis.getPreferredAdjustment();
      if( adj != null )
      {
        final double adjBefore = adj.getBefore();
        final double adjRange = adj.getRange();
        final double adjAfter = adj.getAfter();

        final double rangeMax = mergedDataRange.getMax().doubleValue();
        final double rangeMin = mergedDataRange.getMin().doubleValue();

        // computing preferred adjustment failed if rangesize==0.0, so we set a range minimum depends on adjustment
        final double rangeSize = rangeMax == rangeMin ? 1.0 : rangeMax - rangeMin;
        final double newMin = rangeMin - rangeSize * (adjBefore / adjRange);
        final double newMax = rangeMax + rangeSize * (adjAfter / adjRange);

        axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { newMin, newMax } ) );
      }
      else
      {
        axis.setNumericRange( mergedDataRange );
      }
    }

  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#clear()
   */
  @Override
  public void clear( )
  {
    m_axis2Layers.clear();
    m_manager.clear();
    m_mapperRegistry.clear();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#dispose()
   */
  @Override
  public void dispose( )
  {
    // nothing to dispose

  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#getAxis2Layers()
   */
  @Override
  public Map<IAxis, List<IChartLayer>> getAxis2Layers( )
  {
    return m_axis2Layers;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getId()
   */
  @Override
  public String getId( )
  {
    return m_id;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#getLayerManager()
   */
  @Override
  public ILayerManager getLayerManager( )
  {
    return m_manager;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#getAxisRegistry()
   */
  @Override
  public IMapperRegistry getMapperRegistry( )
  {
    return m_mapperRegistry;
  }

  /**
   * @return DataRange of all domain or target data available in the given layer
   */
  private IDataRange<Number> getRangeFor( final IChartLayer layer, final IAxis axis )
  {
    if( axis == layer.getCoordinateMapper().getDomainAxis() )
      return layer.getDomainRange();

    if( axis == layer.getCoordinateMapper().getTargetAxis() )
      return layer.getTargetRange();

    return null;

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getState()
   */
  @Override
  public IChartModelState getState( )
  {
    return new ChartModelState( getLayerManager() );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getTitle()
   */
  @Override
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#isHideUnusedAxes()
   */
  @Override
  public boolean isHideUnusedAxes( )
  {
    return m_hideUnusedAxes;
  }

  /**
   * Maximizes the chart view - that means all the available data of all layers is shown
   */
  public void maximize( )
  {
    autoscale( null );
    // ModelChangedEvent werfen, damit Composite das Model neu zeichnet
  }

  @Override
  public void panTo( final Point start, final Point end )
  {
    if( start.equals( end ) )
      return;

    final IAxis[] axes = getMapperRegistry().getAxes();
    for( final IAxis axis : axes )
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
      final double diff = nowNum.doubleValue() - startNum.doubleValue();
      if( Double.isNaN( diff ) )
        continue;
      final IDataRange<Number> initRange = axis.getNumericRange();
      newmin = initRange.getMin().doubleValue() + diff;
      newmax = initRange.getMax().doubleValue() + diff;

      final IDataRange<Number> newRange = new ComparableDataRange<Number>( new Number[] { new Double( newmin ), new Double( newmax ) } );
      axis.setNumericRange( newRange );
    }

  }

  /**
   * sets auto scaling
   * 
   * @param b
   *          if true, axes are automatically scaled to show the layers full data range
   */
  @Override
  @Deprecated
  /**use ChartModel#autoscale(null) instead **/
  public void setAutoscale( final boolean b )
  {
    // m_autoscale = b;

    if( b )
    {
      autoscale( null );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setDescription(java.lang.String)
   */
  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
   */
  @Override
  public void setHideUnusedAxes( final boolean b )
  {
    if( b == m_hideUnusedAxes )
      return;
    m_hideUnusedAxes = b;

    for( final IAxis axis : m_mapperRegistry.getAxes() )
    {
      if( m_hideUnusedAxes )
        hideUnusedAxis( axis );
      else
        axis.setVisible( true );
    }

  }

  protected void hideUnusedAxis( final IAxis axis )
  {
    // if axis has no layers, hide axis
    final List<IChartLayer> list = m_axis2Layers.get( axis );
    if( list == null )
    {
      axis.setVisible( false );
      return;
    }
    // if all layers are hidden, hide axis too
    for( final IChartLayer layer : list )
    {
      if( layer.isVisible() )
      {
        axis.setVisible( true );
        return;
      }
    }
    axis.setVisible( false );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setId()
   */
  @Override
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String)
   */
  @Override
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * adds layers to or removes layers from the chart
   * 
   * @param bAdding
   *          if true, the layer will be added; if false, the layer will be removed
   */
  protected void updateAxisLayerMap( final IChartLayer layer, final boolean bAdding )
  {
    final ICoordinateMapper cm = layer.getCoordinateMapper();
    if( cm == null )
      return;
    final IAxis domAxis = cm.getDomainAxis();
    final IAxis valAxis = cm.getTargetAxis();
    List<IChartLayer> domList = m_axis2Layers.get( domAxis );
    List<IChartLayer> valList = m_axis2Layers.get( valAxis );

    if( bAdding )
    {
      // mapping for domain axis
      if( domList == null )
      {
        domList = new ArrayList<IChartLayer>();
        final IAxis axis = cm.getDomainAxis();
        m_axis2Layers.put( axis, domList );
      }
      domList.add( layer );

      // mapping for value axis
      if( valList == null )
      {
        valList = new ArrayList<IChartLayer>();
        final IAxis axis = cm.getTargetAxis();
        m_axis2Layers.put( axis, valList );
      }
      valList.add( layer );

    }
    else
    {
      // remove domain mapping
      if( domList != null )
      {
        domList.remove( layer );
      }

      // remove value mapping
      if( valList != null )
      {
        valList.remove( layer );
      }

    }
    if( m_hideUnusedAxes )
    {
      domAxis.setVisible( domList.size() > 0 );
      valAxis.setVisible( valList.size() > 0 );
    }
    if( m_autoscale )
    {
      autoscale( new IAxis[] { cm.getDomainAxis(), cm.getTargetAxis() } );
    }

  }

  /**
   * Maximizes the content of the plot to the values inside a dragged rectangle
   */
  @Override
  public <T_logical> void zoomIn( final Point start, final Point end )
  {
    final IMapperRegistry ar = getMapperRegistry();
    final IAxis[] axes = ar.getAxes();
    for( final IAxis axis : axes )
    {
      Number from = null;
      Number to = null;
      if( start == null || end == null )
        continue;
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
        axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { from, to } ) );
      }
    }
    // m_eventHandler.fireModelChanged();
  }

  /**
   * minimizes the content of the plot to the values inside a dragged rectangle
   */
  @Override
  public <T_logical> void zoomOut( final Point start, final Point end )
  {
    if( end == null )
      return;
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
        final double mouserange = Math.abs( from - to );

        final IDataRange<Number> numericRange = axis.getNumericRange();

        final Number min = numericRange.getMin();
        final Number max = numericRange.getMax();

        if( min != null && max != null )
        {
          final double oldmin = min.doubleValue();
          final double oldmax = max.doubleValue();
          final double oldrange = Math.abs( oldmin - oldmax );
          final double newrange = (oldrange / mouserange) * oldrange;

          final double newFrom = oldmin - ((Math.abs( from - oldmin ) / oldrange) * newrange);
          final double newTo = oldmax + ((Math.abs( to - oldmax ) / oldrange) * newrange);

          axis.setNumericRange( new ComparableDataRange<Number>( new Number[] { new Double( newFrom ), new Double( newTo ) } ) );
        }
      }
    }
  }

}
