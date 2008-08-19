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
package org.kalypso.service.unittests.literals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.kalypso.service.wps.client.WPSRequest;

/**
 * Small test case for testing the literals simulation.
 * 
 * @author Holger Albert
 */
public class LiteralsJobTest
{
  private static final String INPUT_LITERAL = "INPUT_LITERAL";

  private static final String OUTPUT_LITERAL = "OUTPUT_LITERAL";

  /**
   * This function starts the simulation client, which will contact the wps to activate the literals simulation.
   */
  @Test
  public void testJob( )
  {
    /* Location of the Server, where the simulations will run: No default. */
    System.setProperty( "org.kalypso.service.wps.service", "http://localhost/ogc" );

    /* Location, where the client can put its input data: No default. */
    System.setProperty( "org.kalypso.service.wps.input", "" );

    /* Replacement for providing the URL to the server: No default. */
    System.setProperty( "org.kalypso.service.wps.server.replacement", "" );

    /* Create the inputs. */
    Map<String, Object> inputs = new HashMap<String, Object>();
    inputs.put( INPUT_LITERAL, "1.0" );

    /* Create the outputs. */
    List<String> outputs = new ArrayList<String>();
    outputs.add( OUTPUT_LITERAL );

    /* The name of the simulation. */
    String simulationName = "LiteralsV1.0";

    /* Create the delegate which can handle ISimulations. */
    String serviceEndpoint = System.getProperty( WPSRequest.SYSTEM_PROP_WPS_ENDPOINT );

    /* Start the simulation with a timeout of 300000 ms. */
    WPSRequest simulationJob = new WPSRequest( simulationName, serviceEndpoint, 300000 );
    simulationJob.init( new NullProgressMonitor() );
    IStatus status = simulationJob.run( inputs, outputs, new NullProgressMonitor() );

    /* The end report. */
    System.out.println( status.getMessage() );

    /* Get the result. */
    Map<String, Object> literals = simulationJob.getLiterals();

    /* Assert. */
    Assert.assertNotNull( literals );

    /* The result. */
    System.out.println( literals.get( OUTPUT_LITERAL ) );
  }
}