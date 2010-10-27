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

package org.kalypso.afgui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.wizard.ProjectTemplatePage;
import org.kalypso.module.conversion.ProjectConversionPage;

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

  // FIXME the module should know the project template
  public ProjectConversionWizard( final String moduleID, final String projectTemplate )
  {
    super( new ProjectTemplatePage( "Projektvorlage", "Bitte wählen Sie, welche Projektvorlage verwendet werden soll", projectTemplate ), true, moduleID );

    m_moduleID = moduleID;

    setHelpAvailable( false );
    setNeedsProgressMonitor( true );
    setWindowTitle( "Projekt konvertieren" ); //$NON-NLS-1$
  }

  @Override
  public void addPages( )
  {
    super.addPages();

    m_conversionPage = new ProjectConversionPage( "conversionPage", m_moduleID );

    addPage( m_conversionPage ); //$NON-NLS-1$

    // TODO: conversion pages:
    // - choose conversion parameters?
    // - choose old version (if not known)
  }

  /**
   * @see org.kalypso.afgui.wizards.NewProjectWizard#postCreateProject(org.eclipse.core.resources.IProject,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus postCreateProject( final IProject project, final IProgressMonitor monitor )
  {
    final File inputDir = m_conversionPage.getProjectDir();
    return doConvertProject( inputDir, project, monitor );
  }

  private IStatus doConvertProject( final File sourceDir, final IProject targetProject, final IProgressMonitor monitor )
  {
    try
    {
      final File targetDir = targetProject.getLocation().toFile();

      final ICoreRunnableWithProgress operation = m_conversionPage.getConversionOperation( sourceDir, targetDir, targetProject );
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
      return new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, "Unexpected error during project conversion", e );
    }
    catch( final InterruptedException e )
    {
      return Status.CANCEL_STATUS;
    }
  }
}
