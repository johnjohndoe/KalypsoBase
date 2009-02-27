package de.openali.diagram.ext.base.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.openali.diagram.framework.model.data.IDataContainer;

public abstract class AbstractDomainValueData<T_domain extends Comparable, T_target extends Comparable> implements IDataContainer
{

	private boolean m_isOpen=false;
	protected List<T_domain> m_domainData=new ArrayList<T_domain>();
	protected List<T_target> m_targetData=new ArrayList<T_target>();
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
	
	public List<T_domain> getDomainData()
	{
		return m_domainData;
	}

	public List<T_target> getTargetData()
	{
		return m_targetData;
	}
	
	public void setInputFile(File f)
	{
		m_file=f;
	}
	
	public void close()
	{
		m_domainData=null;
		m_targetData=null;
		m_isOpen=false;
	}

}
