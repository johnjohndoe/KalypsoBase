package de.openali.diagram.framework.model.layer.impl;

import java.util.ArrayList;
import java.util.List;

import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.layer.ILayerManagerEventListener;
import de.openali.diagram.framework.model.layer.ILayerManagerEventProvider;

/**
 * @author burtscher
 */
public class LayerManagerEventHandler implements ILayerManagerEventProvider
{
  private final List<ILayerManagerEventListener> m_listeners = new ArrayList<ILayerManagerEventListener>();

  /**
   * @see de.openali.diagram.framework.axis.ILayerManagerEventProvider#addLayerManagerEventListener(de.openali.diagram.framework.axis.ILayerManagerEventListener)
   */
  public void addLayerManagerEventListener( final ILayerManagerEventListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see de.openali.diagram.framework.axis.ILayerManagerEventProvider#removeLayerManagerEventListener(de.openali.diagram.framework.axis.ILayerManagerEventListener)
   */
  public void removeLayerManagerEventListener( final ILayerManagerEventListener l )
  {
    m_listeners.remove( l );
  }

  protected void fireLayerAdded( final IChartLayer layer)
  {
    for( ILayerManagerEventListener l : m_listeners )
      l.onLayerAdded( layer );
  }

  protected void fireLayerRemoved( final IChartLayer layer )
  {
    for( final ILayerManagerEventListener l : m_listeners )
      l.onLayerRemoved( layer );
  }

}
