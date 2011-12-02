package de.openali.odysseus.service.ows.metadata;

import java.util.ArrayList;
import java.util.List;

public class ServiceMetadata
{

  private final String m_id;

  private final List<OperationMetadata> m_operations;

  public ServiceMetadata( final String id )
  {
    m_id = id;
    m_operations = new ArrayList<OperationMetadata>();
  }

  public String getId( )
  {
    return m_id;
  }

  public List<OperationMetadata> getOperations( )
  {
    return m_operations;
  }

  public void addOperation( final OperationMetadata om )
  {
    m_operations.add( om );
  }

}
