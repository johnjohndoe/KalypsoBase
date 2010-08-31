package de.openali.odysseus.service.ows.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class RequestBean
{
  Map<String, String[]> m_parameters = null;

  String m_url = null;

  public RequestBean( final HttpServletRequest request )
  {
    m_url = request.getRequestURL().toString() + "?" + request.getQueryString();
    m_parameters = preprocessParameters( request.getParameterMap() );
  }

  public String getUrl( )
  {
    return m_url;
  }

  public String getParameterValue( final String key )
  {
    final String[] values = m_parameters.get( key );
    if( (values != null) && (values.length > 0) )
      return values[0];
    else
      return null;
  }

  public String[] getParameterValues( final String key )
  {
    return m_parameters.get( key );
  }

  /**
   * preprocessing of parameter values; not implemented right now, but needed: parameter values can be case-insensitive
   */
  private Map<String, String[]> preprocessParameters( final Map< ? , ? > parameters )
  {
    final Map<String, String[]> newParameters = new HashMap<String, String[]>();

    final Set< ? > set = parameters.keySet();
    for( final Object object : set )
    {
      final String key = (String) object;
      final Object objValue = parameters.get( key );
      final String[] value = (String[]) objValue;
      newParameters.put( key, value );
    }
    return newParameters;
  }
}