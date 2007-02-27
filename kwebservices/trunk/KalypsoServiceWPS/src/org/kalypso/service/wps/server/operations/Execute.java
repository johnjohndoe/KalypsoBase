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
package org.kalypso.service.wps.server.operations;

import javax.xml.bind.JAXBException;

import net.opengeospatial.ows.CodeType;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.utils.Debug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.SimulationManager;
import org.kalypso.service.wps.utils.WPSUtilities;
import org.kalypso.simulation.core.ISimulation;
import org.kalypso.simulation.core.internal.local.LocalSimulationFactory;
import org.kalypso.simulation.core.internal.local.LocalURLCatalog;
import org.kalypso.simulation.core.internal.queued.QueuedSimulationService;

/**
 * This operation will execute a specific simulation.
 * 
 * @author Holger Albert
 */
public class Execute implements IOperation
{
  /**
   * The execute request. If this variable is set, the execute request came via xml. Otherwise it was only a get.
   */
  private net.opengeospatial.wps.Execute m_executeRequest = null;

  /**
   * The constructor.
   */
  public Execute( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( RequestBean request ) throws OWSException
  {
    /* Start the operation. */
    Debug.println( "Operation \"Execute\" started." );

    /* Get the simulation. */
    ISimulation simulation = WPSUtilities.getSimulation( getIdentifier( request ) );

    /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
    SimulationManager manager = SimulationManager.getInstance();
    manager.startSimulation( m_executeRequest.getIdentifier().getValue(), m_executeRequest.getIdentifier().getValue(), m_executeRequest );

    /* Prepare the execute response. */
    
    /* And send it back. */
    return null;
  }

  /**
   * Checks for the parameter or attribute Identifier and returns it. Furthermore it sets the member m_xxxRequest.
   * 
   * @param request
   *          The request.
   * @return The Identifier parameter or null if not present.
   */
  private String getIdentifier( RequestBean request ) throws OWSException
  {
    String simulationType = null;
    if( request.isPost() )
    {
      try
      {
        /* POST with XML. */
        String xml = request.getBody();

        /* Check if ALL parameter are available. */
        m_executeRequest = (net.opengeospatial.wps.Execute) MarshallUtilities.unmarshall( xml );

        /* Need the XML attribute service. */
        CodeType identifier = m_executeRequest.getIdentifier();
        if( identifier != null )
          simulationType = identifier.getValue();
      }
      catch( JAXBException e )
      {
        throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e.getMessage(), "" );
      }
    }
    else
    {
      /* GET or simple POST. */

      /* Search for the parameter Identifier. */
      simulationType = request.getParameterValue( "Identifier" );
    }

    if( simulationType == null || simulationType.length() == 0 )
    {
      Debug.println( "Missing parameter Identifier!" );
      throw new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Parameter Identifier is missing ...", "Identifier" );
    }

    return simulationType;
  }
}