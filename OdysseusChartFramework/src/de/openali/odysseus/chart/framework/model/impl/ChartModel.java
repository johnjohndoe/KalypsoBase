package de.openali.odysseus.chart.framework.model.impl;

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

  private String m_identifier = "";

  private final ILayerManager m_layerManager = new LayerManager( this );

  private final IMapperRegistry m_mapperRegistry = new MapperRegistry();

  protected final BasicChartSettings m_settings = new BasicChartSettings();

  public ChartModel( )
  {
    getLayerManager().addListener( new ChartModelLayerEventListener( this ) );
  }

  /**
   * automatically scales all given axes; scaling means here: show all available values
   */
  @Override
  public void autoscale( final IAxis... axes )
  {
    final AutoScaleVisitor visitor = new AutoScaleVisitor( this, true );

    // TODO ?!? auto scaled axes will be updated when?!? strange behaviour
    final IAxis[] autoscaledAxes = Arrays.isEmpty( axes ) ? getMapperRegistry().getAxes() : axes;
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
    getLayerManager().clear();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#dispose()
   */
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

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getId()
   */
  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  /*
   * (non-Javadoc)
   * @see org.kalypso.chart.framework.model.IChartModel#getLayerManager()
   */
  @Override
  public ILayerManager getLayerManager( )
  {
    return m_layerManager;
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
   * @see de.openali.odysseus.chart.framework.model.ILayerContainer#getParent()
   */
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

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#getSettings()
   */
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
    // TODO ModelChangedEvent werfen, damit Composite das Model neu zeichnet
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.IChartModel#setId()
   */
  @Override
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.ILayerContainer#getModel()
   */
  @Override
  public IChartModel getModel( )
  {
    return this;
  }

}
