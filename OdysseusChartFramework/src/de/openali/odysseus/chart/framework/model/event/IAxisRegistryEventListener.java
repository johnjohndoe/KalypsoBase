package de.openali.odysseus.chart.framework.model.event;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public interface IAxisRegistryEventListener
{
  void onAxisAdded( IAxis<?> axis );

  void onAxisRemoved( IAxis< ? > axis );

  void onAxisChanged( IAxis< ? > axis );
}
