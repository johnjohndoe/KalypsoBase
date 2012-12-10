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

import java.io.File;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.module.internal.Module;
import org.kalypso.module.internal.nature.ModuleFilePreferences;
import org.kalypso.module.internal.nature.ModulePreferences;
import org.osgi.framework.Version;

/**
 * @author Gernot Belger
 */
public class ModuleNature implements IProjectNature
{
  public final static String ID = Module.PLUGIN_ID + ".ModuleNature"; //$NON-NLS-1$

  private IProject m_project;

  private ModulePreferences m_preferences;

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
    m_project = project;

    m_preferences = new ModulePreferences( this );
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
      throw new IllegalStateException( "NatureId present but unable to akquire nature" ); //$NON-NLS-1$

    final String[] newNatureIds = (String[]) ArrayUtils.add( natureIds, ID );
    description.setNatureIds( newNatureIds );

    project.setDescription( description, null );

    final ModuleNature newNature = toThisNature( project );
    Assert.isNotNull( newNature );
    newNature.checkModule( moduleID );
    newNature.checkVersion();

    return newNature;
  }

  private void checkVersion( )
  {
    /* Find current kalypso version */
    // HM: what if we have different product using the same module?

    final IProduct product = Platform.getProduct();
    final Version kalypsoVersion = product.getDefiningBundle().getVersion();

    final Version currentVersion = getPreferences().getVersion();
    if( Version.emptyVersion.equals( currentVersion ) )
      getPreferences().setVersion( kalypsoVersion );
  }

  private void checkModule( final String moduleID ) throws CoreException
  {
    /* Set moduleID or check if the existing id is the same */
    final String projectModuleID = getModule();
    if( projectModuleID == null )
      getPreferences().setModule( moduleID );
    else
    {
      if( !projectModuleID.equals( moduleID ) )
      {
        final String msg = String.format( "Trying to set module ID (%s), but project already has a different module id: %s", moduleID, projectModuleID ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, Module.PLUGIN_ID, msg );
        throw new CoreException( status );
      }
    }
  }

  // FIXME: change to IKalypsoModule later
  /**
   * Returns the module id of this project.
   */
  public String getModule( )
  {
    return getPreferences().getModule();
  }

  public IModulePreferences getPreferences( )
  {
    return m_preferences;
  }

  /**
   * Returns module preferences for a project in the local file system.<br/>
   */
  public static IModulePreferences getPreferences( final File projectDir )
  {
    return new ModuleFilePreferences( projectDir );
  }

// /**
// * For backwards compatibility: let module decide, we ask each module if this project belongs to it.
// *
// * @return <code>null</code>, if no module accepts the gien project.
// */
// public static IKalypsoModule findModule( final IProject project )
// {
// final IKalypsoModule[] kalypsoModules = ModuleExtensions.getKalypsoModules();
// for( final IKalypsoModule module : kalypsoModules )
// {
// try
// {
// if( module.acceptProject( project ) )
// return module;
// }
// catch( final CoreException e )
// {
// Module.getDefault().getLog().log( e.getStatus() );
// }
// }
//
// return null;
// }

}
