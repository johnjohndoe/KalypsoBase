package org.kalypso.service.wps.server.operations;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * Operations for KalypsoServiceWPS have to implement this interface.
 * 
 * @author Holger Albert
 */
public interface IOperation
{
  /**
   * Executes the operation.
   * 
   * @param request
   *          The request bean consists of the parameters for the service and the URL from which the service was
   *          invoked.
   * @return The result.
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException;
}