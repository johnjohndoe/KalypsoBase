package de.openali.odysseus.chart.framework.model.layer;

import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author burtscher an IChartLayer represents a (visual) layer of the chart; it can be assigned to up to 2 axes to
 *         translate logical data into screen values
 */
public interface IChartLayer extends IEventProvider<ILayerEventListener>
{
  /**
   * Setzt die ID des Layers; die ID wird verwendet, um das Layer im Chart zu referenzieren
   */
  void setId( final String id );

  /**
   * Gibt die ID des Layers zurück; die ID wird verwendet, um das Layer im Chart zu referenzieren
   */
  String getId( );

  /**
   * sets the layers title (which will be shown in the legend)
   */
  void setTitle( final String title );

  /**
   * @return the layers title
   */
  String getTitle( );

  /**
   * sets a description for the layer
   */
  void setDescription( String description );

  /**
   * @return the layers description
   */
  String getDescription( );

  /**
   * @return true if the layer is set active, false otherwise
   */
  boolean isActive( );

  void setActive( boolean isActive );

  /**
   * draws the layer using the given GC and Device
   */
  void paint( final GC gc );

  void setVisible( final boolean isVisible );

  boolean isVisible( );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  /**
   * get stored data objects
   */
  Object getData( String identifier );

  IDataRange<Number> getDomainRange( );

  /**
   * @param domainIntervall
   *          shrinks the TargetRange, pass null to retrieve full dataRange
   */
  IDataRange<Number> getTargetRange( IDataRange<Number> domainIntervall );

  ICoordinateMapper getCoordinateMapper( );

  void setCoordinateMapper( ICoordinateMapper coordinateMapper );

  /**
   * Initialization method; will be called after setCoordinateMapper
   */
  void init( );

  void dispose( );

  ILegendEntry[] getLegendEntries( );

  /**
   * @return layer is visible in chart legend
   */
  boolean isLegend( );

  /**
   * @param isVisible
   *          layer is visible in chart legend
   */
  void setLegend( final boolean isVisible );
}
