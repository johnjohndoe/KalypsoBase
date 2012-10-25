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
 * @author Holger Albert
 */
public class SimpleProfileSelection implements IProfileSelection
{
  private final IProfile m_profile;

  public SimpleProfileSelection( final IProfile profile )
  {
    m_profile = profile;
  }

  @Override
  public boolean isEmpty( )
  {
    if( m_profile == null )
      return false;

    return true;
  }

  @Override
  public Object getSource( )
  {
    return null;
  }

  @Override
  public IProfileFeature getProfileFeature( )
  {
    return null;
  }

  @Override
  public IProfile getProfile( )
  {
    return m_profile;
  }

  @Override
  public Object getResult( )
  {
    return null;
  }

  @Override
  public void addProfilListener( final IProfileListener profileListener )
  {
    if( m_profile == null )
      return;

    m_profile.addProfilListener( profileListener );
  }

  @Override
  public void removeProfileListener( final IProfileListener profileListener )
  {
    if( m_profile == null )
      return;

    m_profile.addProfilListener( profileListener );
  }
}