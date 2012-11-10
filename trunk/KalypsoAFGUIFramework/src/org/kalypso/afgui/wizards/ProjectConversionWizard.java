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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.contribs.eclipse.jface.wizard.ProjectTemplatePage;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.module.INewProjectHandler;
import org.kalypso.module.ModuleExtensions;
import org.kalypso.module.conversion.IProjectConversionOperation;
import org.kalypso.module.conversion.ProjectConversionPage;

import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * FIXME: generalize: should be useable for all modules.<br/>
 * This wizard converts project of old KalypsoHydrology versions into the current Kalypso version by creating a new
 * project and copying the the old data to the right places.<br/>
 * 
 * @author Gernot Belger
 */
public class ProjectConversionWizard extends NewProjectWizard
{
  private ProjectConversionPage m_conversionPage;

  private final String m_moduleID;

  private final INewProjectHandler m_handler;

  // FIXME the module should know the project template
  public ProjectConversionWizard( final String moduleID, final String projectTemplate )
  {
    super( new ProjectTemplatePage( StringUtils.EMPTY, StringUtils.EMPTY, projectTemplate ), false, moduleID );
    // REMARK: empty strings, as we know that now template chooser page is shown

    m_moduleID = moduleID;

    final IKalypsoModule module = ModuleExtensions.getKalypsoModule( moduleID );
    m_handler = module.getNewProjectHandler();

    setHelpAvailable( false );
    setNeedsProgressMonitor( true );
    setWindowTitle( "Projekt konvertieren" ); //$NON-NLS-1$

    final ProjectTemplatePage templatePage = getTemplatePage();

    /**
     * Automatically choose language by current settings. It is not so important any more.
     */
    final String language = Locale.getDefault().getLanguage();
    templatePage.selectTemplate( language );
  }

  @Override
  public void addPages( )
  {
    super.addPages();

    m_conversionPage = new ProjectConversionPage( "conversionPage", m_moduleID ); //$NON-NLS-1$

    addPage( m_conversionPage ); //$NON-NLS-1$

    // TODO: conversion pages:
    // - choose conversion parameters?
    // - choose old version (if not known)
  }

  @Override
  public void openProject( final IProject project ) throws CoreException
  {
    if( m_handler == null )
      super.openProject( project );
    else
      m_handler.openProject( project );
  }

  @Override
  public IStatus postCreateProject( final IProject project, final ProjectTemplate template, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "ProjectConversionWizard_1" ), 100 ); //$NON-NLS-1$

    if( m_handler != null )
      m_handler.postCreateProject( project, template, new SubProgressMonitor( monitor, 10 ) );

    // FIXME: We sometimes get a dead lock, if the pre-conversion operation returns too fast;
    // Eclipse still refreshes the workspace and we get a conflict when we start modifying resources.
    // How can we wait for the refresh to finish??

    final File inputDir = m_conversionPage.getProjectDir();
    return doConvertProject( inputDir, project, new SubProgressMonitor( monitor, 90, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK ) );
  }

  private IStatus doConvertProject( final File sourceDir, final IProject targetProject, final IProgressMonitor monitor )
  {
    try
    {
      final File targetDir = targetProject.getLocation().toFile();

      final IProjectConversionOperation operation = m_conversionPage.getConversionOperation( sourceDir, targetDir, targetProject );

      final IStatus preConversion = doPreConversion( operation );
      if( preConversion.matches( IStatus.CANCEL | IStatus.ERROR ) )
        return preConversion;

      return operation.execute( monitor );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
    catch( final InvocationTargetException e )
    {
      e.printStackTrace();
      final Throwable targetException = e.getTargetException();
      return new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, Messages.getString( "ProjectConversionWizard_2" ), targetException ); //$NON-NLS-1$
    }
    catch( final InterruptedException e )
    {
      return Status.CANCEL_STATUS;
    }
    finally
    {
      final ScenarioHandlingProjectNature nature = ScenarioHandlingProjectNature.toThisNatureQuiet( targetProject );
      final IScenarioManager caseManager = nature.getCaseManager();
      caseManager.resetCaseList();
    }
  }

  private IStatus doPreConversion( final IProjectConversionOperation operation )
  {
    final Shell shell = getShell();

    final IStatus[] status = new IStatus[1];
    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        status[0] = operation.preConversion( getShell() );
      }
    };

    // FIXED: not using UIJob with join here, that leads to a dead lock
    shell.getDisplay().syncExec( runnable );

    return status[0];
  }
}