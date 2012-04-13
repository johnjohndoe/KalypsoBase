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

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.commons.i18n.ResourceBundleUtils;

import de.renew.workflow.base.EActivityType;
import de.renew.workflow.base.ITask;
import de.renew.workflow.base.ITaskHelp;
import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.base.Task;
import de.renew.workflow.base.Task.Help;
import de.renew.workflow.contexts.ContextType;

/**
 * @author Gernot Belger
 */
public class Task_Impl implements ITask
{
  private final Task m_task;

  private final ITaskHelp m_help;

  private final IWorkflow m_workflow;

  public Task_Impl( final Task task, final IWorkflow workflow )
  {
    m_task = task;
    m_workflow = workflow;

    final Help help = m_task.getHelp();
    m_help = help == null ? null : new TaskHelp_Impl( help, getResourceBundle() );
  }

  protected Task getTask( )
  {
    return m_task;
  }

  @Override
  public String getURI( )
  {
    return m_task.getURI();
  }

  @Override
  public ContextType getContext( )
  {
    return getTask().getContext();
  }

  @Override
  public EActivityType getType( )
  {
    return getTask().getType();
  }

  @Override
  public String getName( )
  {
    final String name = getTask().getName();
    return ResourceBundleUtils.getI18NString( name, getResourceBundle() );
  }

  @Override
  public String getTooltip( )
  {
    if( m_help == null )
      return null;

    final String value = m_help.getValue();
    if( StringUtils.isBlank( value ) )
      return null;

    return ResourceBundleUtils.getI18NString( value, getResourceBundle() );
  }

  protected ResourceBundle getResourceBundle( )
  {
    return getWorkflow().getResourceBundle();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof Task_Impl )
    {
      final Task_Impl other = (Task_Impl) obj;

      return getURI().equals( other.getURI() );
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    return getURI().hashCode();
  }

  @Override
  public String toString( )
  {
    return getURI().toString();
  }

  @Override
  public IWorkflow getWorkflow( )
  {
    return m_workflow;
  }
}