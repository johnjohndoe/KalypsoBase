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
package org.kalypso.model.wspm.core.util.vegetation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.jts.JTSUtilities;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyEdit;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperationJob;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Guess vegatation classes from existing ax,ay,dp values
 * 
 * @author Dirk Kuch
 */
public class GuessVegetationClassesRunnable implements ICoreRunnableWithProgress
{
  private final IProfil m_profile;

  private final boolean m_overwriteValues;

  private final Double m_maxDelta;

  public GuessVegetationClassesRunnable( final IProfil profile, final boolean overwriteValues, final Double delta )
  {
    m_profile = profile;
    m_overwriteValues = overwriteValues;
    m_maxDelta = delta;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final int propertyClazz = UpdateVegetationProperties.getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );

    final IWspmClassification clazzes = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( clazzes ) )
      throw new CoreException( new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing profile feature for profile %.3f km.", m_profile.getStation() ) ) );

    final List<IStatus> statis = new ArrayList<IStatus>();

    final ProfilOperation operation = new ProfilOperation( "guessing roughness class values", m_profile, true );

    final IProfileRecord[] points = m_profile.getPoints();
    for( final IProfileRecord point : points )
    {
      if( UpdateVegetationProperties.isWritable( m_overwriteValues, point, propertyClazz ) )
        continue;

      final Double ax = point.getBewuchsAx();
      final Double ay = point.getBewuchsAy();
      final Double dp = point.getBewuchsDp();

      if( Objects.isNull( ax, ay, dp ) )
      {
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing ks value - point: %.3f", point.getBreite() ) );
        statis.add( status );

        continue;
      }

      final IVegetationClass clazz = findMatchingClass( clazzes, ax, ax, dp );
      if( Objects.isNull( clazz ) )
      {
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( "Didn't found matching vegation class on point: %.3f", point.getBreite() ) );
        statis.add( status );

        continue;
      }

      operation.addChange( new PointPropertyEdit( point, propertyClazz, clazz.getName() ) );
    }

    new ProfilOperationJob( operation ).schedule();

    return StatusUtilities.createStatus( statis, String.format( "Updated roughness classes from roughness values on profile %.3f", m_profile.getStation() ) );
  }

  private IVegetationClass findMatchingClass( final IWspmClassification clazzes, final Double ax, final Double ay, final Double dp )
  {
    IVegetationClass ptr = null;
    double ptrDistance = Double.MAX_VALUE;

    final Coordinate base = new Coordinate( ax, ay, dp );

    final IVegetationClass[] vegetations = clazzes.getVegetationClasses();
    for( final IVegetationClass vegetation : vegetations )
    {
      final Coordinate c = toCoordinate( vegetation );
      if( Objects.isNull( c ) )
        continue;

      /* roughness is in range? */
      final double distance = JTSUtilities.distanceZ( base, c );
      if( distance == 0.0 )
        return vegetation;

      /* in range */
      if( distance > m_maxDelta )
        continue;

      if( distance < ptrDistance )
      {
        ptrDistance = distance;
        ptr = vegetation;
      }
    }

    return ptr;
  }

  private Coordinate toCoordinate( final IVegetationClass vegetation )
  {
    final BigDecimal ax = vegetation.getAx();
    final BigDecimal ay = vegetation.getAy();
    final BigDecimal dp = vegetation.getDp();
    if( Objects.isNull( ax, ay, dp ) )
      return null;

    return new Coordinate( ax.doubleValue(), ax.doubleValue(), dp.doubleValue() );
  }
}
