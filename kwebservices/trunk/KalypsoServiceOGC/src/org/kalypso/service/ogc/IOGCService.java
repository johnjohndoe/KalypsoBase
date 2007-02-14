package org.kalypso.service.ogc;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.kalypso.service.ogc.exception.OWSException;

public interface IOGCService
{

  /**
   * @param parameters
   * @param response
   * @throws OWSException
   */
  public void executeOperation( RequestBean request, ResponseBean response ) throws OWSException;

  /**
   * method will be called when the main servlet is unloaded; can be used to dispose swt-objects, etc.
   */
  public void destroy( );

}
