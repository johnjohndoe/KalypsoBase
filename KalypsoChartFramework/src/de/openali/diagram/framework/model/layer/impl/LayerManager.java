package de.openali.diagram.framework.model.layer.impl;

import java.util.ArrayList;
import java.util.List;

import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.layer.ILayerManager;
import de.openali.diagram.framework.model.layer.ILayerManagerEventListener;


public class LayerManager implements ILayerManager {

	  /** my layers */
	  private final List<IChartLayer> m_layers = new ArrayList<IChartLayer>();

	  private final LayerManagerEventHandler m_handler = new LayerManagerEventHandler();
	  
	  
	/**
	   * @see de.openali.diagram.framework.layer.ILayerManager#addLayer(de.openali.diagram.framework.layer.IChartLayer)
	   */
	  public void addLayer( final IChartLayer layer )
	  {
	    m_layers.add( layer );
	    m_handler.fireLayerAdded(layer);
	  }

	  /**
	   * @see de.openali.diagram.framework.layer.ILayerManager#removeLayer(de.openali.diagram.framework.layer.IChartLayer) reomves layer
	   *      from chart
	   */
	  public void removeLayer( final IChartLayer layer )
	  {
	    m_layers.remove( layer );
	    m_handler.fireLayerRemoved(layer);
	  }
	  
	  public void addLayerManagerEventListener( ILayerManagerEventListener l )
	  {
		m_handler.addLayerManagerEventListener(l);  
	  }

	  public void removeLayerManagerEventListener( ILayerManagerEventListener l )
	  {
		  m_handler.removeLayerManagerEventListener(l);  
	  }

	  public void clear()
	  {
		 
	  }
	 
	  /**
	   * @return List of all ChartLayer objects
	   */
	  public IChartLayer[] getLayers( )
	  {
	    return m_layers.toArray(new IChartLayer[0]);
	  }

	public void moveLayerToPosition(IChartLayer layer, int position)
	{
		m_layers.remove(layer);
		m_layers.add(position, layer);
	}

	public IChartLayer getLayerById(String id)
	{
		for (IChartLayer layer: m_layers)
		{
			if (layer.getId().equals(id))
				return layer;
		}
		return null;
	}
}
