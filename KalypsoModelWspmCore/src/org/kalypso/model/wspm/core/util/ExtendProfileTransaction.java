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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.transformation.transformer.JTSTransformer;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Gernot Belger
 */
public class ExtendProfileTransaction implements IProfileTransaction
{
  private final Coordinate[] m_newPoints;

  private final int m_insertSign;

  private final double m_simplifyDistance;

  private final String m_newPointsSRS;

  public ExtendProfileTransaction( final Coordinate[] newPoints, final String newPointsSRS, final int insertSign, final double simplifyDistance )
  {
    m_newPoints = newPoints;
    m_newPointsSRS = newPointsSRS;
    m_insertSign = insertSign;
    m_simplifyDistance = simplifyDistance;
  }

  @Override
  public IStatus execute( final IProfile profile )
  {
    if( ArrayUtils.isEmpty( m_newPoints ) )
      return Status.OK_STATUS;

    try
    {
      /* make sure, new points are in same srs as profile */
      final String profileSRS = profile.getSrsName();
      final JTSTransformer jtsTransformer = new JTSTransformer( JTSAdapter.toSrid( m_newPointsSRS ), JTSAdapter.toSrid( profileSRS ) );
      final Coordinate[] transformedNewPoints = jtsTransformer.transform( m_newPoints );

      /* insert locations as new record into profile */
      WspmProfileHelper.insertPoints( profile, m_insertSign, transformedNewPoints );

      /* Douglas peucker the resulting profile */
      // FIXME: it would be more performant, to douglas peucker the to be inserted points instead
      final int length = profile.getPoints().length;
      final int start = m_insertSign == -1 ? 0 : length - m_newPoints.length;
      final int end = m_insertSign == -1 ? m_newPoints.length : length;

      ProfileUtil.simplifyProfile( profile, m_simplifyDistance, start, end - 1 );

      return Status.OK_STATUS;
    }
    catch( FactoryException | TransformException e )
    {
      e.printStackTrace();
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "Failed to insert new points into profile", e ); //$NON-NLS-1$
    }
  }
}