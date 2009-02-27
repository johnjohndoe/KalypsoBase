package de.openali.diagram.framework.model.layer;

import java.util.List;

/**
 * @author burtscher
 * 
 * used to manage chart layers - right now this means adding and removing them 
 */
public interface ILayerManager extends ILayerManagerEventProvider
{
  public void addLayer( IChartLayer layer );

  public void removeLayer( IChartLayer layer );
  
  /**
   * Hiermit wird der Befehl zum Schliessen des LayerManagers erteilt;
   * in der Implementation sollen alle selbst erzeugten Resourcen geschlossen werden
   *
   */
  public void clear();
  
  /**
   * Gibt eine Liste aller vorhandenen Layer zurück. Die List ist geordnet in der Reihenfolge, in der 
   * die Layer gezeichnet werden.
   * @return
   */
  public IChartLayer[] getLayers( );
  
  

  public void moveLayerToPosition(IChartLayer layer, int position);
  
  
  public IChartLayer getLayerById(String id);
}
