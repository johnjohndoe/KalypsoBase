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
package org.kalypso.afgui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.afgui.i18n.Messages;
import org.kalypso.afgui.scenarios.IDerivedScenarioCopyFilter;
import org.kalypso.afgui.scenarios.IScenarioManager;
import org.kalypso.afgui.scenarios.ScenarioManager;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;

import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioHandlingProjectNature extends CaseHandlingProjectNature<IScenario>
{
  public final static String ID = "org.kalypso.afgui.ScenarioHandlingProjectNature"; //$NON-NLS-1$

  public static final String PREFERENCE_ID = "org.kalypso.afgui"; //$NON-NLS-1$

  IDerivedScenarioCopyFilter m_filter = new IDerivedScenarioCopyFilter()
  {
    @Override
    public boolean copy( final IResource resource )
    {
      return true;
    }
  };

  public void setDerivedScenarioCopyFilter( final IDerivedScenarioCopyFilter filter )
  {
    m_filter = filter;
  }

  /**
   * @see de.renew.workflow.connector.context.CaseHandlingProjectNature#createCaseManager(org.eclipse.core.resources.IProject)
   */
  @Override
  public IScenarioManager createCaseManager( final IProject project )
  {
    return new ScenarioManager( project );
  }

  /**
   * @see de.renew.workflow.connector.context.CaseHandlingProjectNature#getCaseManager()
   */
  @Override
  public IScenarioManager getCaseManager( )
  {
    return (IScenarioManager) super.getCaseManager();
  }

  /**
   * Constructs a path for the scenario relative to the project location.
   */
  @Override
  public IPath getRelativeProjectPath( final IScenario caze )
  {
    return getProjectRelativePath( caze );
  }

  /**
   * Static version of {@link #getRelativeProjectPath(Case)}.
   */
  public static IPath getProjectRelativePath( final IScenario caze )
  {
    final IScenario scenario = caze;
    if( scenario.getParentScenario() != null )
    {
      return getProjectRelativePath( scenario.getParentScenario() ).append( scenario.getName() );
    }
    else
    {
      return new Path( scenario.getName() );
    }
  }

  /**
   * @see org.kalypso.kalypso1d2d.pjt.CaseHandlingProjectNature#scenarioAdded(de.renew.workflow.cases.Case)
   */
  @Override
  public void caseAdded( final IScenario scenario )
  {
    super.caseAdded( scenario );
    final IPath projectPath = getRelativeProjectPath( scenario );
    final IFolder newFolder = getProject().getFolder( projectPath );

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
      try
      {
        for( final IScenario derivedScenario : derivedScenarios )
        {
          scenarioFolders.add( derivedScenario.getFolder() );
        }
      }
      catch( final CoreException e )
      {
        resultStatus = e.getStatus();
      }

      if( !resultStatus.isOK() )
      {
        ErrorDialog.openError( window.getShell(), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.1" ), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.2" ), resultStatus ); //$NON-NLS-1$ //$NON-NLS-2$
        KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( resultStatus );
      }

      final IFolder parentFolder = getProject().getFolder( parentPath );
      final WorkspaceModifyOperation copyScenarioContentsOperation = new WorkspaceModifyOperation( parentFolder )
      {

        @Override
        protected void execute( final IProgressMonitor monitor ) throws CoreException
        {
          final SubMonitor submonitor = SubMonitor.convert( monitor, getTotalChildCount( parentFolder ) );
          parentFolder.accept( new IResourceVisitor()
          {
            @Override
            public boolean visit( final IResource resource ) throws CoreException
            {
              if( parentFolder.equals( resource ) )
              {
                return true;
              }
              else if( scenarioFolders.contains( resource ) )
              {
                // ignore scenario folder
                return false;
              }

              if( m_filter.copy( resource ) )
              {
                final IPath parentFolderPath = parentFolder.getFullPath();
                final IPath resourcePath = resource.getFullPath();

                final IPath relativePath = resourcePath.removeFirstSegments( parentFolderPath.segments().length );

                if( resource instanceof IFolder )
                {
                  newFolder.getFolder( relativePath ).create( true, true, submonitor.newChild( 1 ) );
                }
                else if( resource instanceof IFile )
                {
                  resource.copy( newFolder.getFullPath().append( relativePath ), true, submonitor.newChild( 1 ) );
                }

                return true;
              }

              if( !m_filter.copy( resource ) && resource instanceof IFolder )
                return false;

              return true;
            }
          } );
        }

        private int getTotalChildCount( final IContainer container )
        {
          IResource[] members;
          try
          {
            members = container.members();
          }
          catch( final CoreException ex )
          {
            return 0;
          }
          int count = 0;
          for( int i = 0; i < members.length; i++ )
          {
            if( !m_filter.copy( members[i] ) )
              continue;
            if( members[i].getType() == IResource.FILE )
              count++;
            else
              count += getTotalChildCount( (IContainer) members[i] );
          }
          return count;
        }
      };

      resultStatus = RunnableContextHelper.execute( window, true, true, copyScenarioContentsOperation );
      if( !resultStatus.isOK() )
      {
        ErrorDialog.openError( window.getShell(), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.1" ), Messages.getString( "org.kalypso.afgui.ScenarioHandlingProjectNature.2" ), resultStatus ); //$NON-NLS-1$ //$NON-NLS-2$
        KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( resultStatus );
      }
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
      KalypsoAFGUIFrameworkPlugin.getDefault().getLog().log( e.getStatus() );
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
