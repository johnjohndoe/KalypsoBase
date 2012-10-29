/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.model.wspm.core.profil.ProfileFactory;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This helper creates a new profile.<br/>
 * <br/>
 * The following steps are performed:
 * <ol>
 * <li>Computes the width and height for each point.</li>
 * <li>Create a profile with each point and its width and height (only points with elevation are being considered!)</li>
 * <li>The new profile is simplified by Douglas Peucker.</li>
 * </ol>
 *
 * @param profileType
 *          The type of the profile.
 * @author Gernot Belger
 */
public class CreateProfileFromCrdsTransaction implements IProfileTransaction
{
  private final Coordinate[] m_pointsZ;

  private final double m_simplifyDistance;

  public static IProfile createProfileFromCoordinates( final String profileType, final Coordinate[] pointsZ, final String pointsZsrs, final double simplifyDistance )
  {
    /* Create the new profile. */
    final IProfile profile = ProfileFactory.createProfil( profileType, null );
    profile.setSrsName( pointsZsrs );

    /* add points in transaction */
    final CreateProfileFromCrdsTransaction transaction = new CreateProfileFromCrdsTransaction( pointsZ, simplifyDistance );
    profile.doTransaction( transaction );

    return profile;
  }

  private CreateProfileFromCrdsTransaction( final Coordinate[] pointsZ, final double simplifyDistance )
  {
    m_pointsZ = pointsZ;
    m_simplifyDistance = simplifyDistance;
  }

  @Override
  public IStatus execute( final IProfile profile )
  {
    /* STEP 1: Compute the width and height for each point of the new line. */
    /* STEP 2: Create the new profile. */
    calculatePoints( profile, m_pointsZ );
    final int length = profile.getPoints().length;
    if( length == 0 )
      return Status.OK_STATUS;

    /* STEP 3: Simplify the profile. */
    ProfileUtil.simplifyProfile( profile, m_simplifyDistance );

    return Status.OK_STATUS;
  }

  /**
   * This function calculates the points for the profile and creates the new profile.
   *
   * @param profileType
   *          The type of the profile.
   * @param points
   *          All points of the new geo line.
   * @param csOfPoints
   *          The coordinate system of the points.
   * @return The new profile.
   */
  private IProfile calculatePoints( final IProfile profile, final Coordinate[] points )
  {
    // TODO: check: we calculate the 'breite' by just adding up the distances between the points, is this always OK?
    // FIXME: no! this at least depends on the coordinate system....!
    double breite = 0.0;
    Coordinate lastCrd = null;
    for( final Coordinate coordinate : points )
    {
      /* Create a new point. */
      final IProfileRecord profilePoint = WspmProfileHelper.createRecord( profile, coordinate, breite );

      /* Add the new point to the profile. */
      profile.addPoint( profilePoint );

      // FIXME: this at least depends on the coordinate system. In WGS84 we get deegrees, but we always need [m]!
      if( lastCrd != null )
        breite += coordinate.distance( lastCrd );

      /* Store the current coordinate. */
      lastCrd = coordinate;
    }

    return profile;
  }
}