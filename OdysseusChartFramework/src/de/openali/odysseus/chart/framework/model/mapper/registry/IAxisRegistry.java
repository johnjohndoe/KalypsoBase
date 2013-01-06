package de.openali.odysseus.chart.framework.model.mapper.registry;

import de.openali.odysseus.chart.framework.model.event.IAxisRegistryEventListener;
import de.openali.odysseus.chart.framework.model.event.IEventProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants;

/**
 * @author alibu Interface describing a container for the chart axes; used to ensure that only one axis is present for
 *         each DataType
 */

public interface IAxisRegistry extends IEventProvider<IAxisRegistryEventListener>
{
  /**
   * @return Array of all present axes
   */
  IAxis[] getAxes( );

  IAxis getAxis( String id );

  /**
   * @return Array of all axes at the given position
   */
  IAxis[] getAxesAt( IAxisConstants.POSITION pos );

  void addAxis( IAxis axis );

  void accept( IAxisVisitor visitor );

  void clear( );
}