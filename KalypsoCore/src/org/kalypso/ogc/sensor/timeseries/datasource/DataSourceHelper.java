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
package org.kalypso.ogc.sensor.timeseries.datasource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.repository.IDataSourceItem;

/**
 * Helper code for multiple data source references - lke: filter://smeFilterClass?source_1=blub&source_2=blub2&...
 *
 * @author Dirk Kuch
 */
public final class DataSourceHelper
{
  private static final String PREFIX_SOURCE = "source_"; //$NON-NLS-1$

  private DataSourceHelper( )
  {
  }

  /**
   * @param reference
   *          filter://smeFilterClass?source_1=SRC_1&source_2=SRC_2&...
   * @return [SRC_1, SRC_2]
   */
  public static String[] getSources( final String reference )
  {
    if( !isFiltered( reference ) )
      return new String[] { reference };

    final String[] referenceParts = reference.split( "\\?" ); //$NON-NLS-1$
    if( referenceParts.length != 2 )
      return new String[] {};

    final List<String> sources = new ArrayList<>();

    final String[] parts = referenceParts[1].split( "\\&" ); //$NON-NLS-1$
    for( final String part : parts )
    {
      final String[] sourceParts = part.split( "\\=" ); //$NON-NLS-1$
      if( sourceParts.length == 2 && sourceParts[0].startsWith( PREFIX_SOURCE ) )
        sources.add( sourceParts[1] );
    }

    return sources.toArray( new String[] {} );
  }

  public static boolean isFiltered( final String reference )
  {
    return reference.startsWith( IDataSourceItem.FILTER_SOURCE );
  }

  public static boolean hasDataSources( final ITupleModel model )
  {
    final IAxis[] axes = model.getAxes();
    final IAxis[] dataSourceAxes = AxisUtils.findDataSourceAxes( axes );

    return ArrayUtils.isNotEmpty( dataSourceAxes );
  }

  public static IAxis createSourceAxis( final IAxis valueAxis )
  {
    return createSourceAxis( valueAxis, true );

  }

  public static IAxis createSourceAxis( final IAxis valueAxis, final boolean persistable )
  {
    final String name = getDataSourceName( valueAxis );
    final DefaultAxis axis = new DefaultAxis( name, ITimeseriesConstants.TYPE_DATA_SRC, StringUtils.EMPTY, Integer.class, false, persistable );

    return axis;
  }

  public static boolean isUnknown( final String identifier )
  {
    return IDataSourceItem.SOURCE_UNKNOWN.equals( identifier );
  }

  public static String getDataSourceName( final IAxis valueAxis )
  {
    return getDataSourceName( valueAxis.getType() );
  }

  public static String getDataSourceName( final String axisType )
  {
    return String.format( "_dataSource_%s", axisType ); //$NON-NLS-1$
  }

}
