package de.openali.odysseus.chart.framework.model.mapper.registry;

import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.IAxisRegistryEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants;

/**
 * @author alibu Interface describing a container for the chart axes; used to ensure that only one axis is present for
 *         each DataType
 */

public interface IAxisRegistry extends IEventProvider<IAxisRegistryEventListener>
{

  @SuppressWarnings( "rawtypes" )
  /**
   * @return Array of all present axes
   */
  IAxis[] getAxes( );

  @SuppressWarnings( "rawtypes" )
  IAxis getAxis( String id );

 // IMapper getMapper( String id );

  /**
   * returns Array of all present mappers (excluding axes; if you need axes, try getAxes())
   */
  //IMapper[] getMappers( );

  @SuppressWarnings( "rawtypes" )
  /**
   * @return Array of all axes at the given position
   */
  IAxis[] getAxesAt( IAxisConstants.POSITION pos );

  void addAxis( IAxis axis );

  @SuppressWarnings( "rawtypes" )
  //Map<IAxis, IDataRange<Number>> getNumericRangeAxisSnapshot( );

  //<T> IDataOperator<T> getDataOperator( Class<T> clazz );

  void accept( IAxisVisitor visitor );

 // void accept( IMapperVisitor visitor );
  
  void clear();
}
