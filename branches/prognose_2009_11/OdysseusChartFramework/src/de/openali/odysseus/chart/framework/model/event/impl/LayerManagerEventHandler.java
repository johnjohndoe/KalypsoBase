package de.openali.odysseus.chart.framework.model.event.impl;

import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 */
public class LayerManagerEventHandler extends AbstractEventProvider<ILayerManagerEventListener>
{

	public void fireLayerAdded(final IChartLayer layer)
	{
		for (final ILayerManagerEventListener l : getListeners())
		{
			l.onLayerAdded(layer);
		}
	}
	public void fireActiveLayerChanged(final IChartLayer layer)
    {
        for (final ILayerManagerEventListener l : getListeners())
        {
            l.onActivLayerChanged(layer);
        }
    }
	public void fireLayerRemoved(final IChartLayer layer)
	{
		for (final ILayerManagerEventListener l : getListeners())
		{
			l.onLayerRemoved(layer);
		}
	}

	public void fireLayerMoved(final IChartLayer layer)
	{
		for (final ILayerManagerEventListener l : getListeners())
		{
			l.onLayerMoved(layer);
		}
	}

	public void fireLayerVisibilityChanged(final IChartLayer layer)
	{
		for (final ILayerManagerEventListener l : getListeners())
		{
			l.onLayerVisibilityChanged(layer);
		}
	}

	public void fireLayerContentChanged(IChartLayer layer)
	{
		for (final ILayerManagerEventListener l : getListeners())
		{
			l.onLayerContentChanged(layer);
		}
	}

}
