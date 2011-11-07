package de.openali.odysseus.chart.framework.model.impl;

import java.util.Properties;

import org.kalypso.commons.java.lang.Arrays;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.impl.settings.BasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.utils.ChartModelLayerEventListener;
import de.openali.odysseus.chart.framework.model.impl.visitors.AutoScaleVisitor;
import de.openali.odysseus.chart.framework.model.impl.visitors.DisposeLayersVisitor;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;

public class ChartModel implements IChartModel
{
  protected final ChartBehaviour m_behaviour = new ChartBehaviour( this );

  private String m_identifier = ""; //$NON-NLS-1$

  private final ILayerManager m_layerManager = new LayerManager( this );

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  protected final BasicChartSettings m_settings = new BasicChartSettings();

  private final Properties m_properties = new Properties();

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
  @Override
  public void autoscale( final IAxis... axes )
  {
    final AutoScaleVisitor visitor = new AutoScaleVisitor( this, true );
    for( final IAxis axis : Arrays.isEmpty( axes ) ? getMapperRegistry().getAxes() : axes )
    {
      visitor.visit( axis );
    }
  }

  @Override
  public void clear( )
  {
    getLayerManager().clear();
  }

  @Override
  public void dispose( )
  {
    getLayerManager().accept( new DisposeLayersVisitor() );
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
  public IMapperRegistry getMapperRegistry( )
  {
    return m_mapperRegistry;
  }

  @Override
  public ILayerContainer getParent( )
  {
    // chart model is root element! so return null
    return null;
  }

// /**
// * @see de.openali.odysseus.chart.framework.model.IChartModel#getState()
// */
// @Override
// public IChartModelState getState( )
// {
// return new ChartModelState( getLayerManager() );
// }

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
  public Properties getProperties( )
  {
    return m_properties;
  }

}
