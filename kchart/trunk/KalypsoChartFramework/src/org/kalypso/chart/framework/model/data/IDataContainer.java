package org.kalypso.chart.framework.model.data;

import java.util.List;

public interface IDataContainer<T_domain extends Comparable, T_target extends Comparable>
{

	public void open();
	
	public void close();
	
	public boolean isOpen();
	
	public IDataRange<T_domain> getDomainRange();
	
	public IDataRange<T_target> getTargetRange();
	
	public List<T_domain> getDomainValues();
	
	public List<T_target> getTargetValues();
}
