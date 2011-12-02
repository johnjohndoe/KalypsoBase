package de.openali.odysseus.service.ows.request;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * @author alibu
 */
public class ResponseBean
{
  private OutputStream m_out = null;

  private String m_contentType = null;

  public ResponseBean( final HttpServletResponse response )
  {
    try
    {
      m_out = response.getOutputStream();
    }
    catch( final IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Setzt den ContentType des OutputStream (z.B. "text/xml")
   */
  public void setContentType( final String contentType )
  {
    m_contentType = contentType;
  }

  /**
   * Gibt den ContentType des OutputStream (z.B. "text/xml") aus
   */
  public String getContentType( )
  {
    return m_contentType;
  }

  /**
   * Gibt das OutputStream-Objekt zurück
   */
  public OutputStream getOutputStream( )
  {
    return m_out;
  }

}
