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
package de.renew.workflow.connector.internal.cases;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.afgui.scenarios.Scenario;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import com.google.common.base.Charsets;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.cases.Case;
import de.renew.workflow.connector.WorkflowProjectNature;
import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioList;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;
import de.renew.workflow.utils.IgnoreFolder;
import de.renew.workflow.utils.ScenarioConfiguration;

/**
 * Wrapper interface for handling {@link Case} Objects
 *
 * @author Dirk Kuch
 */
public class ScenarioHandler implements IScenario
{
  private final Scenario m_scenario;

  private final IProject m_project;

  public ScenarioHandler( final Scenario scenario, final IProject project )
  {
    m_scenario = scenario;
    m_project = project;

    if( m_scenario.getURI() == null )
    {
      setURI( String.format( "%s%s", NEW_CASE_BASE_URI, getName() ) ); //$NON-NLS-1$
    }
    else if( !getURI().startsWith( NEW_CASE_BASE_URI ) && !getURI().startsWith( OLD_CASE_BASE_URI ) )
    {
      setURI( String.format( "%s%s", NEW_CASE_BASE_URI, getURI() ) ); //$NON-NLS-1$
    }
  }

  @Override
  public String getName( )
  {
    return m_scenario.getName();
  }

  @Override
  public String getDescription( )
  {
    return m_scenario.getDescription();
  }

  @Override
  public String getURI( )
  {
    try
    {
      return URLDecoder.decode( m_scenario.getURI(), Charsets.UTF_8.name() );
    }
    catch( final UnsupportedEncodingException e )
    {
      WorkflowConnectorPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return m_scenario.getURI();
  }

  private void setURI( final String uri )
  {
    m_scenario.setURI( uri );
  }

  @Override
  public IProject getProject( )
  {
    return m_project;
  }

  @Override
  public Case getCase( )
  {
    return m_scenario;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IScenario )
    {
      final IScenario other = (IScenario) obj;

      final EqualsBuilder builder = new EqualsBuilder();

      final Case caze = getCase();
      final Case otherCase = other.getCase();

      if( caze != null && otherCase != null )
      {
        builder.append( caze.getName(), otherCase.getName() );
        builder.append( caze.getURI(), otherCase.getURI() );
      }
      else
        builder.append( caze, otherCase );

      builder.append( getProject(), other.getProject() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();

    final Case caze = getCase();
    if( caze == null )
      builder.append( caze );
    else
    {
      builder.append( caze.getName() );
      builder.append( caze.getURI() );
    }

    builder.append( getProject() );

    return builder.toHashCode();
  }

  @Override
  public IFolder getFolder( )
  {
    final String uri = getURI();
    if( uri.startsWith( OLD_CASE_BASE_URI ) )
    {
      final String base = uri.substring( OLD_CASE_BASE_URI.length() );

      final int cleaned = base.indexOf( "/" ); //$NON-NLS-1$

      return getProject().getFolder( base.substring( cleaned + 1 ) );
    }

    if( uri.startsWith( NEW_CASE_BASE_URI ) )
    {
      return getProject().getFolder( uri.substring( NEW_CASE_BASE_URI.length() ) );
    }

    throw new IllegalStateException();
  }

  @Override
  public IScenarioList getDerivedScenarios( )
  {
    return new ScenarioListHandler( this );
  }

  @Override
  public IScenario getParentScenario( )
  {
    final Scenario parentScenario = m_scenario.getParentScenario();
    if( parentScenario == null )
      return null;

    return new ScenarioHandler( parentScenario, getProject() );
  }

  @Override
  public Scenario getScenario( )
  {
    return m_scenario;
  }

  @Override
  public int getHierarchicalLevel( )
  {
    final IScenario parent = getParentScenario();
    if( parent == null )
      return 0;

    final int level = parent.getHierarchicalLevel();

    return level + 1;
  }

  @Override
  public String toString( )
  {
    return getName();
  }

  @Override
  public IFolder getDerivedFolder( )
  {
    final IFolder myFolder = getFolder();

    try
    {
      final WorkflowProjectNature workflowProjectNature = WorkflowProjectNature.toThisNature( getProject() );
      final IWorkflow workflow = workflowProjectNature.getCurrentWorklist();
      final ScenarioConfiguration scenarioConfiguration = workflow.getScenarioConfiguration();
      if( scenarioConfiguration == null )
        return myFolder;

      final String derivedFolder = scenarioConfiguration.getDerivedFolder();
      if( derivedFolder == null || derivedFolder.length() == 0 )
        return myFolder;

      return myFolder.getFolder( derivedFolder );
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
      return myFolder;
    }
  }

  @Override
  public IFolder[] getSetAsBaseScenarioBlackList( )
  {
    try
    {
      final WorkflowProjectNature workflowProjectNature = WorkflowProjectNature.toThisNature( getProject() );
      final IWorkflow workflow = workflowProjectNature.getCurrentWorklist();
      final ScenarioConfiguration scenarioConfiguration = workflow.getScenarioConfiguration();
      if( scenarioConfiguration == null )
        return new IFolder[] {};

      final IgnoreFolder[] ignoreFolders = scenarioConfiguration.getIgnoreFolders();
      if( ignoreFolders.length == 0 )
        return new IFolder[] {};

      final List<IFolder> results = new ArrayList<>();

      for( final IgnoreFolder ignoreFolder : ignoreFolders )
      {
        if( "SetAsBaseScenario".equals( ignoreFolder.getRoleName() ) ) //$NON-NLS-1$
          results.add( getFolder().getFolder( ignoreFolder.getFolderName() ) );
      }

      return results.toArray( new IFolder[] {} );
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
      return new IFolder[] {};
    }
  }
}