package org.kalypso.service.ogc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

/**
 * Wraps some values of the servlet request.
 *
 * @author Alex Burtscher, Holger Albert
 */
public class RequestBean
{
  public enum TYPE
  {
    POST,
    GET;
  }

  /**
   * The type of the request.
   */
  private TYPE m_type = null;

  /**
   * The called URL.
   */
  private String m_url = null;

  /**
   * The parameters of the request.
   */
  private Map<String, String[]> m_parameters = null;

  /**
   * The body of the request.
   */
  private String m_body = null;

  /**
   * The constructor.
   *
   * @param type
   *          The type of the request beeing used. Can be RequestBean.TYPE.GET or RequestBean.TYPE.POST.
   * @param request
   *          The servlet request.
   */
  @SuppressWarnings("unchecked")
  public RequestBean( final TYPE type, final HttpServletRequest request )
  {
    m_type = type;

    m_url = request.getRequestURL().toString();
    m_parameters = preprocessParameters( request.getParameterMap() );
    m_body = readBody( request );
  }

  /**
   * Preprocessing of parameter values; not implemented right now, but needed: parameter values can be case-insensitive
   *
   * @param parameters
   *          The parameters of the servlet request.
   * @return The preprocessed parameters.
   */
  private Map<String, String[]> preprocessParameters( final Map<String, String[]> parameters )
  {
    final Map<String, String[]> newParameters = new HashMap<String, String[]>();

    final Set<String> set = parameters.keySet();
    for( final String key : set )
    {
      final String[] value = parameters.get( key );
      newParameters.put( key.toLowerCase(), value );
    }

    return newParameters;
  }

  /**
   * This function will read the body from the request and put it into a string.
   *
   * @param request
   *          The servlet request.
   */
  private String readBody( final HttpServletRequest request )
  {
    /* Only try to read the body, if the post-method was used. */
    if( !isPost() )
      return null;

    /* Only try to read, if the content type was set to text/xml. */
    if( !("text/xml".equals( request.getContentType() )) ) //$NON-NLS-1$
      return null;

    String body = null;
    BufferedReader reader = null;

    try
    {
      reader = request.getReader();

      final StringBuffer buffer = new StringBuffer();
      String line = ""; //$NON-NLS-1$

      while( (line = reader.readLine()) != null )
        buffer.append( line + "\n" ); //$NON-NLS-1$

      reader.close();
      body = buffer.toString();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }

    // System.out.println( body );

    return body;
  }

  /**
   * This function returns true, if the request was send with the post method.
   *
   * @boolean True, if the post method was used.
   */
  public boolean isPost( )
  {
    if( m_type == TYPE.POST )
      return true;

    return false;
  }

  /**
   * This function will return the called URL.
   *
   * @return The called URL.
   */
  public String getUrl( )
  {
    return m_url;
  }

  /**
   * This function returns a parameter value for a parameter name.
   *
   * @param key
   *          The parameter name.
   * @return The parameter value or null, if it does not exist.
   */
  public String getParameterValue( final String key )
  {
    final String[] values = m_parameters.get( key.toLowerCase() );
    if( values != null && values.length > 0 )
      return values[0];
    else
      return null;
  }

  /**
   * This function returns the values of the parameter.
   *
   * @param key
   *          The parameter name.
   * @return The parameter values.
   */
  public String[] getParameterValues( final String key )
  {
    return m_parameters.get( key.toLowerCase() );
  }

  /**
   * This function returns the body as string, if the post method was used and a body is available. Otherwise it returns
   * null.
   *
   * @return The body or null.
   */
  public String getBody( )
  {
    if( m_body == null || !isPost() )
      return null;

    return m_body;
  }
}