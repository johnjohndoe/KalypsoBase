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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;

/**
 * @author Dirk Kuch
 */
public final class AxisUtils implements ITimeseriesConstants
{
  private AxisUtils( )
  {
  }

  public static boolean isDateAxis( final IAxis axis )
  {
    return isDateAxis( axis.getType() );
  }

  public static boolean isDateAxis( final String type )
  {
    return TYPE_DATE.equals( type );
  }

  public static boolean isValueAxis( final IAxis axis )
  {
    if( isDateAxis( axis ) )
      return false;
    else if( isDataSrcAxis( axis ) )
      return false;
    else if( isStatusAxis( axis ) )
      return false;
    else if( !axis.isPersistable() )
      return false;

    // TODO so return true?
    return true;
  }

  public static boolean isStatusAxis( final IAxis axis )
  {
    return isStatusAxis( axis.getType() );
  }

  public static boolean isStatusAxis( final String type )
  {
    return KalypsoStatusUtils.STATUS_AXIS_TYPE.equals( type );
  }

  public static boolean isDataSrcAxis( final IAxis axis )
  {
    return isDataSrcAxis( axis.getType() );
  }

  public static boolean isDataSrcAxis( final String type )
  {
    return TYPE_DATA_SRC.equals( type );
  }

  public static IAxis findValueAxis( final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      if( isValueAxis( axis ) )
        return axis;
    }

    return null;
  }

  public static IAxis findDataSourceAxis( final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      if( isDataSrcAxis( axis ) )
        return axis;
    }

    return null;
  }

  public static IAxis findDateAxis( final IAxis[] axes )
  {
    for( final IAxis axis : axes )
    {
      if( isDateAxis( axis ) )
        return axis;
    }
    return null;
  }

  public static IAxis[] findStatusAxes( final IAxis[] axes )
  {
    final List<IAxis> statusAxes = new ArrayList<IAxis>();
    for( final IAxis axis : axes )
    {
      if( isStatusAxis( axis ) )
        statusAxes.add( axis );
    }

    return statusAxes.toArray( new IAxis[] {} );
  }

  public static IAxis[] findValueAxes( final IAxis[] axes )
  {
    final List<IAxis> valueAxes = new ArrayList<IAxis>();
    for( final IAxis axis : axes )
    {
      if( isValueAxis( axis ) )
        valueAxes.add( axis );
    }

    return valueAxes.toArray( new IAxis[] {} );
  }

  public static IAxis[] findDataSourceAxes( final IAxis[] axes )
  {
    final List<IAxis> dataSourceAxes = new ArrayList<IAxis>();
    for( final IAxis axis : axes )
    {
      if( isDataSrcAxis( axis ) )
        dataSourceAxes.add( axis );
    }

    return dataSourceAxes.toArray( new IAxis[] {} );
  }

  public static IAxis findStatusAxis( final IAxis[] axes )
  {
    final IAxis[] found = findStatusAxes( axes );
    if( ArrayUtils.isEmpty( found ) )
      return null;

    return found[0];
  }

  public static IAxis findAxis( final IAxis[] axes, final String identifier )
  {
    for( final IAxis axis : axes )
    {
      if( axis.getType().equals( identifier ) )
        return axis;
    }

    return null;
  }

}
