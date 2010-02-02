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
package org.kalypso.ogc.gml.serialize;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.core.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

/**
 * @author felipe maximino
 *
 */
public class GmlTriSurface2HmoConverter extends Gml2HmoConverter
{
  /* maps a GM_Triangle to its GM_Position's */
  private List<int[]> trianglesPositionsMap;
  
  /* not repeated GM_Positions */
  private List<GM_Position> uniquePositions;
  
  /* to control which 'position' is already and where in the list */
  private Map<GM_Position, Integer> isOnIndex;
    
  
  public GmlTriSurface2HmoConverter( final GM_TriangulatedSurface geometry )
  {
    trianglesPositionsMap = new ArrayList<int[]>( geometry.size() );
    uniquePositions = new ArrayList<GM_Position>();
    isOnIndex = new HashMap<GM_Position, Integer>();  
    
    addsGeometry(geometry); 
  }
  
  public void addsGeometry( final GM_TriangulatedSurface geometry )
  { 
    for( final GM_Triangle triangle : geometry )
    { 
      /* reference to the positions of this triangle */
      int[] posReferences = new int[3]; 
      
      int count = 0;
      /* iterates through each triangle positions, but the last
       * that is the same as the first 
       */      
      final GM_Position[] positions = triangle.getExteriorRing();      
      for(int i = 0; i < positions.length - 1; i++) 
      { 
        GM_Position pos = positions[ i ];
        Integer isOn = isOnIndex.get( pos );
        
        /* the position is not repeated */
        if(  isOn == null )
        {             
          uniquePositions.add( pos ); 
          isOn = uniquePositions.size();
          isOnIndex.put( pos, isOn);
        }        
        posReferences[ count++ ] = isOn;      
      }
      
      trianglesPositionsMap.add( posReferences );
    }
  }  
  
  @Override
  public void writeHmo( final File hmoBaseFile) throws GmlSerializeException 
  {
    if(uniquePositions.isEmpty())
    {
      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.HMOSerializer.11" ) );
    }    

    HMOSerializer hmoSerializer = new HMOSerializer( hmoBaseFile ); 
    writePoints(hmoSerializer);
    writeTriangles(hmoSerializer);
    hmoSerializer.finish();
  }  
  
  public void writePoints(HMOSerializer serializer)
  {
    int count = 1;
    for(final GM_Position pos : uniquePositions)
    {
      serializer.formatPoint(count++, pos.getX(), pos.getY(), pos.getZ());
    }  
  }
  
  public void writeTriangles(HMOSerializer serializer)
  {
    int count = 1;
    for( final int[] ref : trianglesPositionsMap ) {      
      serializer.formatTriangle(count++, ref[0], ref[1], ref[2]);
    }  
  }
}
