package org.kalypso.service.ogc;

import org.kalypso.service.ogc.exception.OWSException;

/**
 * Interface for services, which should be run by the OGCServlet.
 * 
 * @author Holger Albert
 */
public interface IOGCService
{
  /**
   * This function executes the operation.
   * 
   * @param request
   *          The request type, request parameters and if the post method was used for the request the post body.
   * @param response
   *          Some response values, which has to be set.
   * @throws OWSException
   */
  public void executeOperation( RequestBean request, ResponseBean response ) throws OWSException;

  /**
   * This function returns true, if the service can do something with the request.
   * 
   * @param request
   *          The request object, containing the parameters, etc.
   */
  public boolean responsibleFor( RequestBean request );

  /**
   * This method will be called when the main servlet is unloaded. It can be used to dispose swt-objects, etc.
   */
  public void destroy( );
}