package de.openali.odysseus.chart.framework.model.event;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 */
public interface ILayerManagerEventListener
{
  /**
   * called when layer was added to layer stack
   */
  void onLayerAdded( IChartLayer layer );

  /**
   * called when layer was removed from layer stack
   */
  void onLayerRemoved( IChartLayer layer );

  /**
   * called when layer has been moved to another position in the layer stack
   */
  void onLayerMoved( IChartLayer layer );

  /**
   * called when layer visibility has changed
   */
  void onLayerVisibilityChanged( IChartLayer layer );

  /**
   * called when layer content has changed
   */
  void onLayerContentChanged( IChartLayer layer );

  /**
   * called when active layer changed
   */
  void onActivLayerChanged( IChartLayer layer );

}
