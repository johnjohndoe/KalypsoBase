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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;
import org.kalypso.ogc.sensor.timeseries.wq.IWQConverter;

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
    if( Objects.isNull( axis ) )
      return false;

    return isDateAxis( axis.getType() );
  }

  public static boolean isDateAxis( final String type )
  {
    return TYPE_DATE.equals( type );
  }

  public static boolean isValueAxis( final IAxis axis )
  {
    return isValueAxis( axis, true );
  }

  private static boolean isValueAxis( final IAxis axis, final boolean persistable )
  {
    if( Objects.isNull( axis ) )
      return false;

    if( isDateAxis( axis ) )
      return false;
    else if( isDataSrcAxis( axis ) )
      return false;
    else if( isStatusAxis( axis ) )
      return false;
    else if( persistable && !axis.isPersistable() )
      return false;

    return true;
  }

  public static boolean isStatusAxis( final IAxis axis )
  {
    if( Objects.isNull( axis ) )
      return false;

    return isStatusAxis( axis.getType() );
  }

  public static boolean isStatusAxis( final String type )
  {
    return KalypsoStatusUtils.STATUS_AXIS_TYPE.equals( type );
  }

  public static boolean isDataSrcAxis( final IAxis axis )
  {
    if( Objects.isNull( axis ) )
      return false;

    return isDataSrcAxis( axis.getType() );
  }

  public static boolean isDataSrcAxis( final String type )
  {
    return TYPE_DATA_SRC.equals( type );
  }

  public static IAxis findValueAxis( final IAxis[] axes )
  {
    return findValueAxis( axes, true );
  }

  public static IAxis findValueAxis( final IAxis[] axes, final boolean persistable )
  {
    for( final IAxis axis : axes )
    {
      if( isValueAxis( axis, persistable ) )
        return axis;
    }

    return null;
  }

  public static IAxis findDataSourceAxis( final Collection<IAxis> axes, final IAxis valueAxis )
  {
    return findDataSourceAxis( axes.toArray( new IAxis[] {} ), valueAxis );
  }

  public static IAxis findDataSourceAxis( final IAxis[] axes, final IAxis valueAxis )
  {
    if( Arrays.isEmpty( axes ) || Objects.isNull( valueAxis ) )
      return null;

    /** fallback = old behavior of finding data source axes */
    IAxis fallback = null;

    for( final IAxis axis : axes )
    {
      if( isDataSrcAxis( axis ) )
      {
        if( StringUtils.equals( axis.getName(), DataSourceHelper.getDataSourceName( valueAxis ) ) )
          return axis;

        if( Objects.isNull( fallback ) && isPlainDataSourceAxis( axis ) )
          fallback = axis;
      }

    }

    return fallback;
  }

  /**
   * @return data source axis is not bound to a special value axis?
   */
  private static boolean isPlainDataSourceAxis( final IAxis axis )
  {
    if( Objects.isNull( axis ) )
      return false;

    if( !isDataSrcAxis( axis ) )
      return false;

    return !axis.getName().contains( "_dataSource_" );//$NON-NLS-1$ 
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
    return KalypsoStatusUtils.findStatusAxes( axes );
  }

  public static IAxis[] findValueAxes( final Collection<IAxis> axes )
  {
    return findValueAxes( axes.toArray( new IAxis[] {} ), true );
  }

  public static IAxis[] findValueAxes( final IAxis[] axes )
  {
    return findValueAxes( axes, true );
  }

  public static IAxis[] findValueAxes( final IAxis[] axes, final boolean persistable )
  {
    final List<IAxis> valueAxes = new ArrayList<>();
    for( final IAxis axis : axes )
    {
      if( isValueAxis( axis, persistable ) )
        valueAxes.add( axis );
    }

    return valueAxes.toArray( new IAxis[] {} );
  }

  public static IAxis[] findDataSourceAxes( final IAxis[] axes )
  {
    final List<IAxis> dataSourceAxes = new ArrayList<>();
    for( final IAxis axis : axes )
    {
      if( isDataSrcAxis( axis ) )
        dataSourceAxes.add( axis );
    }

    return dataSourceAxes.toArray( new IAxis[] {} );
  }

  public static IAxis findStatusAxis( final IAxis[] axes, final IAxis valueAxis )
  {
    if( Arrays.isEmpty( axes ) || Objects.isNull( valueAxis ) )
      return null;

    return KalypsoStatusUtils.findStatusAxisFor( axes, valueAxis );

  }

  public static IAxis findAxis( final IAxis[] axes, final String type )
  {
    for( final IAxis axis : axes )
    {
      if( axis.getType().equals( type ) )
        return axis;
    }

    return null;
  }

  public static boolean hasAxis( final IAxis[] axes, final String type )
  {
    return Objects.isNotNull( findAxis( axes, type ) );
  }

  public static IAxis findAxisByName( final IAxis[] axes, final String name )
  {
    for( final IAxis axis : axes )
    {
      if( axis.getName().equals( name ) )
        return axis;
    }

    return null;
  }

  public static boolean isEqual( final IAxis a1, final IAxis a2 )
  {
    if( Objects.isNull( a1, a2 ) )
      return false;

    if( Objects.equal( a1, a2 ) )
      return true;

    final EqualsBuilder builder = new EqualsBuilder();
    builder.append( a1.getName(), a2.getName() );
    builder.append( a1.getType(), a2.getType() );
    builder.append( a1.getUnit(), a2.getUnit() );

    return builder.isEquals();
  }

  /**
   * @param converter
   *          Needed to guess the right persistent axis for a non-persistent one.
   */
  public static IAxis findPersistentAxis( final IAxis[] axes, final IWQConverter converter, final IAxis axis )
  {
    if( axis.isPersistable() )
      return axis;

    final boolean isStatusAxis = isStatusAxis( axis );

    IAxis inputAxis;
    if( isStatusAxis )
      inputAxis = KalypsoStatusUtils.findAxisForStatusAxis( axes, axis );
    else
      inputAxis = axis;

    final String type = inputAxis.getType();
    final String persistentType = findConvertedType( converter, type );
    if( persistentType == null )
      throw new IllegalArgumentException( String.format( "Unable to find persistent axis for type: %s", type ) ); //$NON-NLS-1$

    final IAxis persistentAxis = findAxis( axes, persistentType );
    if( persistentAxis == null )
      throw new IllegalArgumentException( String.format( "Persistent axis with type does not exist: %s", persistentType ) ); //$NON-NLS-1$

    if( isStatusAxis )
      return AxisUtils.findStatusAxis( axes, persistentAxis );
    else
      return persistentAxis;
  }

  private static String findConvertedType( final IWQConverter converter, final String type )
  {
    if( type.equals( converter.getFromType() ) )
      return converter.getToType();

    if( type.equals( converter.getToType() ) )
      return converter.getFromType();

    return null;
  }
}