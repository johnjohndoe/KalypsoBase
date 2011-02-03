package de.openali.odysseus.chart.framework.model.impl;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.IChartModelState;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.impl.visitors.AutoScaleVisitor;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisVisitor;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.CompactChartLegendRenderer;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

public class ChartModel implements IChartModel
{
  private boolean m_hideUnusedAxes = true;

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  // /** axis --> List of layers */
  private final Map<IAxis, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis, List<IChartLayer>>();

  private final ILayerManager m_manager = new LayerManager( this );

  /**
   * if set to true, all axes are sized automatically to fit all data into a layer
   */
  private final boolean m_autoscale = false;

  private boolean m_hideTitle = false;

  private boolean m_hideLegend = true;

  private String m_id = "";

  private final List<TitleTypeBean> m_title = new ArrayList<TitleTypeBean>();

  private String m_description = "";

  private ITextStyle m_textStyle = null;

  private String m_renderer;

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
        if( isHideUnusedAxes() )
        {
          final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
          if( coordinateMapper != null )
          {
            hideUnusedAxis( coordinateMapper.getTargetAxis() );
            hideUnusedAxis( coordinateMapper.getDomainAxis() );
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
        if( isHideUnusedAxes() )
        {
          final ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
          if( coordinateMapper != null )
          {
            hideUnusedAxis( coordinateMapper.getTargetAxis() );
            hideUnusedAxis( coordinateMapper.getDomainAxis() );
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
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getState()
   */
  @Override
  public IChartModelState getState( )
  {
    return new ChartModelState( getLayerManager() );
  }

  @Override
  public ITextStyle getTextStyle( )
  {
    if( m_textStyle == null )
      m_textStyle = StyleUtils.getDefaultTextStyle();

    return m_textStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getTitle()
   */
  @Override
  public TitleTypeBean[] getTitles( )
  {
    return m_title.toArray( new TitleTypeBean[] {} );
  }

  protected void hideUnusedAxis( final IAxis axis )
  {
    // if axis has no layers, hide axis
    final List<IChartLayer> list = m_axis2Layers.get( axis );
    if( list == null || list.isEmpty() )
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

  @Override
  public boolean isHideLegend( )
  {
    return m_hideLegend;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#isHideTitle()
   */
  @Override
  public boolean isHideTitle( )
  {
    return m_hideTitle;
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
    // TODO ModelChangedEvent werfen, damit Composite das Model neu zeichnet
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
      autoscale( null );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setDescription(java.lang.String)
   */
  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  @Override
  public void setHideLegend( final boolean hideLegend )
  {
    m_hideLegend = hideLegend;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setHideTitle(boolean)
   */
  @Override
  public void setHideTitle( final boolean b )
  {
    if( isHideTitle() != b )
      m_hideTitle = b;

  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
   */
  @Override
  public void setHideUnusedAxes( final boolean hide )
  {
    if( hide == m_hideUnusedAxes )
      return;

    m_hideUnusedAxes = hide;

    getMapperRegistry().accept( new IAxisVisitor()
    {
      @Override
      public void visit( final IAxis axis )
      {
        if( hide )
          hideUnusedAxis( axis );
        else
          axis.setVisible( true );
      }
    } );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setId()
   */
  @Override
  public void setId( final String id )
  {
    m_id = id;
  }

  public void setTextStyle( final ITextStyle textStyle )
  {
    m_textStyle = textStyle;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String)
   */
  @Override
  public void addTitles( final TitleTypeBean... titles )
  {
    Collections.addAll( m_title, titles );
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

    if( isHideUnusedAxes() )
    {
      for( final IAxis axis : getMapperRegistry().getAxes() )
        hideUnusedAxis( axis );
    }

    if( m_autoscale )
    {
      autoscale( new IAxis[] { mapper.getDomainAxis(), mapper.getTargetAxis() } );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setTitle(java.lang.String,
   *      de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.LABEL_POSITION,
   *      de.openali.odysseus.chart.framework.model.style.ITextStyle, java.awt.Insets)
   */
  @Override
  public void setTitle( final String title, final ALIGNMENT position, final ITextStyle textStyle, final Insets insets )
  {
    m_title.clear();
    m_title.add( new TitleTypeBean( title, position, ALIGNMENT.CENTER, textStyle, insets ) );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setLegendRenderer(java.lang.String)
   */
  @Override
  public void setLegendRenderer( final String renderer )
  {
    m_renderer = renderer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getLegendRenderer()
   */
  @Override
  public IChartLegendRenderer getLegendRenderer( )
  {
    if( m_renderer == null )
      m_renderer = CompactChartLegendRenderer.ID;

    return OdysseusChartFrameworkPlugin.getDefault().getRenderers( m_renderer );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.ILayerContainer#getParent()
   */
  @Override
  public ILayerContainer getParent( )
  {
    return null;
  }

}
