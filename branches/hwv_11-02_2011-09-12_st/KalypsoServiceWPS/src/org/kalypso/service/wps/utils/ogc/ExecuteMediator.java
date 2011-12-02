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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.IOValueType;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author kurzbach
 */
public class ExecuteMediator extends AbstractWPSMediator<net.opengis.wps._1_0.Execute, net.opengeospatial.wps.Execute>
{

  public ExecuteMediator( final Object collegue )
  {
    super( collegue );
  }

  /**
   * Returns the process identifier
   */
  public String getProcessId( )
  {
    switch( getVersion() )
    {
      case V040:
        return getV04().getIdentifier().getValue();
      case V100:
        return getV10().getIdentifier().getValue();
    }
    return null;
  }

  public Map<String, Object> getInputList( )
  {
    final Map<String, Object> inputList = new LinkedHashMap<String, Object>();
    switch( getVersion() )
    {
      case V040:
        final DataInputsType dataInputs = getV04().getDataInputs();
        final List<IOValueType> inputs = dataInputs.getInput();
        for( final IOValueType input : inputs )
        {
          Object value = null;
          final String identifier = input.getIdentifier().getValue();
          if( input.getComplexValue() != null )
          {
            value = input.getComplexValue();
          }
          else if( input.getLiteralValue() != null )
          {
            value = input.getLiteralValue();
          }
          else if( input.getComplexValueReference() != null )
          {
            value = input.getComplexValueReference();
          }
          else if( input.getBoundingBoxValue() != null )
          {
            value = input.getBoundingBoxValue();
          }
          else
          {
            value = null;
          }
          inputList.put( identifier, value );
        }
        break;

      case V100:
        throw new NotImplementedException();
    }
    return inputList;
  }
}
