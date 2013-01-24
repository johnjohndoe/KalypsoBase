package de.openali.odysseus.service.ows.metadata;

import java.util.List;

public class OperationParameter
{

  private final List<String> m_values;

  private final String m_name;

  private final String m_description;

  private final boolean m_isMandatory;

  public OperationParameter( final String name, final String description, final boolean isMandatory, final List<String> values )
  {
    m_values = values;
    m_name = name;
    m_description = description;
    m_isMandatory = isMandatory;
  }

  public boolean isMandatory( )
  {
    return m_isMandatory;
  }

  public String getName( )
  {
    return m_name;
  }

  public List<String> getValues( )
  {
    return m_values;
  }

  public String getDescription( )
  {
    return m_description;
  }
}
