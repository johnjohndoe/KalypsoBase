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

  /**
   * @return renderer for the given axis. If first looks up the renderer that were explicitely registered for a given
   *         axis. If no renderer is found for that axis, it looks up the renderer based on the dataClass of the axis.
   *         If still no renderer is found, it tries to find a renderer for a super class of the axis dataClass.
   */
  // public IAxisRenderer getRenderer( IAxis axis );

  /**
   * checks if a renderer with the given id is available in the registry and returns it oder null otherwise
   */
  // public IAxisRenderer getRenderer( String id );

// /**
// * sets the AxisRenderer for a particular axis, identified by - guess what - the axis' identifier
// */
// public void setRenderer( String identifier, IAxisRenderer renderer );
//
// /**
// * removes the renderer for a particular axis
// */
// public void unsetRenderer( String identifier );

  /**
   * @return the AxisComponent of the given axis or null if there isn't any
   */
// public IAxisComponent getComponent( IAxis axis );

  /**
   * sets the component for a particular axis
   */
// public void setComponent( IAxis axis, IAxisComponent comp );

  /**
   * @return map of Axis-AxisComponent-Pairs
   */
  // public Map<IAxis, IAxisComponent> getAxesToComponentsMap( );

  /**
   * removes all IAxis, IAxisRenderer and IAxisComponent entries
   */
  void clear( );

  void addMapper( IMapper mapper );

  Map<IAxis, IDataRange<Number>> getNumericRangeAxisSnapshot( );

  <T> IDataOperator<T> getDataOperator( Class<T> clazz );
}
