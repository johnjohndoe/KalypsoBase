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
package org.kalypso.model.wspm.core.profil.impl.devider;

import java.util.HashMap;

import org.kalypso.model.wspm.core.profil.IProfilDevider;
import org.kalypso.model.wspm.core.profil.IProfilPoint;


public class ProfilDevider implements IProfilDevider
{
  private DEVIDER_TYP m_Typ;

  private IProfilPoint m_point;

  private String m_label;

  private final HashMap<Object, Object> m_properties = new HashMap<Object, Object>();

  public ProfilDevider( final DEVIDER_TYP typ, final IProfilPoint point )
  {
    m_Typ = typ;
    m_point = point;
  }

  public Object getValueFor( final Object key )
  {
    return m_properties.get( key );
  }

  public IProfilPoint setPoint( IProfilPoint point )
  {
    final IProfilPoint oldPkt = m_point;
    m_point = point;
    return oldPkt;
  }

  public void setValueFor( final Object key, final Object value )
  {
    m_properties.put( key, value );
  }

  public IProfilPoint getPoint( )
  {
    return m_point;
  }

  public DEVIDER_TYP getTyp( )
  {
    return m_Typ;
  }

  public String getLabel( )
  {
    return m_label;
  }

}
