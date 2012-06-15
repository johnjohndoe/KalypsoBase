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
 *  Lesser General License for more details.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * TODO: this should probably not be used by normal client code but only internally in the scenario handling.
 * 
 * @author Stefan Kurzbach
 */
public interface IScenarioManager
{
  /** The status of this case manager, will be set to non-ok, if the cases could not be loaded. */
  IStatus getStatus( );

  /**
   * Returns the current case
   */
  IScenario getCurrentCase( );

  /**
   * Sets the current case
   */
  void setCurrentCase( final IScenario caze );

  /**
   * Returns all the cases
   */
  List<IScenario> getCases( );

  /**
   * Returns the case with the given uri. If no scenario with the given uri exists, <code>null</code> will be returned.
   */
  IScenario getCase( final String uri );

  /**
   * Removes the case.
   */
  void removeCase( final IScenario caze, final IProgressMonitor monitor ) throws CoreException;

  /**
   * Registers <code>listener</code> with this case manager. If an identical listener is already registered, this method
   * has no effect.
   * 
   * @param listener
   *          the listener to be removed, must not be <code>null</code>
   */
  void addCaseManagerListener( final ICaseManagerListener listener );

  /**
   * Removes <code>listener</code> from this case manager. If no identical listener was registered, this method has no
   * effect.
   * 
   * @param listener
   *          the listener to be removed
   */
  void removeCaseManagerListener( final ICaseManagerListener listener );

  /**
   * Deregisters all listeners
   */
  void dispose( );

  /**
   * Creates a new scenario with the given name. It is derived from <code>parentScenario</code>. The scenario metadata
   * file and the database will be updated to reflect the change. The name must be unique within the context of
   * parentScenario. A notification will be sent to registered listeners that the scenarios have changed.
   * 
   * @exception CoreException
   *              if this method fails. Reasons include:
   *              <ul>
   *              <li>A scenario with the given name already exists.</li>
   *              <li>The name of this scenario is not valid (according to <code>IWorkspace.validateName</code>).</li>
   *              <li>There is a problem persisting the database. See {@link #persist()} for details.</li>
   *              <li>There is a problem copying the scenario data from the template.</li>
   * 
   */
  IScenario createScenario( final IScenario parentScenario, final String scenarioName, final String scenarioDescription, final IScenario templateScenario, final boolean copySubScenarios, final IProgressMonitor monitor ) throws CoreException;

  void renameScenario( IScenario scenario, String newName, String newComment, IProgressMonitor monitor ) throws CoreException;

  void resetCaseList( );
}