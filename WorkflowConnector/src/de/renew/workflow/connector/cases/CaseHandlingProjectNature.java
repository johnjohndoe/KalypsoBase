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
package de.renew.workflow.connector.cases;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * This project nature add the possibility to handle cases inside the project and keep information about the current
 * workflow state of cases
 * 
 * @author Stefan Kurzbach
 */
public abstract class CaseHandlingProjectNature implements IProjectNature, ICaseManagerListener
{
  private IScenarioManager m_caseManager;

  private IProject m_project;

  /**
   * Creates a specific case manager for this project
   */
  protected abstract IScenarioManager createCaseManager( final IProject project );

  public IScenarioManager getCaseManager( )
  {
    return m_caseManager;
  }

  /**
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  @Override
  @SuppressWarnings("unused")
  public void configure( ) throws CoreException
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
    if( m_caseManager != null )
    {
      m_caseManager.dispose();
      m_caseManager = null;
    }

    m_project = project;

    if( m_project != null )
    {
      m_caseManager = createCaseManager( m_project );
      m_caseManager.addCaseManagerListener( this );
    }
  }

  /**
   * Constructs a path for the case relative to the project location.
   */
  public IPath getRelativeProjectPath( @SuppressWarnings("unused") final IScenario caze )
  {
    return Path.EMPTY; // caze.getName() );
  }

  @Override
  public void caseAdded( final IScenario caze )
  {
    // FIXME: does not belong here -> move into scenario framework; scenario should not be created in event handling
    final IFolder newFolder = m_project.getFolder( getRelativeProjectPath( caze ) );

    if( !newFolder.exists() )
    {
      try
      {
        newFolder.create( false, true, null );
      }
      catch( final CoreException e )
      {
        final Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        final Status status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, 0, "", e );
        ErrorDialog.openError( activeShell, "Problem", "Konnte neue Falldaten nicht erzeugen.", status );
        WorkflowConnectorPlugin.getDefault().getLog().log( status );
      }
    }
  }

  @Override
  public void caseRemoved( final IScenario caze )
  {
    final IFolder folder = m_project.getFolder( getRelativeProjectPath( caze ) );
    try
    {
      folder.delete( true, null );
    }
    catch( final CoreException e )
    {
      final Shell activeShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
      final Status status = new Status( IStatus.ERROR, WorkflowConnectorPlugin.PLUGIN_ID, 0, "", e );
      ErrorDialog.openError( activeShell, "Problem", "Konnte Falldaten nicht löschen.", status );
      WorkflowConnectorPlugin.getDefault().getLog().log( status );
    }
  }
}