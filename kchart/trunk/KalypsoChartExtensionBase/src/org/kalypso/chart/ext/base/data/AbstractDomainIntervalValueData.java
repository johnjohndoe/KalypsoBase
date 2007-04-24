package org.kalypso.chart.ext.base.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kalypso.chart.framework.model.data.IDataContainer;


public abstract class AbstractDomainIntervalValueData<T_domain, T_target> implements IDataContainer
{

	private boolean m_isOpen=false;
	protected List<T_domain> m_domainIntervalStartValues=new ArrayList<T_domain>();
	protected List<T_domain> m_domainIntervalEndValues=new ArrayList<T_domain>();
	protected List<T_domain> m_domainValues=new ArrayList<T_domain>();
	protected List<T_target> m_targetValues=new ArrayList<T_target>();
	protected File m_file;

	public boolean isOpen()
	{
		return m_isOpen;
	}

	public void open()
	{
		//nur öffnen, wenn nicht schon offen
		if (!m_isOpen)
		{
			System.out.println("opening Data: "+m_file.getAbsolutePath());
			m_isOpen=openData();
		}
	}
	
	public abstract boolean openData();
	
	public List<T_domain> getDomainDataIntervalStart()
	{
		return m_domainIntervalStartValues;
	}
	
	public List<T_domain> getDomainDataIntervalEnd()
	{
		return m_domainIntervalEndValues;
	}

	public List<T_domain> getDomainValues()
	{
		return m_domainValues;
	}
	
	public List<T_target> getTargetValues()
	{
		return m_targetValues;
	}
	
	public void setInputFile(File f)
	{
		m_file=f;
	}
	
	public void close()
	{
		m_domainIntervalStartValues=null;
		m_domainIntervalEndValues=null;
		m_targetValues=null;
		m_isOpen=false;
	}
	

}
