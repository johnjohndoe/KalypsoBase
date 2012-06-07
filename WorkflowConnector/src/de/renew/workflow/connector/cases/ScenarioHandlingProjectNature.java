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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;

import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.connector.internal.cases.ScenarioManager;
import de.renew.workflow.connector.internal.i18n.Messages;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioHandlingProjectNature implements IProjectNature, ICaseManagerListener
{
  public final static String ID = "org.kalypso.afgui.ScenarioHandlingProjectNature"; //$NON-NLS-1$

  public static final String PREFERENCE_ID = "org.kalypso.afgui"; //$NON-NLS-1$

  private IScenarioManager m_caseManager;

  private IProject m_project;

  IDerivedScenarioCopyFilter m_filter = new IDerivedScenarioCopyFilter()
  {
    @Override
    public boolean copy( final IResource resource )
    {
      return true;
    }
  };

  @Override
  public void configure( )
  {
    // does nothing by default
  }

  @Override
  public void deconfigure( )
  {
    // does nothing by default
  }

  @Override
  public IProject getProject( )
  {
    return m_project;
  }

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

  public void setDerivedScenarioCopyFilter( final IDerivedScenarioCopyFilter filter )
  {
    m_filter = filter;
  }

  public IScenarioManager getCaseManager( )
  {
    return m_caseManager;
  }

  private IScenarioManager createCaseManager( final IProject project )
  {
    return new ScenarioManager( project );
  }

  /**
   * Constructs a path for the scenario relative to the project location.
   */
  private IPath getRelativeProjectPath( final IScenario scenario )
  {
    final String uri = scenario.getURI();
    if( uri != null )
    {
      if( StringUtils.startsWithIgnoreCase( uri, "scenario://" ) )
        return new Path( StringUtils.substringAfter( uri, "://" ) );

      return new Path( uri );
    }

    if( scenario.getParentScenario() != null )
      return getRelativeProjectPath( scenario.getParentScenario() ).append( scenario.getName() );

    return new Path( scenario.getName() );
  }

  @Override
  public void caseAdded( final IScenario scenario )
  {
    // FIXME: does not belong here -> move into scenario framework; scenario should not be created in event handling
    final IFolder newFolder = m_project.getFolder( getRelativeProjectPath( scenario ) );

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

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

    IStatus resultStatus = Status.OK_STATUS;
    final IScenario parentScenario = scenario.getParentScenario();
    if( parentScenario != null )
    {
      // this is a new derived scenario, so copy scenario contents of parent folder
      final IPath parentPath = getRelativeProjectPath( parentScenario );
      final List<IScenario> derivedScenarios = parentScenario.getDerivedScenarios().getScenarios();
      final List<IFolder> scenarioFolders = new ArrayList<IFolder>( derivedScenarios.size() );
      for( final IScenario derivedScenario : derivedScenarios )
        scenarioFolders.add( derivedScenario.getFolder() );

      if( !resultStatus.isOK() )
      {
        ErrorDialog.openError( window.getShell(), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.1" ), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.2" ), resultStatus ); //$NON-NLS-1$ //$NON-NLS-2$
        WorkflowConnectorPlugin.getDefault().getLog().log( resultStatus );
      }

      final IFolder parentFolder = getProject().getFolder( parentPath );

      // FIXME: does not belong here: the code that creates the new scenario is responsible for its contents
      final WorkspaceModifyOperation copyScenarioContentsOperation = new CopyScenarioContentsOperation( parentFolder, parentFolder, newFolder, scenarioFolders, m_filter );

      resultStatus = RunnableContextHelper.execute( window, true, true, copyScenarioContentsOperation );
      if( !resultStatus.isOK() )
      {
        ErrorDialog.openError( window.getShell(), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.1" ), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.2" ), resultStatus ); //$NON-NLS-1$ //$NON-NLS-2$
        WorkflowConnectorPlugin.getDefault().getLog().log( resultStatus );
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

  public static final ScenarioHandlingProjectNature toThisNature( final IProject project ) throws CoreException
  {
    return (ScenarioHandlingProjectNature) project.getNature( ID );
  }

  /**
   * Same as {@link #toThisNature(IProject)}, but only logs the thrown exception.
   */
  public static ScenarioHandlingProjectNature toThisNatureQuiet( final IProject project )
  {
    try
    {
      return toThisNature( project );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      WorkflowConnectorPlugin.getDefault().getLog().log( e.getStatus() );
      return null;
    }
  }

  public IDerivedScenarioCopyFilter getDerivedScenarioCopyFilter( )
  {
    return m_filter;
  }

  /**
   * Returns the (scenario specific) preferences for this project.
   */
  public IEclipsePreferences getProjectPreference( )
  {
    final ProjectScope projectScope = new ProjectScope( getProject() );
    return projectScope.getNode( ScenarioHandlingProjectNature.PREFERENCE_ID );
  }

}
