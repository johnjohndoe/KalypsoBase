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
package org.kalypso.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.services.processing.KalypsoWPSPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author skurzbach
 */
public class KnownServices
{
  private static final String PREF_SERVICES_NODE = "ogcwebservices";

  private static final String PREF_SERVICE_CAPABILITIES = "capabilities";

  private static final String ERROR_RESTORING = "Error restoring known services."; // TODO: translate

  private static KnownServices m_instance;

  /**
   * maps service title to service entry
   */
  private final Map<String, IServiceEntry> m_services;

  private final Preferences m_preferences;

  private KnownServices( )
  {
    // Singleton
    m_services = new HashMap<String, IServiceEntry>();
    m_preferences = KalypsoWPSPlugin.getDefault().getInstancePreferences().node( PREF_SERVICES_NODE );
  }

  public static KnownServices getInstance( )
  {
    if( m_instance == null )
    {
      m_instance = new KnownServices();
      try
      {
        m_instance.restore();
        m_instance.updateLocalServices();
      }
      catch( final BackingStoreException e )
      {
        e.printStackTrace();
        KalypsoWPSPlugin.log( IStatus.ERROR, ERROR_RESTORING, e );
      }
    }
    return m_instance;
  }

  private void updateLocalServices( ) throws BackingStoreException
  {
    final IOGCWebService[] localProcessingServices = KalypsoWPSPlugin.getDefault().getLocalProcessingServices();
    for( final IOGCWebService service : localProcessingServices )
    {
      addServiceEntry( service );
    }
  }

  private void addServiceEntry( final IOGCWebService service ) throws BackingStoreException
  {
    final IServiceEntry serviceEntry = ServiceEntryFactory.createServiceEntry( service );
    final Preferences newNode = m_preferences.node( serviceEntry.getTitle() );
    newNode.put( PREF_SERVICE_CAPABILITIES, serviceEntry.getCapabilitiesDocument() );
    m_services.put( serviceEntry.getTitle(), serviceEntry );
    newNode.flush();
    m_preferences.sync();
  }

  private void restore( ) throws BackingStoreException
  {
    for( final String key : m_preferences.childrenNames() )
    {
      final Preferences serviceNode = m_preferences.node( key );
      final String serviceDescription = serviceNode.get( PREF_SERVICE_CAPABILITIES, null );
      final IServiceEntry serviceEntry = ServiceEntryFactory.fromString( serviceDescription );
      m_services.put( serviceEntry.getTitle(), serviceEntry );
    }
  }

  public IServiceEntry[] getServices( )
  {
    return m_services.values().toArray( new IServiceEntry[m_services.size()] );
  }

}
