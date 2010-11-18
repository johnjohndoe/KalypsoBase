package de.openali.odysseus.chart.framework.model.mapper.registry;

import java.util.Map;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.event.IMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;

/**
 * @author alibu Interface describing a container for the chart axes; used to ensure that only one axis is present for
 *         each DataType
 */

public interface IMapperRegistry extends IEventProvider<IMapperRegistryEventListener>
{

  /**
   * @return Array of all present axes
   */
  IAxis[] getAxes( );

  IAxis getAxis( String id );

  IMapper getMapper( String id );

  /**
   * returns Array of all present mappers (excluding axes; if you need axes, try getAxes())
   */
  IMapper[] getMappers( );

  /**
   * @return Array of all axes at the given position
   */
  IAxis[] getAxesAt( IAxisConstants.POSITION pos );

  void addMapper( IMapper mapper );

  Map<IAxis, IDataRange<Number>> getNumericRangeAxisSnapshot( );

  <T> IDataOperator<T> getDataOperator( Class<T> clazz );
}
