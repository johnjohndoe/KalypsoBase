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
package org.kalypso.project.database.client.extension;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.action.IAction;
import org.kalypso.afgui.wizards.INewProjectWizardProvider;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.i18n.I18nUtils;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.project.database.client.extension.database.IKalypsoModuleDatabaseSettings;
import org.kalypso.project.database.client.i18n.Messages;
import org.kalypso.project.database.client.ui.composites.CreateProjectAction;
import org.kalypso.project.database.client.ui.composites.ImportProjectAction;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * @author Gernot Belger
 */
public abstract class AbstractKalypsoModule implements IKalypsoModule, IExecutableExtension
{
  private Version m_version;

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  @Override
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    final String pluginid = config.getContributor().getName();
    final Bundle bundle = Platform.getBundle( pluginid );
    m_version = bundle.getVersion();
  }

  protected URL getInfoURL( final Class< ? > clazz, final Plugin plugin )
  {
    final IPath stateLocation = plugin.getStateLocation();
    final File targetDir = new File( stateLocation.toFile(), "infoPage" ); //$NON-NLS-1$

    try
    {
      /* info page of plugin */
      final URL zipURL = I18nUtils.getLocaleResource( clazz, "infoPage", ".zip" ); //$NON-NLS-1$ //$NON-NLS-2$
      ZipUtilities.unzip( zipURL, targetDir );

      final File targetFile = I18nUtils.getLocaleFile( targetDir, "index", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
      if( targetFile == null )
        return null;

      return targetFile.toURI().toURL();
    }
    catch( final Exception e )
    {
      plugin.getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return null;
    }
  }

  /**
   * @see org.kalypso.project.database.client.extension.IKalypsoModule#getVersion()
   */
  @Override
  public Version getVersion( )
  {
    return m_version;
  }

  /**
   * @see org.kalypso.project.database.client.extension.IKalypsoModule#getProjectActions()
   */
  @Override
  public IAction[] getProjectActions( )
  {
    final Collection<IAction> actions = new ArrayList<IAction>();

    /* Create project actions */
    final INewProjectWizardProvider newProjectWizard = getNewProjectWizard();
    final String commitType = ((IKalypsoModuleDatabaseSettings)getDatabaseSettings()).getModuleCommitType();
    if( newProjectWizard != null )
    {
      final String createProjectLabel = Messages.getString( "org.kalypso.project.database.client.ui.composites.ModulePageComposite.1" ); //$NON-NLS-1$
      final CreateProjectAction createProjectAction = new CreateProjectAction( createProjectLabel, commitType, newProjectWizard );
      actions.add( createProjectAction );
    }
    else
      actions.add( null );

    /* Import external project */
    actions.add( new ImportProjectAction() );

    /* Demo project */
    final INewProjectWizardProvider demoProjectWizard = getDemoProjectWizard();
    if( demoProjectWizard != null )
    {
      final String demoProjectLabel = Messages.getString( "org.kalypso.project.database.client.ui.composites.ModulePageComposite.2" ); //$NON-NLS-1$
      final CreateProjectAction demoProjectAction = new CreateProjectAction( demoProjectLabel, commitType, demoProjectWizard );
      demoProjectAction.setImageDescriptor( CreateProjectAction.IMG_EXTRACT_DEMO );
      actions.add( demoProjectAction );
    }
    else
      actions.add( null );

    /* more module specific actions */
    addProjectActions( actions );

    return actions.toArray( new IAction[actions.size()] );
  }

  protected abstract INewProjectWizardProvider getNewProjectWizard( );

  protected abstract INewProjectWizardProvider getDemoProjectWizard( );

  protected void addProjectActions( final Collection<IAction> actions )
  {
    // adds one placeholder by default
    actions.add( null );
  }

}
