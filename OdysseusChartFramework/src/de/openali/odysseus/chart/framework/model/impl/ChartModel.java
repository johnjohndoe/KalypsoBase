package de.openali.odysseus.chart.framework.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Arrays;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.impl.settings.BasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.utils.ChartModelLayerEventListener;
import de.openali.odysseus.chart.framework.model.impl.visitors.AutoScaleVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.DisposeLayersVisitor;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.AxisRegistry;

public class ChartModel implements IChartModel
{
  private final ChartBehaviour m_behaviour = new ChartBehaviour( this );

  private String m_identifier = ""; //$NON-NLS-1$

  private final ILayerManager m_layerManager = new LayerManager( this );

  private final IAxisRegistry m_mapperRegistry = new AxisRegistry();

  private IBasicChartSettings m_settings = new BasicChartSettings();

  private final Map<String, Object> m_dataMap = new HashMap<>();

  public ChartModel( )
  {
    getLayerManager().addListener( new ChartModelLayerEventListener( this ) );
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   *
   * @param axes
   *          axes == null -> update all chart model axes
   */
  @SuppressWarnings( "rawtypes" )
  @Override
  public void autoscale( final IAxis... axes )
  {
    final AutoScaleVisitor visitor = new AutoScaleVisitor( this );
    // axes==null means all Axes
    for( final IAxis axis : Arrays.isEmpty( axes ) ? getAxisRegistry().getAxes() : axes )
    {
      visitor.visit( axis );
    }
  }

  @Override
  public void clear( )
  {
    m_settings = new BasicChartSettings();
    // FIXME: also the behaviour should be cleard, is this safe here?

    getLayerManager().clear();
    getAxisRegistry().clear();
  }

  @Override
  public void dispose( )
  {
    // TODO: dispose layer manager instead
    final ILayerManager layerManager = getLayerManager();
    layerManager.accept( new DisposeLayersVisitor() );
    // TODO: dispose m_behavior
    m_dataMap.clear();
  }

  @Override
  public IChartBehaviour getBehaviour( )
  {
    return m_behaviour;
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public ILayerManager getLayerManager( )
  {
    return m_layerManager;
  }

  @Override
  public IAxisRegistry getAxisRegistry( )
  {
    return m_mapperRegistry;
  }

  @Override
  public ILayerContainer getParent( )
  {
    // chart model is root element! so return null
    return null;
  }

  @Override
  public IBasicChartSettings getSettings( )
  {
    return m_settings;
  }

  /**
   * Maximizes the chart view - that means all the available data of all layers is shown
   */
  public void maximize( )
  {
    autoscale();
  }

  @Override
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
  }

  @Override
  public IChartModel getModel( )
  {
    return this;
  }

  @Override
  public Object getData( final String key )
  {
    return m_dataMap.get( key );
  }

  @Override
  public Object setData( final String key, final Object value )
  {
    return m_dataMap.put( key, value );
  }

  @Override
  public void accept( final IChartLayerVisitor2 visitor )
  {
    try
    {
      final IChartLayer[] layers = getLayerManager().getLayers();

      if( !visitor.getVisitDirection() )
        ArrayUtils.reverse( layers );

      for( final IChartLayer layer : layers )
        layer.accept( visitor );
    }
    catch( final CancelVisitorException e )
    {
      // simply stop recursion
    }
  }
}
