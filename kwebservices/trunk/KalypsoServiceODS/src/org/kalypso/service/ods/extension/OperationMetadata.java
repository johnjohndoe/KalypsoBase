package org.kalypso.service.ods.extension;

import java.util.List;

public class OperationMetadata {
	
	private String m_operationId;
	private String m_description;
	private String m_version;
	private List<OperationParameter> m_parameters;
	private String m_service;
	
	
	public OperationMetadata(String service, String operationId, String description, String version, List<OperationParameter> parameters)
	{
		m_service=service;
		m_operationId=operationId;
		m_description=description;
		m_version=version;
		m_parameters=parameters;
	}
	
	public String getService()
	{
		return m_service;
	}
	
	public String getOperationId()
	{
		return m_operationId;
	}
	
	public String getDescription()
	{
		return m_description;
	}
	
	public String getVersion()
	{
		return m_version;
	}
	
	public List<OperationParameter> getParameters()
	{
		return m_parameters;
	}
	
	

}
