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
package org.kalypso.service.wps.utils.ogc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kurzbach
 */
public class DescribeProcessMediator extends AbstractWPSMediator<net.opengis.wps._1_0.DescribeProcess, net.opengeospatial.wps.DescribeProcess>
{

  public DescribeProcessMediator( final Object collegue )
  {
    super( collegue );
  }

  /**
   * Returns all process identifiers from this request
   */
  public List<String> getProcessIdentifiers( )
  {
    final List<String> identifiers = new ArrayList<String>();
    switch( getVersion() )
    {
      case V040:
        final List<net.opengeospatial.ows.CodeType> identifier = getV04().getIdentifier();
        if( identifier != null )
        {
          for( final net.opengeospatial.ows.CodeType codeType : identifier )
          {
            identifiers.add( codeType.getValue() );
          }
        }
        break;
      case V100:
        final List<net.opengis.ows._1.CodeType> identifier2 = getV10().getIdentifier();
        if( identifier2 != null )
        {
          for( net.opengis.ows._1.CodeType codeType : identifier2 )
          {
            identifiers.add( codeType.getValue() );
          }
        }
    }
    return identifiers;
  }

}
