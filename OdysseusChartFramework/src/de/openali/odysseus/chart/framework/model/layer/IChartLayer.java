package de.openali.odysseus.chart.framework.model.layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;

import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author burtscher an IChartLayer represents a (visual) layer of the chart; it can be assigned to up to 2 axes to
 *         translate logical data into screen values
 */
public interface IChartLayer extends ILayerContainer, IEventProvider<ILayerEventListener>
{
  void dispose( );

  ICoordinateMapper getCoordinateMapper( );

  /**
   * get stored data objects
   */
  Object getData( String identifier );

  /**
   * @return the layers description
   */
  String getDescription( );

  IDataRange< ? > getDomainRange( );

  ILegendEntry[] getLegendEntries( );

  ILayerProvider getProvider( );

  /**
   * @param domainIntervall
   *          shrinks the TargetRange, pass null to retrieve full dataRange
   */
  IDataRange< ? > getTargetRange( IDataRange< ? > domainIntervall );

  /**
   * @return the layers title
   */
  String getTitle( );

  /**
   * Initialization method; will be called after setCoordinateMapper
   * FIXME remove from here, DataHandling
   */
  void init( );

  /**
   * @return true if the layer is set active, false otherwise
   */
  boolean isActive( );

  /**
   * @return layer is visible in chart legend
   */
  boolean isLegend( );

  boolean isVisible( );

  /**
   * draws the layer using the given GC and Device
   */
  void paint( final GC gc, ChartImageInfo chartImageInfo, IProgressMonitor monitor );

  void setActive( boolean isActive );

  // FIXME: bad; layer should always get mapper in constructor! What layers changes the axis during it's live?
  void setCoordinateMapper( ICoordinateMapper coordinateMapper );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  /**
   * sets a description for the layer
   */
  void setDescription( String description );

  /**
   * @param isVisible
   *          layer is visible in chart legend
   */
  void setLegend( final boolean isVisible );

  void setParent( ILayerContainer parent );

  /**
   * sets the layers title (which will be shown in the legend)
   */
  void setTitle( final String title );

  void setVisible( final boolean isVisible );

  void addFilter( IChartLayerFilter... filter );

  void removeFilter( IChartLayerFilter... filter );

  void setFilter( IChartLayerFilter... filters );

  IChartLayerFilter[] getFilters( );

  boolean isAutoScale( );
}