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
package org.kalypso.commons.tokenreplace;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * @author Gernot Belger
 */
public class PropertiesStringResolver implements IStringResolver
{
  private final StrSubstitutor m_substitutor;

  /**
   * @deprecated use PropertiesStringResolver( final Map<String, Object> properties, final String prefix, final String
   *             suffix )
   */
  @Deprecated
  public PropertiesStringResolver( final Properties properties, final String prefix, final String suffix )
  {
    final Map<String, Object> map = new HashMap<String, Object>();

    final Set<Entry<Object, Object>> entries = properties.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      map.put( (String) entry.getKey(), entry.getValue() );
    }

    m_substitutor = new StrSubstitutor( map, prefix, suffix );
  }

  public PropertiesStringResolver( final Map<String, Object> properties, final String prefix, final String suffix )
  {
    m_substitutor = new StrSubstitutor( properties, prefix, suffix );
  }

  /**
   * @see org.kalypso.commons.tokenreplace.IStringResolver#resolve(java.lang.String)
   */
  @Override
  public String resolve( final String input )
  {
    return m_substitutor.replace( input );
  }
}