package de.openali.odysseus.chart.framework.model;

import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.impl.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.impl.IChartBehaviour;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

public interface IChartModel extends ILayerContainer
{

  IMapperRegistry getMapperRegistry( );

  void clear( );

  Map<IAxis, List<IChartLayer>> getAxis2Layers( );

  void dispose( );

  /**
   * When called, all given axes set their DataRange so all their layers data can be shown
   */
  void autoscale( IAxis[] axes );

  String getId( );

  void setId( String id );

  IChartModelState getState( );

  IChartBehaviour getBehaviour( );

  IBasicChartSettings getSettings( );
}