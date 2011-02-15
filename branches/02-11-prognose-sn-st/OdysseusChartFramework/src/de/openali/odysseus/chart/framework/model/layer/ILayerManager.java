package de.openali.odysseus.chart.framework.model.layer;

import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerManagerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author burtscher used to manage chart layers - right now this means adding and removing them
 */
public interface ILayerManager extends IEventProvider<ILayerManagerEventListener>
{
  void accept( IChartLayerVisitor visitor );

  void accept( IChartLayerVisitor... visitors );

  void addLayer( IChartLayer... layer );

  void addLayer( IChartLayer layer, int position );

  /**
   * Hiermit wird der Befehl zum Schliessen des LayerManagers erteilt; in der Implementation sollen alle selbst
   * erzeugten Resourcen geschlossen werden
   */
  void clear( );

  IChartLayer findLayer( String identifier );

  int getLayerPosition( IChartLayer layer );

  /**
   * Gibt eine Liste aller vorhandenen Layer zurück. Die List ist geordnet in der Reihenfolge, in der die Layer
   * gezeichnet werden.
   * 
   * @return
   */
  IChartLayer[] getLayers( );

  int size( );

  void moveLayerToPosition( IChartLayer layer, int position );

  void removeLayer( IChartLayer layer );

  ILayerContainer getContainer( );

  LayerManagerEventHandler getEventHandler( );

  IChartLayer[] getLayers( IAxis axis, boolean recursive );
}
