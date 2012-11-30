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
package org.kalypso.model.wspm.core.profil.base;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.jts.JTSConverter;
import org.kalypso.jts.JtsVectorUtilities;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.observation.result.IRecord;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public class MoveProfileRunnable implements ICoreRunnableWithProgress
{

  private final IProfile m_profile;

  private final Coordinate m_vector;

  private final Double m_distance;

  private final int m_direction;

  /**
   * move profile point orthogonal to given vector by distance x
   *
   * @param vector
   *          base vector
   * @param distance
   *          move points x meters
   */
  public MoveProfileRunnable( final IProfile profile, final Coordinate vector, final Double distance, final int direction )
  {
    m_profile = profile;
    m_vector = vector;
    m_distance = distance;
    m_direction = direction;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final int xComponent = m_profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_RECHTSWERT );
    final int yComponent = m_profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOCHWERT );

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final Number x = (Number) point.getValue( xComponent );
      final Number y = (Number) point.getValue( yComponent );
      if( Objects.isNull( x, y ) )
        continue;

      final Coordinate moved = move( new Coordinate( x.doubleValue(), y.doubleValue() ) );

      point.setValue( xComponent, moved.x );
      point.setValue( yComponent, moved.y );

    }

    return new Status( IStatus.OK, KalypsoModelWspmCorePlugin.getID(), Messages.getString("MoveProfileRunnable_0") ); //$NON-NLS-1$
  }

  private Coordinate move( final Coordinate coordinate )
  {
    final Point moved = JtsVectorUtilities.movePoint( JTSConverter.toPoint( coordinate ), m_vector, m_distance, m_direction );

    return moved.getCoordinate();
  }
}
