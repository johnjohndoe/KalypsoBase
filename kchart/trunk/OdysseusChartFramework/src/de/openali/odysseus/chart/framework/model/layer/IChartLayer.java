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
   * @return true if the layer is set active, false otherwise
   */
  public boolean isActive( );

  public void setActive( boolean isActive );

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

  /**
   * Initialization method; will be called after setCoordinateMapper
   */
  public void init( );

  public void dispose( );

  public ILegendEntry[] getLegendEntries( );


  // @SuppressWarnings("unchecked")
  // public void setMappers( Map<String, IMapper> mapperMap );

}
