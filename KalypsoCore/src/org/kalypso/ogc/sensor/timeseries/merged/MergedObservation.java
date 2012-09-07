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
package org.kalypso.ogc.sensor.timeseries.merged;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.impl.AbstractObservationDecorator;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHelper;

/**
 * encapsulates multiple observations.
 *
 * @author Dirk Kuch
 */
public class MergedObservation extends AbstractObservationDecorator
{
  /**
   * @deprecated For backwards compability only: used to create the metadata of the merged observation, usually a copy
   *             of the first source-observation. Instead, this decision (which metadata to use) should be made outside
   *             of this class.
   */
  @Deprecated
  public static MetadataList getMetaData( final ObservationSource[] sources )
  {
    // FIXME: instead, we should get an metadata template from outside,
    // it is not possible to decide here which metadata to use!

    for( final ObservationSource source : sources )
    {
      // *grmml* first model defines axes of result model?
      final IObservation observation = source.getObservation();

      // keep original meta data for a later processing
      final MetadataList clone = MetadataHelper.clone( observation.getMetadataList() );
      final DataSourceHandler handler = new DataSourceHandler( clone );
      handler.removeAllDataSources();

      return clone;
    }

    return new MetadataList();
  }

  /**
   * Same as {@link MergedObservation#MergedObservation(String, ObservationSource[], MetadataList, null)}
   */
  public MergedObservation( final String href, final ObservationSource[] sources, final MetadataList metadata )
  {
    this( href, sources, metadata, null );
  }

  /**
   * @param forceAxisType
   *          If set to non-<code>null</code>, we use the axis from the source that contains an persistable axis of this
   *          type. Else, the axes from the first source are used.
   * @param metadata
   *          The metadata for this new observation. During merge process, all data-source information will be cleared
   *          and replaced by the new data source information.
   */
  public MergedObservation( final String href, final ObservationSource[] sources, final MetadataList metadata, final String forceAxisType )
  {
    super( merge( href, sources, metadata, forceAxisType ) );
  }

  private static IObservation merge( final String href, final ObservationSource[] sources, final MetadataList metadata, final String forceAxisType )
  {
    final IAxis[] axes = getAxisList( sources, forceAxisType );
    final IAxis[] axesWithSources = addSourceAxes( axes );

    final MergeObservationWorker worker = new MergeObservationWorker( href, sources, axesWithSources, metadata );
    worker.execute( new NullProgressMonitor() );

    return worker.getObservation();
  }

  private static IAxis[] addSourceAxes( final IAxis[] axes )
  {
    final Collection<IAxis> resultAxes = new ArrayList<>();

    resultAxes.addAll( Arrays.asList( axes ) );

    for( final IAxis axis : axes )
    {
      if( !AxisUtils.isValueAxis( axis ) )
        continue;

      if( AxisUtils.findDataSourceAxis( axes, axis ) == null )
      {
        final IAxis dataSourceAxis = DataSourceHelper.createSourceAxis( axis );
        resultAxes.add( dataSourceAxis );
      }
    }

    return resultAxes.toArray( new IAxis[resultAxes.size()] );
  }

  private static IAxis[] getAxisList( final ObservationSource[] sources, final String forceAxisType )
  {
    final ObservationSource source = findAxesSource( sources, forceAxisType );
    if( source == null )
      return new IAxis[] {};

    // *grmml* first model defines axes of result model?
    final IObservation observation = source.getObservation();
    final Collection<IAxis> valuesAxes = new ArrayList<>();

    final IAxis[] axes = observation.getAxes();
    for( final IAxis axis : axes )
    {
      if( axis.isPersistable() )
        valuesAxes.add( axis );
    }

    final Collection<IAxis> valueAxesAndSourceAxes = new ArrayList<>( valuesAxes );

    for( final IAxis axis : valuesAxes )
    {
      if( !AxisUtils.isValueAxis( axis ) )
        continue;

      if( AxisUtils.findDataSourceAxis( axes, axis ) == null )
      {
        final IAxis dataSourceAxis = DataSourceHelper.createSourceAxis( axis );
        valueAxesAndSourceAxes.add( dataSourceAxis );
      }
    }

    return valueAxesAndSourceAxes.toArray( new IAxis[valueAxesAndSourceAxes.size()] );
  }

  private static ObservationSource findAxesSource( final ObservationSource[] sources, final String forceAxisType )
  {
    if( sources.length < 1 )
      return null;

    if( StringUtils.isBlank( forceAxisType ) )
      return sources[0];

    /* If axis type is set: search for observation with that (persistable) axis */
    for( final ObservationSource observationSource : sources )
    {
      final IAxis[] axes = observationSource.getObservation().getAxes();
      final IAxis forcedAxis = AxisUtils.findAxis( axes, forceAxisType );
      if( forcedAxis != null && forcedAxis.isPersistable() )
        return observationSource;
    }

    return sources[0];
  }
}
