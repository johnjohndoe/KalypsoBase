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
package org.kalypso.swtchart.chart.styles;

import java.util.ArrayList;

import org.kalypso.swtchart.chart.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
 */
public class LayerStyle implements ILayerStyle
{
  private ArrayList<IStyledElement> m_elements;

  public LayerStyle( )
  {
    m_elements = new ArrayList<IStyledElement>();
  }

  public void add( IStyledElement se )
  {
    m_elements.add( se );
  }

  /*
   * TODO: Besser Map<Type, List<Styles>>
   */

  /**
   * @see org.kalypso.swtchart.styles.ILayerStyle#getElement(org.kalypso.swtchart.styles.IStyleConstants.SE_TYPE, int)
   *      Gibt das StyledElement zurück, das sich an der gewünschten Position befindet; wenn es keines an der Position
   *      gibt, wird das letzte zurückgegeben - oder null, wenns garkeines von dem Typ gibt
   * @param type
   *          Typ des gesuchten StyledElements
   * @param pos
   *          Position des Elements - nur sinnvoll, falls es meherere des gleichen Typs gibt
   * @return das gefundene IStyledElement oder null
   */
  public IStyledElement getElement( SE_TYPE type, int pos )
  {
    IStyledElement elt = null;
    int count = 0;
    for( IStyledElement se : m_elements )
    {
      if( se.getType() == type )
      {
        elt = se;
        count++;
        if( pos == count )
          break;
      }
    }
    return elt;
  }

}
