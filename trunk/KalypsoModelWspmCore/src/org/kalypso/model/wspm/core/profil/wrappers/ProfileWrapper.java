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

import java.util.Set;
import java.util.TreeSet;

import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.util.WspmProfileHelper;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class ProfileWrapper
{

  private final IProfil m_profile;

  public ProfileWrapper( final IProfil profile )
  {
    m_profile = profile;
  }

  public boolean hasPoint( final double width )
  {
    if( findPoint( width ) == null )
      return false;

    return true;
  }

  private ProfilePointWrapper findPoint( final Double width )
  {
    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final ProfilePointWrapper wrapper = new ProfilePointWrapper( point );
      final double breite = wrapper.getBreite();
      if( breite == width )
        return wrapper;
    }

    return null;
  }

  public ProfilePointWrapper addPoint( final Double width )
  {
    if( hasPoint( width ) )
      return findPoint( width );

    final IRecord[] points = m_profile.getPoints();
    ProfilePointWrapper base = null;
    for( final IRecord point : points )
    {
      final ProfilePointWrapper wrapper = new ProfilePointWrapper( point );
      final double breite = wrapper.getBreite();

      if( breite < width )
        base = wrapper;
      else
        break;
    }

    if( base == null )
      throw new IllegalStateException();

    final IRecord add = m_profile.createProfilPoint();
    final ProfilePointWrapper addWrapper = new ProfilePointWrapper( add );
    addWrapper.setBreite( width );
    addWrapper.setHoehe( base.getHoehe() );

    final IRecord added = WspmProfileHelper.addRecordByWidth( m_profile, add );
    return new ProfilePointWrapper( added );
  }

  public ProfilePointWrapper[] findPointsBetween( final Double p1, final Double p2 )
  {
    final Double min = Math.min( p1, p2 );
    final Double max = Math.max( p1, p2 );

    final Set<ProfilePointWrapper> between = new TreeSet<ProfilePointWrapper>( ProfilePointWrapper.COMPARATOR );

    final IRecord[] points = m_profile.getPoints();
    for( final IRecord point : points )
    {
      final ProfilePointWrapper wrapper = new ProfilePointWrapper( point );
      final double breite = wrapper.getBreite();

      if( breite >= min && breite <= max )
      {
        between.add( wrapper );
      }
    }

    return between.toArray( new ProfilePointWrapper[] {} );
  }

}
