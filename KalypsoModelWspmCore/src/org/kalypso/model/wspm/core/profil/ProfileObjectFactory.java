/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.core.profil;

import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.impl.GenericProfileHorizon;
import org.kalypso.model.wspm.core.profil.impl.GenericProfileHorizonProvider;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Dirk Kuch
 */
public final class ProfileObjectFactory
{
  private ProfileObjectFactory( )
  {
  }

  /**
   * Creates a profile object for the given object id.
   */
  public static IProfileObject createProfileObject( final IProfile profile, final String profileProviderId )
  {
    final IProfileObjectProvider provider = KalypsoModelWspmCoreExtensions.getProfileObjectProvider( profileProviderId );
    if( provider != null )
      return provider.createProfileObject( profile );

    /* Use generic provider for unknown id's */
    return new GenericProfileHorizon( profileProviderId );
  }

  public static IProfileObject createProfileObject( final IProfile profile, final Feature profileObjectFeature )
  {
    final String profileProviderId = profileObjectFeature.getName();

    final IProfileObjectProvider provider = KalypsoModelWspmCoreExtensions.getProfileObjectProvider( profileProviderId );
    if( provider != null )
      return provider.buildProfileObject( profile, profileObjectFeature );

    /* Use generic provider for unknown id's */
    return new GenericProfileHorizonProvider().buildProfileObject( profile, profileObjectFeature );
  }
}