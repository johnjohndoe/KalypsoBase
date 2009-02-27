package org.kalypso.chart.framework.model.layer;

import org.kalypso.chart.framework.model.event.IEventProvider;
import org.kalypso.chart.framework.model.event.ILayerManagerEventListener;

/**
 * @author burtscher used to manage chart layers - right now this means adding and removing them
 */
public interface ILayerManager extends IEventProvider<ILayerManagerEventListener>
{
  public void addLayer( IChartLayer< ? , ? > layer );

  public void addLayer( IChartLayer< ? , ? > layer, int position );

  public int getLayerPosition( IChartLayer< ? , ? > layer );

  public void removeLayer( IChartLayer< ? , ? > layer );

  /**
   * Hiermit wird der Befehl zum Schliessen des LayerManagers erteilt; in der Implementation sollen alle selbst
   * erzeugten Resourcen geschlossen werden
   */
  public void clear( );

  /**
   * Gibt eine Liste aller vorhandenen Layer zurück. Die List ist geordnet in der Reihenfolge, in der die Layer
   * gezeichnet werden.
   * 
   * @return
   */
  public IChartLayer< ? , ? >[] getLayers( );

  public void moveLayerToPosition( IChartLayer< ? , ? > layer, int position );

  public IChartLayer< ? , ? > getLayerById( String id );

  public int getSize( );

  /**
   * returns only layers which are editable (
   */
  public IEditableChartLayer< ? , ? >[] getEditableLayers( );
}
