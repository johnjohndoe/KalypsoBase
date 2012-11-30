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
package org.kalypso.afgui.wizards;

import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.jface.wizard.ProjectTemplatePage;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.INewProjectHandler;
import org.kalypso.module.ISetAsBaseScenarioHandler;
import org.kalypso.module.ModuleExtensions;

import de.renew.workflow.connector.cases.CopyScenarioContentsOperation;
import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Holger Albert
 */
public class SetAsBaseScenarioWizard extends NewProjectWizard
{
  private final IScenario m_scenario;

  public SetAsBaseScenarioWizard( final String categoryId, final String moduleID, final IScenario scenario )
  {
    super( categoryId, false, moduleID );

    m_scenario = scenario;

    final ProjectTemplatePage templatePage = getTemplatePage();
    templatePage.selectTemplate( Locale.getDefault().getLanguage() );
  }

  @Override
  public IStatus postCreateProject( final IProject project, final ProjectTemplate template, IProgressMonitor monitor )
  {
    if( monitor == null )
      monitor = new NullProgressMonitor();

    monitor.beginTask( String.format( Messages.getString("SetAsBaseScenarioWizard_0"), m_scenario.getName() ), 1000 ); //$NON-NLS-1$
    monitor.subTask( Messages.getString("SetAsBaseScenarioWizard_1") ); //$NON-NLS-1$

    try
    {
      final String moduleID = getModuleID();
      final IKalypsoModule module = ModuleExtensions.getKalypsoModule( moduleID );

      final INewProjectHandler newHandler = module.getNewProjectHandler();
      if( newHandler != null )
        newHandler.postCreateProject( project, template, new SubProgressMonitor( monitor, 100 ) );

      final ISetAsBaseScenarioHandler setAsHandler = module.getSetAsBaseScenarioHandler();
      if( setAsHandler != null )
        setAsHandler.postCreateProject( m_scenario.getProject(), project, new SubProgressMonitor( monitor, 100 ) );

      final IFolder sourceFolder = m_scenario.getFolder();
      final IFolder derivedFolder = m_scenario.getDerivedFolder();
      final IFolder targetFolder = project.getFolder( "Basis" ); //$NON-NLS-1$

      IFolder[] ignoreFolders = m_scenario.getSetAsBaseScenarioBlackList();
      if( !sourceFolder.equals( derivedFolder ) )
        ignoreFolders = ArrayUtils.add( ignoreFolders, derivedFolder );

      final CopyScenarioContentsOperation operation = new CopyScenarioContentsOperation( sourceFolder, targetFolder, ignoreFolders, null );
      return operation.execute( new SubProgressMonitor( monitor, 800 ) );
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex );
    }
    finally
    {
      monitor.done();
    }
  }
}