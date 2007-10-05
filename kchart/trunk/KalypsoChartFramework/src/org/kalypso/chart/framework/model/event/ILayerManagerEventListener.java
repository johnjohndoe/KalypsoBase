package org.kalypso.chart.framework.model.event;

import org.kalypso.chart.framework.model.layer.IChartLayer;

/**
 * @author burtscher
 */
public interface ILayerManagerEventListener
{
  public void onLayerAdded( IChartLayer layer );

  public void onLayerRemoved( IChartLayer layer );

  public void onLayerMoved( IChartLayer layer );

  public void onLayerVisibilityChanged( IChartLayer layer );

}
