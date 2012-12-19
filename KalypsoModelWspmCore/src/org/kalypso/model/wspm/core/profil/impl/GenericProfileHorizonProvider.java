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
package org.kalypso.model.wspm.core.profil.impl;

import org.kalypso.model.wspm.core.gml.ProfileObjectBinding;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.IProfileObjectProvider;
import org.kalypso.model.wspm.core.profil.ProfileObjectHelper;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Holger Albert
 */
public class GenericProfileHorizonProvider implements IProfileObjectProvider
{
  @Override
  public IProfileObject createProfileObject( final IProfile profile )
  {
    // Thats the meaning of 'Generic' -> cvannot be created via it's id, the id does not correspond to a provider

    throw new UnsupportedOperationException();
  }

  @Override
  public IProfileObject buildProfileObject( final IProfile profile, final Feature profileObjectFeature )
  {
    /* Create the profile object. */
    final String id = profileObjectFeature.getName();

    final GenericProfileHorizon profileObject = new GenericProfileHorizon( id );

    /* REMARK: Handle feature as profile object binding (new style). */
    final ProfileObjectBinding profileObjectBinding = (ProfileObjectBinding)profileObjectFeature;

    /* Fill the records and the metadata. */
    ProfileObjectHelper.fillRecords( profileObjectBinding, profileObject );
    ProfileObjectHelper.fillMetadata( profileObjectBinding, profileObject );

    return profileObject;
  }
}