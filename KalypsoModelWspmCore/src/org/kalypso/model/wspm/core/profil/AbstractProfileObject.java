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
package org.kalypso.model.wspm.core.profil;

import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * @author Kim Werner
 * @author Dirk Kuch
 */
public abstract class AbstractProfileObject implements IProfileObject
{
  // private final IProfil m_profile;

  private final IObservation<TupleResult> m_observation;

  protected AbstractProfileObject( final IObservation<TupleResult> observation )
  {
    // m_profile = profile;
    m_observation = observation;
  }

  @Deprecated
  protected IProfil getProfile( )
  {
    return null;
    // return m_profile;
  }

  @Override
  public IObservation<TupleResult> getObservation( )
  {
    return m_observation;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getObjectProperty(java.lang.String)
   */
  @Override
  public IComponent getObjectProperty( final String componentId )
  {
    final IComponent[] components = getObjectProperties();
    if( components.length < 1 )
      return null;
    for( final IComponent component : components )
    {
      if( component.getId().equals( componentId ) )
        return component;
    }
    return null;
  }

  @Deprecated
  protected void init( )
  {
// for( final String id : getProfileProperties() )
// {
// final IComponent property = m_profile.getPointPropertyFor( id );
// if( !m_profile.hasPointProperty( property ) )
// m_profile.addPointProperty( property );
// }
  }

  protected abstract String[] getProfileProperties( );

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getObjectProperties()
   */
  @Override
  public IComponent[] getObjectProperties( )
  {
    return getObservation().getResult().getComponents();
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfileObject#getPointProperties()
   */
  @Override
  @Deprecated
  public IComponent[] getPointProperties( )
  {
    return null;
// final List<IComponent> myProperties = new ArrayList<IComponent>();
// for( final String id : getProfileProperties() )
// {
// final IComponent component = m_profile.hasPointProperty( id );
// if( component != null )
// myProperties.add( component );
// }
// return myProperties.toArray( new IComponent[] {} );

  }

  protected static IComponent getObjectComponent( final String id )
  {
    return ProfilUtil.getFeatureComponent( id );
  }

}
