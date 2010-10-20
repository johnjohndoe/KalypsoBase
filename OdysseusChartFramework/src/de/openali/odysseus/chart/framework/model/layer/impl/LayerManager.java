package de.openali.odysseus.chart.framework.model.layer.impl;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerManagerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;

public class LayerManager implements ILayerManager
{
  private final ILayerEventListener m_layerListener = new AbstractLayerEventListener()
  {
    /**
     * @see de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerEventListener#onActiveLayerChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onActiveLayerChanged( final IChartLayer layer1 )
    {
      m_handler.fireActiveLayerChanged( layer1 );
    }

    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractLayerEventListener#onLayerContentChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerContentChanged( final IChartLayer layer1 )
    {
      m_handler.fireLayerContentChanged( layer1 );
    }

    /**
     * @see de.openali.odysseus.chart.framework.impl.model.event.AbstractLayerEventListener#onLayerVisibilityChanged(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
     */
    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer1 )
    {
      m_handler.fireLayerVisibilityChanged( layer1 );
    }
  };

  /**
   * TODO implement as hash set and overwrite equals() and hash() methods of IChartLayer instances - to prevent multiple
   * adding of layers with the same id. (see method getLayerById())
   */
  private final List<IChartLayer> m_layers = new ArrayList<IChartLayer>();

  final LayerManagerEventHandler m_handler = new LayerManagerEventHandler();

  /**
   * @see de.openali.odysseus.chart.framework.layer.ILayerManager#addLayer(de.openali.odysseus.chart.framework.layer.IChartLayer)
   */
  @Override
  public void addLayer( final IChartLayer layer )
  {
    if( layer == null )
      return;

    m_layers.add( layer );
    layer.addListener( m_layerListener );

    m_handler.fireLayerAdded( layer );
  }

  /**
   * @see de.openali.odysseus.chart.framework.layer.ILayerManager#removeLayer(de.openali.odysseus.chart.framework.layer.IChartLayer)
   *      removes layer from chart
   */
  @Override
  public void removeLayer( final IChartLayer layer )
  {
    layer.setActive( false );

    m_layers.remove( layer );
    layer.removeListener( m_layerListener );

    m_handler.fireLayerRemoved( layer );
  }

  @Override
  public void addListener( final ILayerManagerEventListener l )
  {
    m_handler.addListener( l );
  }

  @Override
  public void removeListener( final ILayerManagerEventListener l )
  {
    m_handler.removeListener( l );
  }

  @Override
  public void clear( )
  {
    final IChartLayer[] layers = m_layers.toArray( new IChartLayer[m_layers.size()] );
    for( final IChartLayer next : layers )
      removeLayer( next );
  }

  /**
   * @return List of all ChartLayer objects
   */
  @Override
  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[0] );
  }

  @Override
  public void moveLayerToPosition( final IChartLayer layer, final int position )
  {
    m_layers.remove( layer );
    if( position < m_layers.size() )
      m_layers.add( position, layer );
    else
      m_layers.add( layer );
    m_handler.fireLayerMoved( layer );
  }

  @Override
  public IChartLayer getLayerById( final String id )
  {
    for( final IChartLayer layer : m_layers )
    {
      if( layer != null && layer.getId().equals( id ) )
        return layer;
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#addLayer(de.openali.odysseus.chart.framework.model.layer.IChartLayer,
   *      int)
   */
  @Override
  public void addLayer( final IChartLayer layer, final int position )
  {
    m_layers.add( position, layer );
    layer.addListener( m_layerListener );
    m_handler.fireLayerAdded( layer );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getLayerPosition(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public int getLayerPosition( final IChartLayer layer )
  {
    int count = 0;
    for( final IChartLayer currentLayer : m_layers )
    {
      if( layer == currentLayer )
        return count;

      count++;
    }

    return -1;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getSize()
   */
  @Override
  public int getSize( )
  {
    return m_layers.size();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getEditableLayers()
   */
  @Override
  public IEditableChartLayer[] getEditableLayers( )
  {
    final List<IEditableChartLayer> editLayers = new ArrayList<IEditableChartLayer>();
    for( final IChartLayer layer : getLayers() )
    {
      if( layer instanceof IEditableChartLayer )
        editLayers.add( (IEditableChartLayer) layer );
    }

    return editLayers.toArray( new IEditableChartLayer[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getTooltipLayers()
   */
  @Override
  public ITooltipChartLayer[] getTooltipLayers( )
  {
    final List<ITooltipChartLayer> tooltipLayers = new ArrayList<ITooltipChartLayer>();
    for( final IChartLayer layer : getLayers() )
    {
      if( layer instanceof ITooltipChartLayer )
        tooltipLayers.add( (ITooltipChartLayer) layer );
    }
    return tooltipLayers.toArray( new ITooltipChartLayer[] {} );
  }
}
