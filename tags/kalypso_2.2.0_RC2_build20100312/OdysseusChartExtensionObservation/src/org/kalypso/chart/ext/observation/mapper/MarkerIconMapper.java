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
package org.kalypso.chart.ext.observation.mapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.ImageDescriptor;

import de.openali.odysseus.chart.ext.base.axis.AbstractRetinalMapper;
import de.openali.odysseus.chart.framework.model.data.impl.StringDataOperator;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ImageMarker;

/**
 * @author burtscher1
 */
public class MarkerIconMapper extends AbstractRetinalMapper
{

  private final Map<Number, ImageDescriptor> m_mapping = new HashMap<Number, ImageDescriptor>();

  public MarkerIconMapper( String id, LinkedHashMap<String, ImageDescriptor> mapping )
  {
    super( id );

    StringDataOperator sdop = new StringDataOperator( mapping.keySet().toArray( new String[mapping.size()] ) );
    addDataOperator( String.class, sdop );

    // Umformen des Mappings, damit direkt über Zahlen zugegriffen werden kann
    for( Entry<String, ImageDescriptor> e : mapping.entrySet() )
      m_mapping.put( sdop.logicalToNumeric( e.getKey() ), e.getValue() );

  }

  /**
   * @return if no mapping is possible or a non-IPointStyle is given, an invisible copy of the style is returned
   * @see de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper#numericToScreen(java.lang.Number,
   *      de.openali.odysseus.chart.framework.model.style.IStyle)
   */
  public IStyle numericToScreen( Number value, IStyle bluePrintStlye )
  {
    if( m_mapping.get( value ) != null )
      if( bluePrintStlye instanceof IPointStyle )
      {
        IPointStyle copy = ((IPointStyle) bluePrintStlye).copy();
        copy.setMarker( new ImageMarker( m_mapping.get( value ) ) );
        return copy;
      }

    // Falls kein Mapping vorhanden oder falscher Style-Typ, wird ein unsichtbarer Style zurückgegeben
    IStyle copy = bluePrintStlye.copy();
    copy.setVisible( false );
    return copy;
  }
}
