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
package org.kalypso.model.wspm.core.profil.wrappers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfilePointMarker;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;

/**
 * @deprecated use IProfile / IProfileRecord implementation
 * @author Dirk Kuch
 */
@Deprecated
public class ProfileWrapper
{
  private final IProfile m_profile;

  public ProfileWrapper( final IProfile profile )
  {
    m_profile = profile;
  }

  public double getStation( )
  {
    return m_profile.getStation();
  }

  public boolean hasPoint( final double width )
  {
    final IProfileRecord point = ProfileVisitors.findPoint( m_profile, width );
    if( Objects.isNotNull( point ) )
      return true;

    return false;
  }

  public IProfilePointMarker[] getProfilePointMarkerWrapper( final String marker )
  {
    final List<IProfilePointMarker> wrappers = new ArrayList<>();

    final IProfilePointMarker[] markers = m_profile.getPointMarkerFor( marker );
    for( final IProfilePointMarker m : markers )
    {
      wrappers.add( m );
    }

    return wrappers.toArray( new IProfilePointMarker[] {} );
  }

  @Override
  public String toString( )
  {
    return String.format( Messages.getString( "ProfileWrapper_0" ), m_profile.getStation() ); //$NON-NLS-1$
  }

  public IProfile getProfile( )
  {
    return m_profile;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ProfileWrapper )
    {
      final ProfileWrapper other = (ProfileWrapper) obj;

      final String station = String.format( "%.3f", getStation() ); //$NON-NLS-1$
      final String otherStation = String.format( "%.3f", other.getStation() ); //$NON-NLS-1$

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( station, otherStation );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final String station = String.format( "%.3f", getStation() ); //$NON-NLS-1$

    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( station );

    return builder.toHashCode();
  }

  public void remove( final IProfileRecord... remove )
  {
    for( final IProfileRecord record : remove )
    {
      m_profile.removePoint( record );
    }
  }

  public double findLowestHeight( )
  {
    final IProfileRecord point = ProfileVisitors.findLowestPoint( m_profile );
    return point.getHoehe();

  }

  public IProfilePointMarker[] getPointMarkers( final IProfileRecord point )
  {
    final IProfilePointMarker[] markers = m_profile.getPointMarkerFor( point );
    if( ArrayUtils.isEmpty( markers ) )
      return new IProfilePointMarker[] {};

    final List<IProfilePointMarker> myMarkers = new ArrayList<>();

    for( final IProfilePointMarker marker : markers )
    {
      myMarkers.add( marker );
    }

    return myMarkers.toArray( new IProfilePointMarker[] {} );
  }

}
