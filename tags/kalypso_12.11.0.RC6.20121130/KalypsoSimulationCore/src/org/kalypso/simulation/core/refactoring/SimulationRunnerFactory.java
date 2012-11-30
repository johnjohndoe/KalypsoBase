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
package org.kalypso.simulation.core.refactoring;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.simulation.core.KalypsoSimulationCoreExtensions;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.core.simspec.Modeldata.Input;
import org.kalypso.simulation.core.simspec.Modeldata.Output;

/**
 * @author Dirk Kuch
 */
public class SimulationRunnerFactory
{
  // FIXME: do not belong here
  public static final String WPS_ENDPOINT_PROPERTY = "org.kalypso.service.wps.service"; //$NON-NLS-1$

  // FIXME: do not belong here
  public static final String WPS_USE_ENDPOINT_PROPERTY = "org.kalypso.hwv.use.wps"; //$NON-NLS-1$

  /**
   * FIXME refactoring
   *
   * <pre>
   *
   * final ISimulationRunner runner = SimulationRunnerFacotry.createRunner( typeID );
   * runner.getSpec();
   *
   * final String typeID = modeldata.getTypeID();
   *
   * // ‹bersetzung modeldata -&gt; hashmap
   * // - Ableich modelspec/modeldata
   *
   * // modelspec -&gt; Map&lt;String, Object&gt;
   * // - Literal: String, Double, Integer
   * // - ComplexValueType: Feature/Image
   * // - ComplexReferenceType: URL/URI
   *
   * final IStatus status = runner.run( Map &lt; String, Object &gt; inputs, List &lt; String &gt; outputs, progress );
   *
   * </pre>
   */

  /**
   * Always creates a local runner
   */
  public static ISimulationRunner createRunner( final Modeldata modeldata, final URL inputDir ) throws CoreException
  {
    return createRunner( modeldata, inputDir, null, null );
  }

  /**
   * Create a remote runner, if an endpoint is set and is configured to be used, depending on the given system
   * properties.<br/>
   * If no endpoint is found, return a local runner.
   */
  public static ISimulationRunner createRunner( final Modeldata modeldata, final URL inputDir, final String endpointProperty, final String useEndpointProperty ) throws CoreException
  {
    final ISimulationRunner runner = createRunner( endpointProperty, useEndpointProperty );
    runner.init( modeldata, inputDir );
    return runner;
  }

  private static ISimulationRunner createRunner( final String endpointProperty, final String useEndpointProperty ) throws CoreException
  {
    final boolean remoteCalculation = useEndpoint(endpointProperty, useEndpointProperty);
    if( remoteCalculation )
      return KalypsoSimulationCoreExtensions.createSimulationRunner( "wps" ); //$NON-NLS-1$
    else
      return KalypsoSimulationCoreExtensions.createSimulationRunner( "local" ); //$NON-NLS-1$
  }

  private static boolean useEndpoint( final String endpointProperty, final String useEndpointProperty )
  {
    /* endpoint not configured: cannot execute remote */
    if( StringUtils.isBlank( endpointProperty ) )
      return false;

    // REMARK: very crude: If no WPS-Endpoint is configured, we try to start the calculation locally
    final String serviceEndpoint = System.getProperty( endpointProperty );

    /* If use property is not set, use wps if endpoint is defined */
    if( StringUtils.isBlank( useEndpointProperty ) )
      return !StringUtils.isBlank( serviceEndpoint );

    // REMARK: Instead of the WPS-Endpoint a different system property is checked...
    // TODO: We should introduce an abstraction for all available WPS (including a 'fake' local one)
    // and find out, which ones are available for calculation this typeID.
    // If more than one is available, the user should be able to choose.
    return Boolean.parseBoolean( System.getProperty( useEndpointProperty, Boolean.FALSE.toString() ) ); //$NON-NLS-1$
  }

  public static List<String> resolveOutputs( final List<Output> output )
  {
    final List<String> myOutputs = new ArrayList<>();
    for( final Output o : output )
    {
      myOutputs.add( o.getId() );
    }

    return myOutputs;
  }

  public static Map<String, Object> resolveInputs( final List<Input> input )
  {
    final Map<String, Object> myInputs = new HashMap<>();

    for( final Input i : input )
    {
      myInputs.put( i.getId(), i.getPath() );
    }

    return myInputs;
  }

  public static String getServiceEndpoint( )
  {
    return System.getProperty( "org.kalypso.service.wps.service" ); //$NON-NLS-1$
  }
}