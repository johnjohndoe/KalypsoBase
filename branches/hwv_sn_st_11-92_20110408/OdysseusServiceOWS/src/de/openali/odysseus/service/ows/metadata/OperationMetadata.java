package de.openali.odysseus.service.ows.metadata;

import java.util.List;

public class OperationMetadata
{

  private final String m_operationId;

  private final List<OperationParameter> m_parameters;

  private final String m_service;

  private final boolean m_isPublic;

  private final String m_description;

  public OperationMetadata( final String service, final String operationId, final String operationDescription, final List<OperationParameter> parameters, final boolean isPublic )
  {
    m_service = service;
    m_operationId = operationId;
    m_parameters = parameters;
    m_isPublic = isPublic;
    m_description = operationDescription;
  }

  public String getService( )
  {
    return m_service;
  }

  public String getOperationId( )
  {
    return m_operationId;
  }

  public List<OperationParameter> getParameters( )
  {
    return m_parameters;
  }

  public boolean isPublic( )
  {
    return m_isPublic;
  }

  public String getDescription( )
  {
    return m_description;
  }

}
