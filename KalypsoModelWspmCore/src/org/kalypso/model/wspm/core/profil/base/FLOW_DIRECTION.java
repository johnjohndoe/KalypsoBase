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

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileMetadata;

/**
 * direction eEstuary2Src = waterBody.isDirectionUpstreams
 * 
 * @author Dirk Kuch
 */
public enum FLOW_DIRECTION
{
  eSrc2Estuary,
  eEstuary2Src;

  public IProfile getUpperProfile( final IProfile p1, final IProfile p2 )
  {
    // FIXME: use StationComparator! Do not reimplement everything!

    final FLOW_DIRECTION direction = valueOf( name() );

    final double s1 = p1.getStation();
    final double s2 = p2.getStation();

    if( eSrc2Estuary.equals( direction ) )
    {
      if( s1 < s2 )
        return p1;

      return p2;
    }
    else if( eEstuary2Src.equals( direction ) )
    {
      if( s1 > s2 )
        return p1;

      return p2;
    }

    throw new UnsupportedOperationException();
  }

  public IProfile getLowerProfile( final IProfile p1, final IProfile p2 )
  {
    final FLOW_DIRECTION direction = valueOf( name() );

    final double s1 = p1.getStation();
    final double s2 = p2.getStation();

    if( eSrc2Estuary.equals( direction ) )
    {
      if( s1 > s2 )
        return p1;

      return p2;
    }
    else if( eEstuary2Src.equals( direction ) )
    {
      if( s1 < s2 )
        return p1;

      return p2;
    }

    throw new UnsupportedOperationException();
  }

  public boolean isUpstream( )
  {
    switch( this )
    {
      case eEstuary2Src:
        return true;
      case eSrc2Estuary:
        return false;
      default:
        return false;
    }
  }

  public static FLOW_DIRECTION toFlowDirection( final boolean upstream )
  {
    if( upstream )
      return eEstuary2Src;

    return eSrc2Estuary;
  }

  public static FLOW_DIRECTION findDirection( final IProfile... profiles )
  {
    for( final IProfile profile : profiles )
    {
      final IProfileMetadata metadata = profile.getMetadata();
      final String flowDirection = metadata.getMetadata( IWspmConstants.PROFIL_PROPERTY_FLOW_DIRECTION );
      if( flowDirection == null )
        continue;

      return FLOW_DIRECTION.toFlowDirection( Boolean.parseBoolean( flowDirection ) );
    }

    return null;
  }
}