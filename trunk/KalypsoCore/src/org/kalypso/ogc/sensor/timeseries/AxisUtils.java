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
package org.kalypso.ogc.sensor.timeseries;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;

/**
 * @author Dirk Kuch
 */
public class AxisUtils implements TimeserieConstants
{
  public static boolean isDateAxis( final IAxis axis )
  {
    return isDateAxis( axis.getType() );
  }

  private static boolean isDateAxis( final String type )
  {
    return TYPE_DATE.equals( type );
  }

  public static boolean isValueAxis( final IAxis axis )
  {
    return isValueAxis( axis.getType() );
  }

  private static boolean isValueAxis( final String type )
  {
    if( isDataSrcAxis( type ) )
      return false;
    else if( isStatusAxis( type ) )
      return false;
    else if( isDateAxis( type ) )
      return false;

    // TODO so return true?
    return true;
  }

  public static boolean isStatusAxis( final IAxis axis )
  {
    return isStatusAxis( axis.getType() );
  }

  private static boolean isStatusAxis( final String type )
  {
    return KalypsoStatusUtils.STATUS_AXIS_TYPE.equals( type );
  }

  public static boolean isDataSrcAxis( final IAxis axis )
  {
    return isDataSrcAxis( axis.getType() );
  }

  private static boolean isDataSrcAxis( final String type )
  {
    return TYPE_DATA_SRC.equals( type );
  }
}
