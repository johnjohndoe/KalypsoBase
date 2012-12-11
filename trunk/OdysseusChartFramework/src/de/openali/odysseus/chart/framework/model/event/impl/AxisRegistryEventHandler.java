package de.openali.odysseus.chart.framework.model.event.impl;

import de.openali.odysseus.chart.framework.model.event.IAxisRegistryEventListener;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

/**
 * @author alibu
 */
public class AxisRegistryEventHandler extends AbstractEventProvider<IAxisRegistryEventListener>
{
  public void fireAxisAdded( final IAxis< ? > axis )
  {
    for( final IAxisRegistryEventListener l : getListeners( IAxisRegistryEventListener.class ) )
    {
      l.onAxisAdded( axis );
    }

  }

  public void fireAxisRemoved( final IAxis< ? > axis )
  {
    for( final IAxisRegistryEventListener l : getListeners( IAxisRegistryEventListener.class ) )
    {
      l.onAxisRemoved( axis );
    }

  }

  public void fireAxisChanged( final IAxis< ? > axis )
  {
    for( final IAxisRegistryEventListener l : getListeners( IAxisRegistryEventListener.class ) )
    {
      l.onAxisChanged( axis );
    }

  }
}
