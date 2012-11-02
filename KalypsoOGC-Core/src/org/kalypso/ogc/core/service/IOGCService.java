/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.core.service;

import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;

/**
 * This interface should be implemented by classes, that should provide a OGC service.
 * 
 * @author Toni DiNardo
 */
public interface IOGCService
{
  /**
   * This constant defines the name of the service parameter.
   */
  public static final String PARAMETER_SERVICE = "SERVICE"; //$NON-NLS-1$

  /**
   * This constant defines the name of the request parameter.
   */
  public static final String PARAMETER_REQUEST = "REQUEST"; //$NON-NLS-1$

  /**
   * This constant defines the name of the version parameter.
   */
  public static final String PARAMETER_VERSION = "VERSION"; //$NON-NLS-1$

  /**
   * This constant defines the name of the acceptVersions parameter.
   */
  public static final String PARAMETER_ACCEPT_VERSIONS = "ACCEPTVERSIONS"; //$NON-NLS-1$

  /**
   * This constant defines the name of the language parameter.
   */
  public static final String PARAMETER_LANGUAGE = "LANGUAGE"; //$NON-NLS-1$

  /**
   * This constant defines the name of the GetCababilities operation.
   */
  public static final String OPERATION_GET_CAPABILITIES = "GetCapabilities"; //$NON-NLS-1$

  /**
   * This constant defines the name of the DescribeProcess operation.
   */
  public static final String OPERATION_DESCRIBE_PROCESS = "DescribeProcess"; //$NON-NLS-1$

  /**
   * This constant defines the name of the Execute operation.
   */
  public static final String OPERATION_EXECUTE = "Execute"; //$NON-NLS-1$

  /**
   * This function returns the name of the OGC service.
   * 
   * @return The name of the OGC service.
   */
  public String getName( );

  /**
   * This function returns the version of the OGC service.
   * 
   * @return The version of the OGC service.
   */
  public String getVersion( );

  /**
   * This function is called to execute an OGC operation of the OGC service.
   * 
   * @param request
   *          The OGC request.
   * @param response
   *          The OGC response.
   * @param operation
   *          The OGC operation.
   */
  public void execute( OGCRequest request, OGCResponse response, IOGCOperation operation ) throws OWSException;

  /**
   * This function is called after the operation was executed.
   */
  public void dispose( );
}