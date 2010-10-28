/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.module.internal.nature;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.kalypso.module.internal.Module;
import org.kalypso.module.nature.IModulePreferences;
import org.kalypso.module.nature.ModuleNature;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Gernot Belger
 */
public class ModulePreferences implements IModulePreferences
{
  private final IEclipsePreferences m_node;

  public ModulePreferences( final ModuleNature moduleNature )
  {
    final ProjectScope projectScope = new ProjectScope( moduleNature.getProject() );
    m_node = projectScope.getNode( ModuleNature.ID );
  }

  private void writePreference( final String key, final String value )
  {
    try
    {
      m_node.put( key, value );
      m_node.flush();
    }
    catch( final BackingStoreException e )
    {
      final IStatus status = new Status( IStatus.ERROR, Module.PLUGIN_ID, "Failed to write preferences", e ); //$NON-NLS-1$
      Module.getDefault().getLog().log( status );
    }
  }

  /**
   * @see org.kalypso.module.nature.IModulePreferences#setModule(java.lang.String)
   */
  @Override
  public void setModule( final String moduleID )
  {
    writePreference( PREFERENCE_MODULE, moduleID );
  }

  /**
   * @see org.kalypso.module.nature.IModulePreferences#getModule()
   */
  @Override
  public String getModule( )
  {
    return m_node.get( PREFERENCE_MODULE, null );
  }

  /**
   * @see org.kalypso.module.nature.IModulePreferences#setVersion(org.osgi.framework.Version)
   */
  @Override
  public void setVersion( final Version version )
  {
    final Version versionToSet = findVersionToSet( version );
    final String versionPref = versionToSet.equals( Version.emptyVersion ) ? null : versionToSet.toString();
    writePreference( PREFERENCE_VERSION, versionPref );
  }

  /**
   * For debug reasons: if we are working from eclipse, the qualifier is always set to 'qualifier' which is lexically
   * behin the numbers.<br/>
   * We replace this one with the empty string, so all debug version are less than a normal deploy version.
   */
  private Version findVersionToSet( final Version version )
  {
    final String qualifier = version.getQualifier();
    if( "qualifier".equals( qualifier ) ) //$NON-NLS-1$
      return new Version( version.getMajor(), version.getMinor(), version.getMicro() );

    return version;
  }

  /**
   * @see org.kalypso.module.nature.IModulePreferences#getVersion()
   */
  @Override
  public Version getVersion( )
  {
    final String property = m_node.get( PREFERENCE_VERSION, null );
    return ModuleUtils.parseVersion( property );
  }

}
