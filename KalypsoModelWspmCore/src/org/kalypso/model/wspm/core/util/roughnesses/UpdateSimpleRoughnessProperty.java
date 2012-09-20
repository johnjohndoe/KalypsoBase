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

/**
 * updates a "simple" ks / kst value from roughness class
 *
 * @author Dirk Kuch
 */
public class UpdateSimpleRoughnessProperty implements ICoreRunnableWithProgress
{
  private final IProfile m_profile;

  private final String m_property;

  private final boolean m_overwrite;

  private final Set<IProfileChange> m_changes = new LinkedHashSet<>();

  public UpdateSimpleRoughnessProperty( final IProfile profile, final String property, final boolean overwrite )
  {
    m_profile = profile;
    m_property = property;
    m_overwrite = overwrite;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final int property = UpdateVegetationProperties.getPropety( m_profile, m_property );
    final int clazz = UpdateVegetationProperties.getPropety( m_profile, IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );

    final IWspmClassification clazzes = WspmClassifications.getClassification( m_profile );
    if( Objects.isNull( clazzes ) )
      throw new CoreException( new Status( IStatus.CANCEL, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "UpdateSimpleRoughnessProperty_0" ), m_profile.getStation() ) ) ); //$NON-NLS-1$

    final List<IStatus> statis = new ArrayList<>();

    final IProfileRecord[] points = m_profile.getPoints();
    for( final IProfileRecord point : points )
    {
      final String lnkClazz = (String) point.getValue( clazz );
      final IRoughnessClass roughness = clazzes.findRoughnessClass( lnkClazz );

      if( Objects.isNull( roughness ) )
      {
        final IStatus status = new Status( IStatus.WARNING, KalypsoModelWspmCorePlugin.getID(), String.format( Messages.getString( "UpdateSimpleRoughnessProperty_1" ), point.getBreite() ) ); //$NON-NLS-1$
        statis.add( status );

        continue;
      }

      if( UpdateVegetationProperties.isWritable( m_overwrite, point, property ) )
      {
        final BigDecimal value = roughness.getValue( m_property );
        final Double dblValue = value == null ? null : value.doubleValue();
        m_changes.add( new PointPropertyEdit( m_profile, point, property, dblValue ) );
      }
    }

    return StatusUtilities.createStatus( statis, String.format( Messages.getString( "UpdateSimpleRoughnessProperty_2" ), m_profile.getStation() ) ); //$NON-NLS-1$
  }

  public IProfileChange[] getChanges( )
  {
    return m_changes.toArray( new IProfileChange[] {} );
  }
}