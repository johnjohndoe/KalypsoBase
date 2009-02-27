package org.kalypso.chart.ext.observation.data;

import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;

public class TupleResultDomainIntervalValueData<T_domain, T_target> implements IDataContainer<T_domain, T_target>
{

  public void close( )
  {
    // TODO Auto-generated method stub

  }

  public boolean isOpen( )
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void open( )
  {

  }

  public IDataRange<T_domain> getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  public IDataRange<T_target> getTargetRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  public T_domain[] getDomainValues( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  public T_target[] getTargetValues( )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
