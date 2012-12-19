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
package org.kalypso.model.wspm.core.util.roughnesses;

import java.math.BigDecimal;
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
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.core.gml.classifications.helper.WspmClassifications;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.changes.PointPropertyEdit;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.util.vegetation.UpdateVegetationProperties;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;

/**
 * Guess roughness class from existing ks / kst value
 * 
 * @author Dirk Kuch
 */
public class GuessRoughessClassesRunnable implements ICoreRunnableWithProgress
{
  private final IProfile m_profile;

  private final String m_property;

  private final boolean m_overwriteValues;

  private final Double m_maxDelta;

  private final Set<IProfileChange> m_changes = new LinkedHashSet<>();

  public GuessRoughessClassesRunnable( final IProfile profile, final String property, final boolean overwriteValues, final Double delta )
  {
    m_profile = profile;
    m_property = property;
    m_overwriteValues = overwriteValues;
    m_maxDelta = delta;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final int property = UpdateVegetationProperties.getPropety( m_profile, m_property );
    final int propertyClazz = UpdateVegetationProperties.getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );

    final IWspmClassification clazzes = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( clazzes ) )
      throw new CoreException( new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "GuessRoughessClassesRunnable_0" ), m_profile.getStation() ) ) ); //$NON-NLS-1$

    final IComponent component = m_profile.getPointPropertyFor( m_property );
    final String componentLabel = ComponentUtilities.getComponentLabel( component );

    final List<IStatus> statis = new ArrayList<>();

    final IProfileRecord[] points = m_profile.getPoints();
    for( final IProfileRecord point : points )
    {
      if( !UpdateVegetationProperties.isWritable( m_overwriteValues, point, propertyClazz ) )
        continue;

      final Double value = (Double)point.getValue( property );
      if( Objects.isNull( value ) )
      {

        final Double width = (Double)point.getValue( m_profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE ) );
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "GuessRoughessClassesRunnable_1" ), width, componentLabel ) ); //$NON-NLS-1$
        statis.add( status );

        continue;
      }

      final IRoughnessClass clazz = findMatchingClass( clazzes, value );
      if( Objects.isNull( clazz ) )
      {
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "GuessRoughessClassesRunnable_2" ), componentLabel, value, point.getBreite() ) ); //$NON-NLS-1$
        statis.add( status );

        continue;
      }

      m_changes.add( new PointPropertyEdit( m_profile, point, propertyClazz, clazz.getName() ) );
    }

    return StatusUtilities.createStatus( statis, String.format( Messages.getString( "GuessRoughessClassesRunnable_3" ), m_profile.getStation() ) ); //$NON-NLS-1$
  }

  private IRoughnessClass findMatchingClass( final IWspmClassification clazzes, final Double value )
  {
    IRoughnessClass ptr = null;
    double ptrDiff = Double.MAX_VALUE;

    final IRoughnessClass[] roughnesses = clazzes.getRoughnessClasses();
    for( final IRoughnessClass roughness : roughnesses )
    {
      final BigDecimal v = roughness.getValue( m_property );
      if( v == null )
        continue;

      /* roughness is in range? */
      final double delta = Math.abs( v.doubleValue() - value );
      if( delta == 0.0 )
        return roughness;

      if( delta > m_maxDelta )
        continue;

      if( delta < ptrDiff )
      {
        ptrDiff = delta;
        ptr = roughness;
      }
    }

    return ptr;
  }

  public IProfileChange[] getChanges( )
  {
    return m_changes.toArray( new IProfileChange[] {} );
  }
}
