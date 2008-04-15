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
package org.kalypso.gmlschema.property.restriction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kalypso.gmlschema.annotation.AnnotationAdapterFactory;
import org.kalypso.gmlschema.annotation.ILanguageAnnontationProvider;

/**
 * @author doemming refactored kuch
 */
public class EnumerationRestriction implements IRestriction
{
  private final String m_lang;

  private final Map<Object, ILanguageAnnontationProvider> m_map;

  public EnumerationRestriction( Map<Object, ILanguageAnnontationProvider> map )
  {
    m_map = map;
    m_lang = AnnotationAdapterFactory.getPlatformLang();
  }

  public Object[] getEnumeration( )
  {
    return m_map.keySet().toArray();
  }

  public String[] getLabels( String language )
  {
    List<String> list = new LinkedList<String>();
    for( Entry<Object, ILanguageAnnontationProvider> map : m_map.entrySet() )
    {
      list.add( map.getValue().getAnnotation( language ).getLabel() );
    }

    return list.toArray( new String[] {} );
  }

  public String[] getLabels( )
  {
    return getLabels( m_lang );
  }

  public Map<Object, ILanguageAnnontationProvider> getMapping( )
  {
    return m_map;
  }

}
