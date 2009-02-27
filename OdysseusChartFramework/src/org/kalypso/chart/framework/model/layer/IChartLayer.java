package org.kalypso.chart.framework.model.layer;

import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.event.IEventProvider;
import org.kalypso.chart.framework.model.event.ILayerEventListener;
import org.kalypso.chart.framework.model.legend.ILegendItem;
import org.kalypso.chart.framework.model.mapper.ICoordinateMapper;
import org.kalypso.chart.framework.model.styles.ILayerStyle;

/**
 * @author burtscher an IChartLayer represents a (visual) layer of the chart; it can be assigned to up to 2 axes to
 *         translate logical data into screen values
 */
public interface IChartLayer<T_domain, T_target> extends IEventProvider<ILayerEventListener>
{

  /**
   * Setzt die ID des Layers; die ID wird verwendet, um das Layer im Chart zu referenzieren
   */
  public void setId( final String id );

  /**
   * Gibt die ID des Layers zurück; die ID wird verwendet, um das Layer im Chart zu referenzieren
   */
  public String getId( );

  /**
   * sets the layers title (which will be shown in the legend)
   */
  public void setTitle( final String title );

  /**
   * @return the layers title
   */
  public String getTitle( );

  /**
   * sets a description for the layer
   */
  public void setDescription( String description );

  /**
   * @return the layers description
   */
  public String getDescription( );

  /**
   * @return the IAxis object used with domain data
   * @deprecated use getCoordinateMapper().getDomainAxis() instead
   * @Deprecated
   */
  // public IAxis<T_domain> getDomainAxis( );
  /**
   * @return the IAxis object used with value data
   * @deprecated use getCoordinateMapper().getDomainAxis() instead
   * @Deprecated
   */
  // public IAxis<T_target> getTargetAxis( );
  /**
   * TODO: remove this from IChartLayer interface ILegendItem should be constructed upon layers, client should always
   * use drawIcon and getName only the legend should know about legend-items
   * 
   * @deprecated use getSymbolMap() instead
   * 
   * @return ILegendItem which describes the Layers content and style
   */
  @Deprecated
  public ILegendItem getLegendItem( );

  /**
   * draws a picture of a given size into the given Image; the icon will be used for the legend to describe the layers
   * visual representation
   * 
   * @deprecated use getSymbolMap() instead
   */
  @Deprecated
  public void drawIcon( final Image img );

  /**
   * 
   */
  public Map<String, ImageData> getSymbolMap( );

  /**
   * @return true if the layer is set active, false otherwise
   */
  public boolean isActive( );

  public void setActive( boolean isActive );

  /**
   * @return the layer style
   */
  public ILayerStyle getStyle( );

  /**
   * sets the layer's style
   */
  public void setStyle( final ILayerStyle style );

  /**
   * draws the layer using the given GC and Device
   */
  public void paint( final GC gc );

  public void setVisible( final boolean isVisible );

  public boolean isVisible( );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  /**
   * get stored data objects
   */
  public Object getData( String identifier );

  public IDataRange<Number> getDomainRange( );

  public IDataRange<Number> getTargetRange( );

  public ICoordinateMapper getCoordinateMapper( );

  public void setCoordinateMapper( ICoordinateMapper coordinateMapper );

  public void setDataContainer( IDataContainer data );

  // Initialization method; to be called after setAxes, setDataContainer
  public void init( );

}
