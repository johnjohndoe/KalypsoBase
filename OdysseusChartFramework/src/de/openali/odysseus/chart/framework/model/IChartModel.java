package de.openali.odysseus.chart.framework.model;

import de.openali.odysseus.chart.framework.model.impl.IChartBehaviour;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;

public interface IChartModel extends ILayerContainer
{
  /**
   * When called, all given axes set their DataRange so all their layers data can be shown
   */
  void autoscale( IAxis<?>... axes );

  void clear( );

  void dispose( );

  IChartBehaviour getBehaviour( );

  IAxisRegistry getAxisRegistry( );

  IBasicChartSettings getSettings( );

  Object getData( String key );

  Object setData( String key, Object value );
}