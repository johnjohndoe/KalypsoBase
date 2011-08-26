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
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

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

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @SuppressWarnings("deprecation")
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final IComponent propertyAx = getPropety( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
    final IComponent propertyAy = getPropety( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
    final IComponent propertyDp = getPropety( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );
    final IComponent propertyClazz = getPropety( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );

    final IWspmClassification clazzes = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( clazzes ) )
      throw new CoreException( new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing profile feature for profile %.3f km.", m_profile.getStation() ) ) );

    final List<IStatus> statis = new ArrayList<IStatus>();

    final ProfilOperation operation = new ProfilOperation( "guessing roughness class values", m_profile, true );

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final Double ax = getProperty( point, propertyAx );
      final Double ay = getProperty( point, propertyAy );
      final Double dp = getProperty( point, propertyDp );

      if( Objects.isNull( ax, ay, dp ) )
      {
        final Double width = getProperty( point, m_profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BREITE ) );
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( "Missing ks value - point: %.3f", width ) );
        statis.add( status );

        continue;
      }

      final IVegetationClass clazz = findMatchingClass( clazzes, ax, ax, dp );
      if( Objects.isNull( clazz ) )
      {
        final Double width = (Double) point.getValue( m_profile.hasPointProperty( IWspmPointProperties.POINT_PROPERTY_BREITE ) );
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( "Didn't found matching vegation class on point: %.3f", width ) );
        statis.add( status );

        continue;
      }

      if( isWritable( point, propertyClazz ) )
        operation.addChange( new PointPropertyEdit( point, propertyClazz, clazz.getName() ) );
    }

    new ProfilOperationJob( operation ).schedule();

    return StatusUtilities.createStatus( statis, String.format( "Updated roughness classes from roughness values on profile %.3f", m_profile.getStation() ) );
  }

  private Double getProperty( final IRecord point, final IComponent property )
  {
    final Object value = point.getValue( property );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return null;
  }

  private boolean isWritable( final IRecord point, final IComponent propertyClazz )
  {
    if( m_overwriteValues )
      return true;

    return Objects.isNull( point.getValue( propertyClazz ) );
  }

  private IComponent getPropety( final String property ) throws CoreException
  {
    final IComponent ax = m_profile.hasPointProperty( property );
    if( Objects.isNull( ax ) )
    {
      final Status status = new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( "Can't update profile %.3f km. Missing point property: %s", m_profile.getStation(), property ) );
      throw new CoreException( status );
    }

    return ax;
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
