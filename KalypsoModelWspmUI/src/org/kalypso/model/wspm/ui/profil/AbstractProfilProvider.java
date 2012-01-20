/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.model.wspm.ui.profil;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.model.wspm.core.profil.IProfil;

/**
 * @author Gernot Belger
 */
public abstract class AbstractProfilProvider implements IProfilProvider
{
  private final List<IProfilProviderListener> m_listeners = new ArrayList<IProfilProviderListener>( 5 );

  private IProfil m_profile = null;

  @Override
  public void addProfilProviderListener( final IProfilProviderListener l )
  {
    m_listeners.add( l );
  }

  @Override
  public void removeProfilProviderListener( final IProfilProviderListener l )
  {
    m_listeners.remove( l );
  }

  protected void fireOnProfilProviderChanged( final IProfilProvider provider, final IProfil oldProfile, final IProfil newProfile )
  {
    final IProfilProviderListener[] ls = m_listeners.toArray( new IProfilProviderListener[m_listeners.size()] );
    for( final IProfilProviderListener l : ls )
    {
      l.onProfilProviderChanged( provider, oldProfile, newProfile );
    }
  }

  @Override
  public IProfil getProfil( )
  {
    return m_profile;
  }

  protected void setProfil( final IProfil oldProfile, final IProfil newProfile )
  {
    m_profile = newProfile;

    fireOnProfilProviderChanged( this, oldProfile, newProfile );
  }
}
