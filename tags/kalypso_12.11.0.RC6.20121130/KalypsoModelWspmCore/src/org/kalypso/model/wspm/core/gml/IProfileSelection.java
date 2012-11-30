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
package org.kalypso.model.wspm.core.gml;

import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;

/**
 * A selected profile, consists of {@link IProfileFeature} and its really selected source object (e.e. TuhhReachSegment)
 *
 * @author Gernot Belger
 */
public interface IProfileSelection
{
  /**
   * If <code>false</code> is returned, {@link #getProfileFeature()} and {@link #getProfile()} are garantueed to return non-<code>null</code> values.
   */
  boolean isEmpty( );

  Object getSource( );

  IProfileFeature getProfileFeature( );

  IProfile getProfile( );

  Object getResult( );

  /**
   * Adds a {@link IProfileListener} to the IProfile represented by this selection.<br/>
   * Does nothing, if this selection is empty.
   */
  void addProfilListener( IProfileListener profileListener );

  /**
   * Removes a {@link IProfileListener} to the IProfile represented by this selection.<br/>
   * Does nothing, if this selection is empty.
   */
  void removeProfileListener( IProfileListener profileListener );
}