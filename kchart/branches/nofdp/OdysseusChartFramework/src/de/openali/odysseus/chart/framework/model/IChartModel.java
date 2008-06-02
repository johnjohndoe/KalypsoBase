package de.openali.odysseus.chart.framework.model;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

public interface IChartModel extends IEventProvider<IChartModelEventListener>
{

  public IMapperRegistry getMapperRegistry( );

  public ILayerManager getLayerManager( );

  public void clear( );

  public Map<IAxis, List<IChartLayer>> getAxis2Layers( );

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

  public String getId( );

  public void setId( String id );

  public String getTitle( );

  public void setTitle( String title );

  public String getDescription( );

  public void setDescription( String description );

  public <T_logical> void zoomIn( Point start, Point end );

  public <T_logical> void zoomOut( Point start, Point end );

  public void panTo( Point start, Point end );

}