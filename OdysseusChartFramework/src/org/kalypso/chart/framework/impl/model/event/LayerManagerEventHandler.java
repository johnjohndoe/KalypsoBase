package org.kalypso.chart.framework.impl.model.event;

import org.kalypso.chart.framework.model.event.ILayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 */
public class LayerManagerEventHandler extends AbstractEventProvider<ILayerManagerEventListener>
{

  public void fireLayerAdded( final IChartLayer< ? , ? > layer )
  {
    for( final ILayerManagerEventListener l : getListeners() )
      l.onLayerAdded( layer );
  }

  public void fireLayerRemoved( final IChartLayer< ? , ? > layer )
  {
    for( final ILayerManagerEventListener l : getListeners() )
      l.onLayerRemoved( layer );
  }

  public void fireLayerMoved( final IChartLayer< ? , ? > layer )
  {
    for( final ILayerManagerEventListener l : getListeners() )
      l.onLayerMoved( layer );
  }

  public void fireLayerVisibilityChanged( final IChartLayer< ? , ? > layer )
  {
    for( final ILayerManagerEventListener l : getListeners() )
      l.onLayerVisibilityChanged( layer );
  }

  public void fireLayerContentChanged( IChartLayer< ? , ? > layer )
  {
    for( final ILayerManagerEventListener l : getListeners() )
      l.onLayerContentChanged( layer );
  }

}
