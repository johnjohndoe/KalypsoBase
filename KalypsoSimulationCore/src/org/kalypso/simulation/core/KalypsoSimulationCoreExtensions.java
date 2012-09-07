/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.simulation.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.net.IUrlCatalog;
import org.kalypso.simulation.core.i18n.Messages;
import org.kalypso.simulation.core.internal.FailureService;
import org.kalypso.simulation.core.refactoring.ISimulationRunner;

/**
 * Helper class to read extension points from registry.
 *
 * @author Belger
 */
public class KalypsoSimulationCoreExtensions
{
  private final static String EXT_ELEMENT_SERVICE = "simulationService"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_SERVICECLASS = "class"; //$NON-NLS-1$

  private final static String EXT_ELEMENT_SIMULATION = "simulation"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_SIMULATIONID = "typeID"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_SIMULATIONCLASS = "simulationClass"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_CATALOGCLASS = "catalogClass"; //$NON-NLS-1$

  private static final Object EXT_NAME_SIMULATION = "simulation"; //$NON-NLS-1$

  private static final Object EXT_NAME_SIMULATION_RUNNER = "simulationRunner"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_ID = "id"; //$NON-NLS-1$

  private final static String EXT_ATTRIB_CLASS = "class"; //$NON-NLS-1$

  /**
   * Reads the extension point.<br/>
   * REMARK: we hash with the typeID, so if anyone tries to register another simulation for the same typeID, only one
   * survives. </p>
   * <p>
   * Maybe it is better not to hash, and let the user decide which implementation to use.
   * </p>
   */
  private static Map<String, IConfigurationElement> readSimulations( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint point = registry.getExtensionPoint( KalypsoSimulationCorePlugin.getID(), EXT_ELEMENT_SIMULATION );

    final IExtension[] extensions = point.getExtensions();

    final Map<String, IConfigurationElement> elements = new HashMap<>();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] configurationElements = extension.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        if( EXT_NAME_SIMULATION.equals( element.getName() ) )
        {
          final String typeID = element.getAttribute( EXT_ATTRIB_SIMULATIONID );
          elements.put( typeID, element );
        }
      }
    }

    return elements;
  }

  /**
   * Adds all defined {@link IUrlCatalog}s to the given list.
   *
   * @return A status indicating the success of the process.
   */
  public static IStatus createCatalogs( final List<IUrlCatalog> catalogs )
  {
    final Map<String, IConfigurationElement> elements = readSimulations();
    final MultiStatus status = new MultiStatus( KalypsoSimulationCorePlugin.getID(), 0, Messages.getString( "org.kalypso.simulation.core.KalypsoSimulationCoreExtensions.0" ), null ); //$NON-NLS-1$
    // final List<IUrlCatalog> catalogs = new ArrayList<IUrlCatalog>( elements.size() );
    for( final IConfigurationElement element : elements.values() )
    {
      if( element.getAttribute( EXT_ATTRIB_CATALOGCLASS ) != null )
      {
        try
        {
          final IUrlCatalog catalog = (IUrlCatalog) element.createExecutableExtension( EXT_ATTRIB_CATALOGCLASS );
          catalogs.add( catalog );
        }
        catch( final CoreException e )
        {
          // element.getAttribute( "id" );
          status.add( e.getStatus() );
        }
      }
      else
      {
        // TODO: put this into a tracing-option; this is NOT an error/problem
// Logger.getLogger( KalypsoSimulationCoreExtensions.class.getName() ).warning( "<" + EXT_ATTRIB_CATALOGCLASS +
        // "> attribute is not defined for definition with typeID: "
// + element.getAttribute( EXT_ATTRIB_SIMULATIONID ) );
      }
    }

    return status;
  }

  /** @return null, if the typeID is unknown */
  public static ISimulation createSimulation( final String typeID ) throws CoreException
  {
    final Map<String, IConfigurationElement> elements = readSimulations();
    final IConfigurationElement element = elements.get( typeID );
    if( element == null )
      return null;

    return (ISimulation) element.createExecutableExtension( EXT_ATTRIB_SIMULATIONCLASS );
  }

  /**
   * This function creates a linked list with all simulations.
   *
   * @return All simulations.
   */
  public static List<ISimulation> createSimulations( ) throws CoreException
  {
    final List<ISimulation> simulations = new LinkedList<>();

    final Map<String, IConfigurationElement> elements = readSimulations();
    final Iterator<String> keys = elements.keySet().iterator();
    while( keys.hasNext() )
    {
      final String key = keys.next();
      final IConfigurationElement element = elements.get( key );

      /* Add the simulation. */
      simulations.add( (ISimulation) element.createExecutableExtension( EXT_ATTRIB_SIMULATIONCLASS ) );
    }

    return simulations;
  }

  public static ISimulationRunner createSimulationRunner( final String id ) throws CoreException
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint point = registry.getExtensionPoint( KalypsoSimulationCorePlugin.getID(), EXT_ELEMENT_SIMULATION );

    final IExtension[] extensions = point.getExtensions();

    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] configurationElements = extension.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        if( EXT_NAME_SIMULATION_RUNNER.equals( element.getName() ) )
        {
          final String elementId = element.getAttribute( EXT_ATTRIB_ID );
          if( elementId.equals( id ) )
            return (ISimulationRunner) element.createExecutableExtension( EXT_ATTRIB_CLASS );
        }
      }
    }

    final String messsage = String.format( "Failed to instantiate simulation runner with id '%s'", id ); //$NON-NLS-1$
    final IStatus status = new Status( IStatus.ERROR, KalypsoSimulationCorePlugin.getID(), messsage );
    throw new CoreException( status );
  }

  public static String[] getRegisteredTypeIDs( )
  {
    final Map<String, IConfigurationElement> elements = readSimulations();
    return elements.keySet().toArray( new String[elements.size()] );
  }

  private static List<IConfigurationElement> readServices( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint point = registry.getExtensionPoint( KalypsoSimulationCorePlugin.getID(), EXT_ELEMENT_SERVICE );

    final IExtension[] extensions = point.getExtensions();

    final List<IConfigurationElement> services = new ArrayList<>();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] configurationElements = extension.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
        services.add( element );
    }

    return services;
  }

  public static ISimulationService[] createServices( )
  {
    final List<IConfigurationElement> serviceElements = readServices();
    final List<ISimulationService> services = new ArrayList<>( serviceElements.size() );
    for( final IConfigurationElement element : serviceElements )
    {
      try
      {
        final ISimulationService service = (ISimulationService) element.createExecutableExtension( EXT_ATTRIB_SERVICECLASS );
        services.add( service );
      }
      catch( final CoreException e )
      {
        e.printStackTrace();

        // instead of ignoring, we add this 'failure'-service. So the service still shows in any lists, but invokation
        // show what originally happend.
        services.add( new FailureService( new SimulationException( Messages.getString( "org.kalypso.simulation.core.KalypsoSimulationCoreExtensions.1" ), e ) ) ); //$NON-NLS-1$
      }
    }

    return services.toArray( new ISimulationService[services.size()] );
  }
}
