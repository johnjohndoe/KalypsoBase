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
  public void onLayerAdded( IChartLayer layer );

  /**
   * called when layer was removed from layer stack
   */
  public void onLayerRemoved( IChartLayer layer );

  /**
   * called when layer has been moved to another position in the layer stack
   */
  public void onLayerMoved( IChartLayer layer );

  /**
   * called when layer visibility has changed
   */
  public void onLayerVisibilityChanged( IChartLayer layer );

  /**
   * called when layer content has changed
   */
  public void onLayerContentChanged( IChartLayer layer );
  
  /**
   * called when active layer changed
   */
  public void onActivLayerChanged( IChartLayer layer );

}
