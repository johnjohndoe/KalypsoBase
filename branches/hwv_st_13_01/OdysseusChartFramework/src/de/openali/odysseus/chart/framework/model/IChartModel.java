package de.openali.odysseus.chart.framework.model;

import java.util.Properties;

import de.openali.odysseus.chart.framework.model.impl.IChartBehaviour;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

public interface IChartModel extends ILayerContainer
{

  /**
   * When called, all given axes set their DataRange so all their layers data can be shown
   */
  void autoscale( IAxis... axes );

  void clear( );

  void dispose( );

  IChartBehaviour getBehaviour( );

  IMapperRegistry getMapperRegistry( );

  IBasicChartSettings getSettings( );

  Properties getProperties( );

  // IChartModelState getState( );
}