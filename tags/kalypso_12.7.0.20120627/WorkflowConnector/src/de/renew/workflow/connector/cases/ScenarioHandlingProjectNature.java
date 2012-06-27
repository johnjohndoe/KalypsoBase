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
package de.renew.workflow.connector.cases;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.connector.internal.cases.ScenarioManager;

/**
 * @author Stefan Kurzbach
 */
public class ScenarioHandlingProjectNature implements IProjectNature
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
      m_caseManager = new ScenarioManager( m_project );
  }

  public void setDerivedScenarioCopyFilter( final IDerivedScenarioCopyFilter filter )
  {
    m_filter = filter;
  }

  public IScenarioManager getCaseManager( )
  {
    return m_caseManager;
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
