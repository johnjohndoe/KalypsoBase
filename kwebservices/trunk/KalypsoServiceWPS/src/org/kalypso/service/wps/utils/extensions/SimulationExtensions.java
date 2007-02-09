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
package org.kalypso.service.wps.utils.extensions;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.simulation.core.ISimulation;

/**
 * This class manages the extensions of the org.kalypso.service.wps extension point.
 * 
 * @author Holger Albert
 */
public class SimulationExtensions
{
  /**
   * Stores the simulations.
   */
  private HashMap<String, IConfigurationElement> SIMULATIONS = null;

  /**
   * The singleton instance.
   */
  private static SimulationExtensions instance = null;

  /**
   * The constructor.
   */
  private SimulationExtensions( )
  {
  }

  /**
   * This function returns an instance of the class SimulationExtensions.
   * 
   * @return The instance of this class.
   */
  public static SimulationExtensions getInstance( )
  {
    if( instance == null )
      instance = new SimulationExtensions();

    return instance;
  }

  /**
   * This function will build the ISimulation requested.
   * 
   * @param id
   *          The literal used in the extension point, where the ISimulation is registered.
   * @return The ISimulation wanted or null, if it is not registerd.
   */
  public ISimulation getSimulation( final String id ) throws CoreException
  {
    /* Initialize, if it is not already done. */
    init();

    /* Try to get the simulation. */
    IConfigurationElement element = SIMULATIONS.get( id );

    if( element == null )
      return null;

    return (ISimulation) element.createExecutableExtension( "Class" );
  }

  /**
   * This function builds all ISimulations registered and returns them.
   * 
   * @return A HashMap containing all registered ISimulations as values.<br>
   *         The keys will be the literal described in the extension point.<br>
   *         If no ISimulation is registered, it will return an empty HashMap.
   */
  public HashMap<String, ISimulation> getSimulations( ) throws CoreException
  {
    /* Initialize, if it is not already done. */
    init();

    /* The map for saving the simulations. */
    HashMap<String, ISimulation> sims = new HashMap<String, ISimulation>();

    Iterator<String> itr = SIMULATIONS.keySet().iterator();
    while( itr.hasNext() )
    {
      String id = itr.next();
      IConfigurationElement element = SIMULATIONS.get( id );

      sims.put( id, (ISimulation) element.createExecutableExtension( "Class" ) );
    }

    return sims;
  }

  /**
   * This function initializes the configuration elements.<br>
   * It fills the list with the elements registered via extension points to the WPS.<br>
   * <br>
   * If the list is already there, it does nothing.
   */
  private void init( )
  {
    if( SIMULATIONS != null )
      return;

    SIMULATIONS = new HashMap<String, IConfigurationElement>();

    final IExtensionRegistry er = Platform.getExtensionRegistry();

    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.service.wps" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "Literal" );
        SIMULATIONS.put( id, element );
      }
    }
  }
}