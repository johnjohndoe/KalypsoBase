package de.openali.odysseus.chart.framework.model.data;

public interface IDataContainer<T_domain, T_target>
{
  void open( );

  void close( );

  boolean isOpen( );

  IDataRange<T_domain> getDomainRange( );

  IDataRange<T_target> getTargetRange( );
}
