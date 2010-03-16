package de.openali.odysseus.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.ChartModelEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.impl.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.component.IAxisComponent;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

public class ChartModel implements IChartModel
{
  private boolean m_hideUnusedAxes = false;

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  /** axis --> List of layers */
  private final Map<IAxis, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis, List<IChartLayer>>();

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
    m_manager.addListener( m_layerMana );

    m_eventHandler = new ChartModelEventHandler();
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
    List<IChartLayer> domList = m_axis2Layers.get( cm.getDomainAxis() );
    List<IChartLayer> valList = m_axis2Layers.get( cm.getTargetAxis() );

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

      // axis-components must be visible

      final IAxisComponent domainComp = m_mapperRegistry.getComponent( cm.getDomainAxis() );
      if( domainComp != null )
      {
        domainComp.setVisible( true );
      }
      final IAxisComponent valueComp = m_mapperRegistry.getComponent( cm.getTargetAxis() );
      if( valueComp != null )
      {
        valueComp.setVisible( true );
        // m_mapperRegistry.getComponent( layer.getValueAxis()
        // ).setVisible( true );
      }

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

      // eventually hide axes
      if( m_hideUnusedAxes )
      {
        if( domList == null || domList.size() == 0 )
        {
          m_mapperRegistry.getComponent( cm.getDomainAxis() ).setVisible( false );
        }
        if( valList == null || valList.size() == 0 )
        {
          m_mapperRegistry.getComponent( cm.getTargetAxis() ).setVisible( false );
        }
      }
    }

    if( m_autoscale )
    {
      autoscale( new IAxis[] { cm.getDomainAxis(), layer.getCoordinateMapper().getTargetAxis() } );
    }

  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#getAxisRegistry()
   */
  public IMapperRegistry getMapperRegistry( )
  {
    return m_mapperRegistry;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#getLayerManager()
   */
  public ILayerManager getLayerManager( )
  {
    return m_manager;
  }

  /*
   * (non-Javadoc)
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
   * @see org.kalypso.chart.framework.model.IChartModel#getAxis2Layers()
   */
  public Map<IAxis, List<IChartLayer>> getAxis2Layers( )
  {
    return m_axis2Layers;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
   */
  public void setHideUnusedAxes( final boolean b )
  {
    m_hideUnusedAxes = b;

    final IAxis[] axes = m_mapperRegistry.getAxes();
    for( final IAxis axis : axes )
    {
      final List<IChartLayer> list = m_axis2Layers.get( axis );
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
        final double rangeSize = rangeMax == rangeMin ? rangeMax : rangeMax - rangeMin;
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
   * sets autoscaling
   * 
   * @param b
   *          if true, axes are automatically scaled to show the layers full data range
   */
  public void setAutoscale( final boolean b )
  {
    m_autoscale = b;

    if( m_autoscale )
    {
      autoscale( null );
    }
  }

  /**
   * maximises the chart view - that means all the available data of all layers is shown
   */
  public void maximize( )
  {
    final IAxis[] axes = getMapperRegistry().getAxes();
    autoscale( axes );
    // ModelChangedEvent werfen, damit Composite das Model neu zeichnet
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  public void addListener( final IChartModelEventListener listener )
  {
    m_eventHandler.addListener( listener );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  public void removeListener( final IChartModelEventListener listener )
  {
    m_eventHandler.removeListener( listener );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getId()
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setId()
   */
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setDescription(java.lang.String)
   */
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String)
   */
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * maximises the content of the plot to the values inside a dragged rectangle
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
