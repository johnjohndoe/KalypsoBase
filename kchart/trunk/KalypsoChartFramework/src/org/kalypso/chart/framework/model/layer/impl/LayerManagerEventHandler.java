package org.kalypso.chart.framework.model.layer.impl;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.ILayerManagerEventProvider;

/**
 * @author burtscher
 */
public class LayerManagerEventHandler implements ILayerManagerEventProvider
{
  private final List<ILayerManagerEventListener> m_listeners = new ArrayList<ILayerManagerEventListener>();

  /**
   * @see org.kalypso.chart.framework.axis.ILayerManagerEventProvider#addLayerManagerEventListener(org.kalypso.chart.framework.axis.ILayerManagerEventListener)
   */
  public void addLayerManagerEventListener( final ILayerManagerEventListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.chart.framework.axis.ILayerManagerEventProvider#removeLayerManagerEventListener(org.kalypso.chart.framework.axis.ILayerManagerEventListener)
   */
  public void removeLayerManagerEventListener( final ILayerManagerEventListener l )
  {
    m_listeners.remove( l );
  }

  protected void fireLayerAdded( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : m_listeners )
      l.onLayerAdded( layer );
  }

  protected void fireLayerRemoved( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : m_listeners )
      l.onLayerRemoved( layer );
  }

}
