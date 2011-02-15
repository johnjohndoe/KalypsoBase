package de.openali.odysseus.chart.framework.model;

import de.openali.odysseus.chart.framework.model.impl.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.IChartBehaviour;
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

  String getIdentifier( );

  IMapperRegistry getMapperRegistry( );

  IBasicChartSettings getSettings( );

  void setIdentifier( String identifier );

  // IChartModelState getState( );
}