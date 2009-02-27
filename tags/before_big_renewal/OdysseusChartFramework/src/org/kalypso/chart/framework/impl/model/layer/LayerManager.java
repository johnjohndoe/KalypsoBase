package org.kalypso.chart.framework.impl.model.layer;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.framework.impl.model.event.AbstractLayerEventListener;
import org.kalypso.chart.framework.impl.model.event.LayerManagerEventHandler;
import org.kalypso.chart.framework.model.event.ILayerEventListener;
import org.kalypso.chart.framework.model.event.ILayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.IEditableChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;

public class LayerManager implements ILayerManager
{

  /** my layers */
  private final List<IChartLayer< ? , ? >> m_layers = new ArrayList<IChartLayer< ? , ? >>();

  private final LayerManagerEventHandler m_handler = new LayerManagerEventHandler();

  /**
   * @see org.kalypso.chart.framework.layer.ILayerManager#addLayer(org.kalypso.chart.framework.layer.IChartLayer)
   */
  public void addLayer( final IChartLayer< ? , ? > layer )
  {
    m_layers.add( layer );
    ILayerEventListener lel = new AbstractLayerEventListener()
    {
      /**
       * @see org.kalypso.chart.framework.impl.model.event.AbstractLayerEventListener#onLayerContentChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerContentChanged( IChartLayer< ? , ? > layer )
      {
        m_handler.fireLayerContentChanged( layer );
      }

      /**
       * @see org.kalypso.chart.framework.impl.model.event.AbstractLayerEventListener#onLayerVisibilityChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
       */
      @Override
      public void onLayerVisibilityChanged( IChartLayer< ? , ? > layer )
      {
        m_handler.fireLayerVisibilityChanged( layer );
      }
    };
    layer.addListener( lel );

    m_handler.fireLayerAdded( layer );
  }

  /**
   * @see org.kalypso.chart.framework.layer.ILayerManager#removeLayer(org.kalypso.chart.framework.layer.IChartLayer)
   *      reomves layer from chart
   */
  public void removeLayer( final IChartLayer< ? , ? > layer )
  {
    m_layers.remove( layer );
    m_handler.fireLayerRemoved( layer );
  }

  public void addListener( ILayerManagerEventListener l )
  {
    m_handler.addListener( l );
  }

  public void removeListener( ILayerManagerEventListener l )
  {
    m_handler.removeListener( l );
  }

  public void clear( )
  {

  }

  /**
   * @return List of all ChartLayer objects
   */
  public IChartLayer< ? , ? >[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[0] );
  }

  public void moveLayerToPosition( IChartLayer< ? , ? > layer, int position )
  {
    m_layers.remove( layer );
    m_layers.add( position, layer );
    m_handler.fireLayerMoved( layer );
  }

  public IChartLayer< ? , ? > getLayerById( String id )
  {
    for( final IChartLayer< ? , ? > layer : m_layers )
    {
      if( layer.getId().equals( id ) )
        return layer;
    }
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.ILayerManager#addLayer(org.kalypso.chart.framework.model.layer.IChartLayer,
   *      int)
   */
  public void addLayer( IChartLayer< ? , ? > layer, int position )
  {
    m_layers.add( position, layer );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.ILayerManager#getLayerPosition(org.kalypso.chart.framework.model.layer.IChartLayer)
   */
  public int getLayerPosition( IChartLayer< ? , ? > layer )
  {
    int count = 0;
    for( IChartLayer< ? , ? > l : m_layers )
    {
      if( layer == l )
        return count;
      count++;
    }
    return -1;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.ILayerManager#getSize()
   */
  public int getSize( )
  {
    return m_layers.size();
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.ILayerManager#getEditableLayers()
   */
  @SuppressWarnings( { "unchecked", "cast" })
  public IEditableChartLayer[] getEditableLayers( )
  {
    List<IEditableChartLayer> editLayers = new ArrayList<IEditableChartLayer>();
    for( IChartLayer layer : getLayers() )
    {
      if( layer instanceof IEditableChartLayer )
        editLayers.add( (IEditableChartLayer) layer );
    }
    return (IEditableChartLayer[]) editLayers.toArray( new IEditableChartLayer[] {} );
  }
}
