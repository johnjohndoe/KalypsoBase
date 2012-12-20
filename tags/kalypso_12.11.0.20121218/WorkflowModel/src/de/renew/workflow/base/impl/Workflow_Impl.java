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
package de.renew.workflow.base.impl;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import de.renew.workflow.base.ITask;
import de.renew.workflow.base.ITaskGroup;
import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.base.Task;
import de.renew.workflow.base.Workflow;
import de.renew.workflow.utils.ScenarioConfiguration;

/**
 * @author Gernot Belger
 */
public class Workflow_Impl extends TaskGroup_Impl implements IWorkflow
{
  private final URL m_context;

  private final ResourceBundle m_resourceBundle;

  /**
   * The scenario configuration contains options for handling a scenario. May be null.
   */
  private final ScenarioConfiguration m_scenarioConfiguration;

  private ITask m_defaultTask;

  /**
   * @param context
   *          The context against which relative references within the workflow.xml are resolved. Tyically the location
   *          where the workflow.xml is stored.
   * @param scenarioConfiguration
   *          The scenario configuration contains options for handling a scenario. May be null.
   */
  public Workflow_Impl( final Workflow workflow, final ResourceBundle resourceBundle, final URL context, final ScenarioConfiguration scenarioConfiguration )
  {
    super( workflow, null );

    m_resourceBundle = resourceBundle;
    m_context = context;
    m_scenarioConfiguration = scenarioConfiguration;

    final Task defaultTask = workflow.getDefaultTask();
    if( defaultTask != null )
      changeDefaultTask( defaultTask.getURI() );
  }

  @Override
  public URL getResourceContext( )
  {
    return m_context;
  }

  @Override
  public ResourceBundle getResourceBundle( )
  {
    return m_resourceBundle;
  }

  @Override
  public ScenarioConfiguration getScenarioConfiguration( )
  {
    return m_scenarioConfiguration;
  }

  @Override
  public ITask getDefaultTask( )
  {
    return m_defaultTask;
  }

  @Override
  public synchronized void changeDefaultTask( final String uri )
  {
    final ITask defaultTask = findDefaultTask( this, uri );
    m_defaultTask = defaultTask;
  }

  private ITask findDefaultTask( final ITask task, final String uri )
  {
    if( task.getURI().equals( uri ) )
      return task;

    if( task instanceof ITaskGroup )
    {
      final List<ITask> children = ((ITaskGroup) task).getTasks();
      for( final ITask child : children )
      {
        final ITask childDefaultTask = findDefaultTask( child, uri );
        if( childDefaultTask != null )
          return childDefaultTask;
      }
    }

    return null;
  }

  @Override
  public IWorkflow getWorkflow( )
  {
    return this;
  }
}