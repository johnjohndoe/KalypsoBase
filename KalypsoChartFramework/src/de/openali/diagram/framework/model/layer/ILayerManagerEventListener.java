package de.openali.diagram.framework.model.layer;

/**
 * @author burtscher
 */
public interface ILayerManagerEventListener
{
  public void onLayerAdded( IChartLayer layer );

  public void onLayerRemoved( IChartLayer layer );
}
