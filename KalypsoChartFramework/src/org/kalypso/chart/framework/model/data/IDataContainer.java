package org.kalypso.chart.framework.model.data;

public interface IDataContainer<T_domain, T_target>
{
  public void open( );

  public void close( );

  public boolean isOpen( );

  public IDataRange<T_domain> getDomainRange( );

  public IDataRange<T_target> getTargetRange( );
}
