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
package org.kalypso.module.nature;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.kalypso.module.internal.Module;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Gernot Belger
 */
public class ModuleNature implements IProjectNature
{
  public final static String ID = Module.PLUGIN_ID + ".ModuleNature"; //$NON-NLS-1$

  private final String PREFERENCE_MODULE = "module";

  private final String PREFERENCE_VERSION = "version";

  private IProject m_project;

  /**
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  @Override
  public void configure( )
  {
    // does nothing by default
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  @Override
  public void deconfigure( )
  {
    // does nothing by default
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  @Override
  public IProject getProject( )
  {
    return m_project;
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
   */
  @Override
  public void setProject( final IProject project )
  {
    this.m_project = project;
  }

  public static final ModuleNature toThisNature( final IProject project )
  {
    try
    {
      return (ModuleNature) project.getNature( ID );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      Module.getDefault().getLog().log( e.getStatus() );
      return null;
    }
  }

  /**
   * Returns the nature specific project preferences.
   */
  public IEclipsePreferences getPreferences( )
  {
    final ProjectScope projectScope = new ProjectScope( getProject() );
    return projectScope.getNode( ID );
  }

  /**
   * Ensures that the given project is of this nature, setting it if necessary.<br/>
   * This is actually for backwards compatibility only. We should remove this method, as soon as we are sure that all
   * new projects automatically get the new nature (i.e. all templates are preconfigured with it).
   */
  public static ModuleNature enforceNature( final IProject project, final String moduleID ) throws CoreException
  {
    final ModuleNature existingNature = toThisNature( project );
    if( existingNature != null )
    {
      existingNature.checkModule( moduleID );
      return existingNature;
    }

    final IProjectDescription description = project.getDescription();
    final String[] natureIds = description.getNatureIds();

    /* Paranoid: check if my ID is contained in the description (should never happen) */
    final int indexOfId = ArrayUtils.indexOf( natureIds, ID );
    if( indexOfId != -1 )
      throw new IllegalStateException( "NatureId present but unable to akquire nature" );

    final String[] newNatureIds = (String[]) ArrayUtils.add( natureIds, ID );
    description.setNatureIds( newNatureIds );

    project.setDescription( description, null );

    final ModuleNature newNature = toThisNature( project );
    Assert.isNotNull( newNature );
    newNature.checkModule( moduleID );
    return newNature;
  }

  private void checkModule( final String moduleID ) throws CoreException
  {
    /* Set moduleID or check if the existing id is the same */
    final String projectModuleID = getModule();
    if( projectModuleID == null )
      writePreference( PREFERENCE_MODULE, moduleID );
    else
    {
      if( !projectModuleID.equals( moduleID ) )
      {
        final String msg = String.format( "Trying to set module ID (%s), but project already has a different module id: %s", moduleID, projectModuleID );
        final IStatus status = new Status( IStatus.ERROR, Module.PLUGIN_ID, msg );
        throw new CoreException( status );
      }
    }
  }

  private void writePreference( final String key, final String value )
  {
    try
    {
      final IEclipsePreferences preferences = getPreferences();
      preferences.put( key, value );
      preferences.flush();
    }
    catch( final BackingStoreException e )
    {
      final IStatus status = new Status( IStatus.ERROR, Module.PLUGIN_ID, "Failed to write preferences", e );
      Module.getDefault().getLog().log( status );
    }
  }

  // FIXME: change to IKalypsoModule later
  /**
   * Returns the module id of this project.
   */
  public String getModule( )
  {
    final IEclipsePreferences preferences = getPreferences();
    return preferences.get( PREFERENCE_MODULE, null );
  }

  /**
   * Returns the project version of this project.
   * 
   * @param May
   *          be <code>null</code>, if the version is not known.
   */
  public String getVersion( )
  {
    final IEclipsePreferences preferences = getPreferences();
    return preferences.get( PREFERENCE_VERSION, null );
  }

}
