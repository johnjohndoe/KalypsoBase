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
package org.kalypso.model.wspm.ui.profil.dialogs.reducepoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.changes.PointRemove;
import org.kalypso.model.wspm.core.profil.operation.ProfileOperation;
import org.kalypso.model.wspm.core.profil.operation.ProfileOperationRunnable;
import org.kalypso.model.wspm.core.profil.util.DouglasPeuckerHelper;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class SimplifyProfileOperation
{
  private final IProfile m_profile;

  private final IPointsProvider m_provider;

  private final double m_distance;

  private final String[] m_buildingComponents;

  private ProfileOperation m_operation;

  public SimplifyProfileOperation( final IProfile profile, final IPointsProvider provider, final double distance, final String[] buildingComponents )
  {
    m_profile = profile;
    m_provider = provider;
    m_distance = distance;
    m_buildingComponents = buildingComponents;
  }

  public Pair<IProfileRecord[], IStatus> findPointsToRemove( )
  {
    if( m_provider == null || Double.isNaN( m_distance ) )
    {
      final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmUIPlugin.ID, Messages.getString( "org.kalypso.model.wspm.ui.profil.dialogs.reducepoints.DouglasPeuckerDialog.11" ) ); //$NON-NLS-1$
      return Pair.of( null, status );
    }

    /* Get important values. */
    final double allowedDistance = m_distance;
    final IProfileRecord[] points = m_provider.getPoints();

    /* Find out which points to keep */
    final IProfileRecord[] pointsToKeep = getPointsToKeep();

    /* Get the profile changes. */

    final IProfileRecord[] pointsToRemove = DouglasPeuckerHelper.reducePoints( points, pointsToKeep, allowedDistance );
    if( pointsToRemove.length == 0 )
    {
      final IStatus status = new Status( IStatus.INFO, KalypsoModelWspmUIPlugin.ID, Messages.getString( "org.kalypso.model.wspm.ui.profil.dialogs.reducepoints.DouglasPeuckerDialog.12" ) ); //$NON-NLS-1$
      return Pair.of( null, status );
    }

    /* Message for the user. */
    final String message = Messages.getString( "org.kalypso.model.wspm.ui.profil.dialogs.reducepoints.DouglasPeuckerDialog.14" , pointsToRemove.length, points.length ); //$NON-NLS-1$

    final IStatus status = new Status( IStatus.INFO, KalypsoModelWspmUIPlugin.ID, message );
    return Pair.of( pointsToRemove, status );
  }

  public IStatus doRemovePoints( )
  {
    final Pair<IProfileRecord[], IStatus> result = findPointsToRemove();

    final IProfileRecord[] pointsToRemove = result.getKey();
    final IStatus reduceStatus = result.getValue();

    if( pointsToRemove == null )
      return reduceStatus;

    /* Create the profile operation. */
    final PointRemove pointRemove = new PointRemove( m_profile, pointsToRemove );
    m_operation = new ProfileOperation( Messages.getString( "org.kalypso.model.wspm.ui.profil.dialogs.reducepoints.DouglasPeuckerDialog.13" ), m_profile, pointRemove, false ); //$NON-NLS-1$

    /* Create the runnable. */
    final ProfileOperationRunnable operationRunnable = new ProfileOperationRunnable( m_operation );

    /* Execute the value. */
    final IStatus operationStatus = operationRunnable.execute( new NullProgressMonitor() );
    if( !operationStatus.isOK() )
      return operationStatus;

    return reduceStatus;
  }

  private IProfileRecord[] getPointsToKeep( )
  {
    final Collection<IProfileRecord> pointsToKeep = new ArrayList<>();

    /* Marked points (TF, DB, BV) should never get simplified */
    final IProfileRecord[] markedPoints = m_profile.getMarkedPoints();
    pointsToKeep.addAll( Arrays.asList( markedPoints ) );

    /*
     * TODO: in order to do a correct simplifikatino with bridges/weirs, we need to split up the simplifikation in
     * several chunks per bridge-part.
     */

    /* for now, we just keep all bridge points, which does not work if all points are bridge points */
    final IProfileRecord[] buildingPoints = getBuildingPoints( m_profile );
    pointsToKeep.addAll( Arrays.asList( buildingPoints ) );

    return pointsToKeep.toArray( new IProfileRecord[pointsToKeep.size()] );
  }

  private IProfileRecord[] getBuildingPoints( final IProfile profile )
  {
    final Collection<IProfileRecord> buildingPoints = new ArrayList<>();

    for( final String buildingComponent : m_buildingComponents )
    {
      final IProfileRecord[] componentPoints = getBuildingPoints( profile, buildingComponent );
      buildingPoints.addAll( Arrays.asList( componentPoints ) );
    }

    return buildingPoints.toArray( new IProfileRecord[buildingPoints.size()] );
  }

  private IProfileRecord[] getBuildingPoints( final IProfile profile, final String buildingComponent )
  {
    if( !ArrayUtils.isEmpty( m_buildingComponents ) )
      return getAllValidPoints( profile, buildingComponent );

    return getStartingEndBuildingPoints( profile, buildingComponent );
  }

  /**
   * Get all profile points of a component, which values are of type number (i.e non-<code>null</code>).
   */
  private IProfileRecord[] getAllValidPoints( final IProfile profile, final String buildingComponent )
  {
    final Collection<IProfileRecord> allPoints = new ArrayList<>();

    final int componentIndex = profile.indexOfProperty( buildingComponent );
    if( componentIndex == -1 )
      return new IProfileRecord[0];

    final IProfileRecord[] points = profile.getPoints();
    for( final IProfileRecord point : points )
    {
      final Object value = point.getValue( componentIndex );
      if( value instanceof Number )
      {
        allPoints.add( point );
      }
    }

    return allPoints.toArray( new IProfileRecord[allPoints.size()] );
  }

  // FIXME: does not work properly we need to consider if the point lies on the soil or not, see BridgeRule
  @SuppressWarnings("unused")
  private IProfileRecord[] getStartingEndBuildingPoints( final IProfile profile, final String buildingComponent )
  {
    final Collection<IProfileRecord> startOrEndPoints = new HashSet<IProfileRecord>();

// final int componentIndex = profile.indexOfProperty( buildingComponent );
// if( componentIndex == -1 )
// return new IRecord[0];
//
// final IRecord[] points = profile.getPoints();
//
// boolean lastValid = false;
// IRecord lastPoint = null;
// for( final IRecord point : points )
// {
// final Object value = point.getValue( componentIndex );
// final boolean isValid = value instanceof Number;
//
// /* Start point */
// if( isValid && !lastValid )
// startOrEndPoints.add( point );
//
// if( !isValid && lastValid && lastPoint != null )
// startOrEndPoints.add( lastPoint );
//
// lastValid = isValid;
// lastPoint = point;
// }

    return startOrEndPoints.toArray( new IProfileRecord[startOrEndPoints.size()] );
  }

  public IStatus resetLastOperation( ) throws ExecutionException
  {
    if( m_operation == null )
      return Status.OK_STATUS;

    final IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
    final IStatus status = operationHistory.undoOperation( m_operation, new NullProgressMonitor(), null );
    m_operation = null;
    return status;
  }
}