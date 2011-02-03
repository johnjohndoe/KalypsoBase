package de.openali.odysseus.chart.framework.model;

import java.awt.Insets;
import java.util.List;
import java.util.Map;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.util.img.legend.renderer.IChartLegendRenderer;

public interface IChartModel extends ILayerContainer
{

  IMapperRegistry getMapperRegistry( );

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

  TitleTypeBean[] getTitles( );

  void setTitle( String title, final ALIGNMENT position, ITextStyle textStyle, Insets insets );

  void addTitles( TitleTypeBean... titles );

  String getDescription( );

  void setDescription( String description );

  IChartModelState getState( );

  ITextStyle getTextStyle( );

  void setLegendRenderer( String renderer );

  IChartLegendRenderer getLegendRenderer( );
}