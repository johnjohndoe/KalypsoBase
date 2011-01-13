package de.openali.odysseus.chart.framework.model.layer.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerManagerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;

public class LayerManager implements ILayerManager
{
  final LayerManagerEventHandler m_handler = new LayerManagerEventHandler();

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
  /**
   * FIXME HashSet instead of list?!?
   */
  private final List<IChartLayer> m_layers = new ArrayList<IChartLayer>();

  @Override
  public void accept( final IChartLayerVisitor... visitors )
  {
    for( final IChartLayerVisitor visitor : visitors )
    {
      accept( visitor );
    }
  }

  @Override
  public void accept( final IChartLayerVisitor visitor )
  {
    for( final IChartLayer layer : m_layers )
    {
      visitor.visit( layer );
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.layer.ILayerManager#addLayer(de.openali.odysseus.chart.framework.layer.IChartLayer)
   */
  @Override
  public void addLayer( final IChartLayer... layers )
  {
    if( ArrayUtils.isEmpty( layers ) )
      return;

    Collections.addAll( m_layers, layers );

    for( final IChartLayer layer : layers )
    {
      layer.addListener( m_layerListener );
      m_handler.fireLayerAdded( layer );
    }
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

  @Override
  public void addListener( final ILayerManagerEventListener l )
  {
    m_handler.addListener( l );
  }

  @Override
  public void clear( )
  {
    for( final IChartLayer next : getLayers() )
    {
      removeLayer( next );
    }
  }

  @Override
  public IChartLayer findLayer( final String identifier )
  {
    final FindLayerVisitor visitor = new FindLayerVisitor( identifier );
    accept( visitor );

    return visitor.getLayer();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getLayerPosition(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public int getLayerPosition( final IChartLayer layer )
  {
    return m_layers.indexOf( layer );
  }

  /**
   * @return List of all ChartLayer objects
   */
  @Override
  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[] {} );
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
    layer.dispose();

    m_handler.fireLayerRemoved( layer );
  }

  @Override
  public void removeListener( final ILayerManagerEventListener l )
  {
    m_handler.removeListener( l );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ILayerManager#getSize()
   */
  @Override
  public int size( )
  {
    return m_layers.size();
  }
}
