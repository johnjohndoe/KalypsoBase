package de.openali.odysseus.chart.framework.model.event.impl;

import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 */
public class LayerManagerEventHandler extends AbstractEventProvider<ILayerManagerEventListener>
{
  public void fireLayerAdded( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onLayerAdded( layer );
    }
  }

  public void fireActiveLayerChanged( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onActivLayerChanged( layer );
    }
  }

  public void fireLayerRemoved( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onLayerRemoved( layer );
    }
  }

  public void fireLayerMoved( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onLayerMoved( layer );
    }
  }

  public void fireLayerVisibilityChanged( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onLayerVisibilityChanged( layer );
    }
  }

  public void fireLayerContentChanged( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : getListeners( ILayerManagerEventListener.class ) )
    {
      l.onLayerContentChanged( layer );
    }
  }

}
