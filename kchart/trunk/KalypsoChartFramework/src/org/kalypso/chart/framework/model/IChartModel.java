package org.kalypso.chart.framework.model;

import java.util.List;
import java.util.Map;

import org.kalypso.chart.framework.model.event.IChartModelEventListener;
import org.kalypso.chart.framework.model.event.IEventProvider;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;

public interface IChartModel extends IEventProvider<IChartModelEventListener>
{

  public IMapperRegistry getMapperRegistry( );

  public ILayerManager getLayerManager( );

  public void clear( );

  public Map<IAxis< ? >, List<IChartLayer>> getAxis2Layers( );

  /**
   * @param b
   *            if true, axes in the AxisRegistry which are not used by any layer are hidden; if false, all axes are
   *            shown
   */
  public void setHideUnusedAxes( boolean b );

  /**
   * When called, all given axes set their DataRange so all ther layers data can be shown
   */
  public void autoscale( IAxis[] axes );

  /**
   * sets automatical autoscaling of axes
   */
  public void setAutoscale( boolean b );

}