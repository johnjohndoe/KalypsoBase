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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.command.ICommand;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * The case data provider functions as a bridge between the abstract case data model and actual data objects. Generics
 * provide a way to create data providers that return data objects of a type more specific than {@link Object}.
 *
 * @author Gernot Belger
 * @author Stefan Kurzbach
 */
public interface IScenarioDataProvider
{
  /**
   * Returns the data object corresponding to the given case data key.<br/>
   * The model must use a binding interface D, else we get a class cast exception (adaptgion does not work with this
   * method).
   *
   * @param id
   *          Id of the queried data (probably the extension-id with which this data was registered)
   */
  <D extends IModel> D getModel( final String id ) throws CoreException;

  /**
   * Saves all model data
   */
  void saveModel( IProgressMonitor monitor ) throws CoreException;

  /**
   * Saves the data object corresponding to the given case data key
   *
   * @param id
   *          Id of the queried data (probably the extension-id with which this data was registered)
   */
  void saveModel( String id, IProgressMonitor monitor ) throws CoreException;

  /**
   * Returns <code>true</code> if the data object corresponding to the given case data key has been modified since it
   * was retrieved.
   *
   * @param id
   *          Id of the queried data (probably the extension-id with which this data was registered)
   */
  boolean isDirty( String id );

  boolean isDirty( );

  void reloadModel( );

  void resetDirty( );

  void setCurrent( IScenario scenario, IProgressMonitor monitor );

  CommandableWorkspace getCommandableWorkSpace( String name ) throws IllegalArgumentException, CoreException;

  void addScenarioDataListener( IScenarioDataListener listener );

  void removeScenarioDataListener( IScenarioDataListener listener );

  void postCommand( String name, ICommand command ) throws InvocationTargetException;

  // FIXME: remove
  IScenario getScenario( );

  // FIXME: remove
  IContainer getScenarioFolder( );

  boolean waitForModelToLoad( final String id, final int maxWaitTimeInMillis ) throws InterruptedException;
}