package de.openali.odysseus.chart.framework.model;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

public interface IChartModel
{

  IMapperRegistry getMapperRegistry( );

  ILayerManager getLayerManager( );

  void clear( );

  Map<IAxis, List<IChartLayer>> getAxis2Layers( );

  void dispose( );

  /**
   * @param b
   *          if true, axes in the AxisRegistry which are not used by any layer are hidden; if false, all axes are shown
   */
  void setHideUnusedAxes( boolean b );

  boolean isHideUnusedAxes( );

  void setHideLegend( boolean b );

  boolean isHideLegend( );

  void setHideTitle( boolean b );

  boolean isHideTitle( );

  /**
   * When called, all given axes set their DataRange so all their layers data can be shown
   */
  void autoscale( IAxis[] axes );

  /**
   * automatic call autoscale of axes when adding or removing layer
   */
  void setAutoscale( boolean b );

  String getId( );

  void setId( String id );

  String[] getTitle( );

  void setTitle( String[] title );

  String getDescription( );

  void setDescription( String description );

  <T_logical> void zoomIn( Point start, Point end );

  <T_logical> void zoomOut( Point start, Point end );

  void panTo( Point start, Point end );

  IChartModelState getState( );

  ITextStyle getTextStyle( );

}