package de.openali.odysseus.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.impl.visitors.AutoScaleVisitor;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;

public class ChartModel implements IChartModel
{
  protected final BasicChartSettings m_settings = new BasicChartSettings();

  protected final ChartBehaviour m_behaviour = new ChartBehaviour( this );

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  /** axis --> List of layers */
  private final Map<IAxis, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis, List<IChartLayer>>();

  private final ILayerManager m_manager = new LayerManager( this );

  private String m_id = "";

  public ChartModel( )
  {
    final AbstractLayerManagerEventListener layerManagerEventListener = new AbstractLayerManagerEventListener()
    {
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

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( final IChartLayer layer )
      {
        if( getBehaviour().isHideUnusedAxes() )
        {
          final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
          if( coordinateMapper != null )
          {
            m_behaviour.hideUnusedAxis( coordinateMapper.getTargetAxis() );
            m_behaviour.hideUnusedAxis( coordinateMapper.getDomainAxis() );
          }
        }
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

      /**
       * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( final IChartLayer layer )
      {
        if( getBehaviour().isHideUnusedAxes() )
        {
          final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
          if( coordinateMapper != null )
          {
            m_behaviour.hideUnusedAxis( coordinateMapper.getTargetAxis() );
            m_behaviour.hideUnusedAxis( coordinateMapper.getDomainAxis() );
          }
        }
      }
    };

    getLayerManager().addListener( layerManagerEventListener );
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @Override
  public void autoscale( final IAxis[] axes )
  {
    final AutoScaleVisitor visitor = new AutoScaleVisitor( this );

    // TODO ?!? auto scaled axes will be updated when?!? strange behavior
    final IAxis[] autoscaledAxes = axes == null ? getMapperRegistry().getAxes() : axes;
    for( final IAxis axis : autoscaledAxes )
    {
      visitor.visit( axis );
    }
  }

  /**
   * @see org.kalypso.chart.framework.model.IChartModel#clear()
   */
  @Override
  public void clear( )
  {
    m_axis2Layers.clear();
    getLayerManager().clear();
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

  @Override
  public IChartBehaviour getBehaviour( )
  {
    return m_behaviour;
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
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getState()
   */
  @Override
  public IChartModelState getState( )
  {
    return new ChartModelState( getLayerManager() );
  }

  /**
   * Maximizes the chart view - that means all the available data of all layers is shown
   */
  public void maximize( )
  {
    autoscale( null );
    // TODO ModelChangedEvent werfen, damit Composite das Model neu zeichnet
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
   * adds layers to or removes layers from the chart
   * 
   * @param add
   *          if true, the layer will be added; if false, the layer will be removed
   */
  protected void updateAxisLayerMap( final IChartLayer layer, final boolean add )
  {
    final ICoordinateMapper mapper = layer.getCoordinateMapper();
    if( mapper == null )
      return;

    final IAxis domainAxis = mapper.getDomainAxis();
    final IAxis targetAxis = mapper.getTargetAxis();

    List<IChartLayer> domainList = m_axis2Layers.get( domainAxis );
    List<IChartLayer> targetList = m_axis2Layers.get( targetAxis );

    if( add )
    {
      // mapping for domain axis
      if( domainList == null )
      {
        domainList = new ArrayList<IChartLayer>();
        final IAxis axis = mapper.getDomainAxis();
        m_axis2Layers.put( axis, domainList );
      }

      domainList.add( layer );

      // mapping for value axis
      if( targetList == null )
      {
        targetList = new ArrayList<IChartLayer>();
        final IAxis axis = mapper.getTargetAxis();
        m_axis2Layers.put( axis, targetList );
      }

      targetList.add( layer );
    }
    else
    {
      // remove domain mapping
      if( domainList != null )
      {
        domainList.remove( layer );
      }
      // remove value mapping
      if( targetList != null )
      {
        targetList.remove( layer );
      }

    }

    if( m_behaviour.isHideUnusedAxes() )
    {
      for( final IAxis axis : getMapperRegistry().getAxes() )
        m_behaviour.hideUnusedAxis( axis );
    }

    if( getBehaviour().isSetAutoscale() )
    {
      autoscale( new IAxis[] { mapper.getDomainAxis(), mapper.getTargetAxis() } );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.ILayerContainer#getParent()
   */
  @Override
  public ILayerContainer getParent( )
  {
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getSettings()
   */
  @Override
  public IBasicChartSettings getSettings( )
  {
    return m_settings;
  }

}
