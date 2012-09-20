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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyEdit;
import org.kalypso.observation.result.IRecord;

/**
 * updates a "simple" ks / kst value from roughness class
 *
 * @author Dirk Kuch
 */
public class UpdateVegetationProperties implements ICoreRunnableWithProgress
{
  private final IProfile m_profile;

  private final boolean m_overwrite;

  final Set<IProfileChange> m_changes = new LinkedHashSet<>();

  public UpdateVegetationProperties( final IProfile profile, final boolean overwrite )
  {
    m_profile = profile;
    m_overwrite = overwrite;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final int ax = getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
    final int ay = getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
    final int dp = getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );
    final int clazz = getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );

    final IWspmClassification clazzes = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( clazzes ) )
      throw new CoreException( new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "UpdateVegetationProperties_0" ), m_profile.getStation() ) ) ); //$NON-NLS-1$

    final List<IStatus> statis = new ArrayList<>();

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final String lnkClazz = (String) point.getValue( clazz );
      final IVegetationClass vegetation = clazzes.findVegetationClass( lnkClazz );

      if( Objects.isNull( vegetation ) )
      {
        final Double width = (Double) point.getValue( m_profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE ) );
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "UpdateVegetationProperties_1" ), width ) ); //$NON-NLS-1$
        statis.add( status );

        continue;
      }

      if( isWritable( m_overwrite, point, ax ) )
        m_changes.add( new PointPropertyEdit( m_profile, point, ax, vegetation.getAx().doubleValue() ) );
      if( isWritable( m_overwrite, point, ay ) )
        m_changes.add( new PointPropertyEdit( m_profile, point, ay, vegetation.getAy().doubleValue() ) );
      if( isWritable( m_overwrite, point, dp ) )
        m_changes.add( new PointPropertyEdit( m_profile, point, dp, vegetation.getDp().doubleValue() ) );
    }

    return StatusUtilities.createStatus( statis, String.format( Messages.getString( "UpdateVegetationProperties_2" ), m_profile.getStation() ) ); //$NON-NLS-1$
  }

  // FIXME move into helper
  public static int getPropety( final IProfile profile, final String property ) throws CoreException
  {
    final int index = profile.indexOfProperty( property );
    if( index == -1 )
    {
      final Status status = new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "UpdateVegetationProperties_3" ), profile.getStation(), property ) ); //$NON-NLS-1$
      throw new CoreException( status );
    }

    return index;
  }

  // FIXME: move into helper
  public static boolean isWritable( final boolean overwrite, final IRecord point, final int property )
  {
    if( overwrite )
      return true;

    return Objects.isNull( point.getValue( property ) );
  }

  public IProfileChange[] getChanges( )
  {
    return m_changes.toArray( new IProfileChange[] {} );
  }
}
