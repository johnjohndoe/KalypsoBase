package org.kalypso.service.wps.server.operations;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * The implementation of the GetCapabilities function.
 * 
 * @author Holger Albert
 */
public class GetCapabilities implements IOperation
{
  /**
   * The constructor.
   */
  public GetCapabilities( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    return null;
  }
}