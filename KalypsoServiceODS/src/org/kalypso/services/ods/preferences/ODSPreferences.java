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
package org.kalypso.services.ods.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author alibu
 *
 */
public class ODSPreferences
{
  private static final String DEFAULT_CONFIG_PATH = Platform.getInstanceLocation().getURL().getPath() + "KalypsoODS/";

  private static final String DEFAULT_CONFIG_NAME = "Configuration.xml";

  private static final String DEFAULT_CAPABILITIES_PATH = DEFAULT_CONFIG_PATH;

  private static final String DEFAULT_CAPABILITIES_NAME = "Capabilities.xml";

  private static final String DEFAULT_SERVICE_ROOT = "http://www.example.org/ods";

  private static IEclipsePreferences m_prefs = new InstanceScope().getNode( "org.kalypso.services.ods" );

  public ODSPreferences( )
  {
    init();
  }

  public void init( )
  {
    //Mit Standarddaten initialisieren, falls keine Inhalte drin sind
    if( m_prefs != null )
    {
      boolean dirty = false;
      String tmp = m_prefs.get( IODSPreferences.CONFIG_PATH, "" );
      if( tmp.compareTo( "" ) == 0 )
      {
        m_prefs.put( IODSPreferences.CONFIG_PATH, DEFAULT_CONFIG_PATH );
        dirty = true;
      }
      tmp = m_prefs.get( IODSPreferences.CONFIG_NAME, "" );
      if( tmp.compareTo( "" ) == 0 )
      {
        m_prefs.put( IODSPreferences.CONFIG_NAME, DEFAULT_CONFIG_NAME );
        dirty = true;
      }

      tmp = m_prefs.get( IODSPreferences.CAPABILITIES_PATH, "" );
      if( tmp.compareTo( "" ) == 0 )
      {
        m_prefs.put( IODSPreferences.CAPABILITIES_PATH, DEFAULT_CAPABILITIES_PATH );
        dirty = true;
      }

      tmp = m_prefs.get( IODSPreferences.CAPABILITIES_NAME, "" );
      if( tmp.compareTo( "" ) == 0 )
      {
        m_prefs.put( IODSPreferences.CAPABILITIES_NAME, DEFAULT_CAPABILITIES_NAME );
        dirty = true;
      }

      tmp = m_prefs.get( IODSPreferences.SERVICE_ROOT, "" );
      if( tmp.compareTo( "" ) == 0 )
      {
        m_prefs.put( IODSPreferences.SERVICE_ROOT, DEFAULT_SERVICE_ROOT );
        dirty = true;
      }
      //Nur schreiben, wenn mind. 1 Wert nicht vorhanden war
      if (dirty)
      {
        try
        {
          m_prefs.flush();
        }
        catch( BackingStoreException e )
        {
          System.out.println( "Konnte Preferences nicht speichern" );
          e.printStackTrace();
        }
      }
    }
  }

  public IEclipsePreferences getPreferences( )
  {
    return m_prefs;
  }
}
