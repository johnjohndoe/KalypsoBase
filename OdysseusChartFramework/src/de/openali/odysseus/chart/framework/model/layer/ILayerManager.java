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
  /**
   * @deprecated Use {@link #accept(IChartLayerVisitor2)} instead. This implementation does not recurse and is hence
   *             rubbish.
   */
  @Deprecated
  void accept( IChartLayerVisitor visitor );

  /**
   * @deprecated Use {@link ILayerContainer#accept(IChartLayerVisitor2)} instead. This implementation does not recurse
   *             and is hence rubbish.
   */
  @Deprecated
  void accept( IChartLayerVisitor... visitors );

  void addLayer( IChartLayer... layer );

  /**
   * Hiermit wird der Befehl zum Schliessen des LayerManagers erteilt; in der Implementation sollen alle selbst
   * erzeugten Resourcen geschlossen werden
   */
  void clear( );

  IChartLayer findLayer( String identifier );

  int getLayerPosition( IChartLayer layer );

  /**
   * Gibt eine Liste aller vorhandenen Layer zur�ck. Die List ist geordnet in der Reihenfolge, in der die Layer
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
