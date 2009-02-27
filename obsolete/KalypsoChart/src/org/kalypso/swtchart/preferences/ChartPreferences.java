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
package org.kalypso.swtchart.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * @author alibu
 */
public class ChartPreferences
{
  private static final String DEFAULT_CONFIG_PATH = Platform.getInstanceLocation().getURL().getPath() + "KalypsoChart/";

  private static final String DEFAULT_CONFIG_NAME = "Configuration.xml";

  private static IEclipsePreferences m_prefs = new InstanceScope().getNode( "org.kalypso.chart" );

  public ChartPreferences( )
  {
    init();
  }

  public void init( )
  {
    // Mit Standarddaten initialisieren, falls keine Inhalte drin sind
    if( m_prefs != null )
    {
      String tmp = m_prefs.get( IChartPreferences.CONFIG_PATH, "" );
      if( tmp.compareTo( "" ) == 0 )
        m_prefs.put( IChartPreferences.CONFIG_PATH, DEFAULT_CONFIG_PATH );

      tmp = m_prefs.get( IChartPreferences.CONFIG_NAME, "" );
      if( tmp.compareTo( "" ) == 0 )
        m_prefs.put( IChartPreferences.CONFIG_NAME, DEFAULT_CONFIG_NAME );
    }
  }

  public IEclipsePreferences getPreferences( )
  {
    return m_prefs;
  }
}
