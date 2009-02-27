package org.kalypso.service.ods.extension;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author burtscher simple interface to define service operations
 */
public interface IOWSOperation
{
  /**
   * starts ods operation - used for ods operation extension point
   */
  public void executeOperation( RequestBean requestBean, ResponseBean responseBean ) throws OWSException;

}
