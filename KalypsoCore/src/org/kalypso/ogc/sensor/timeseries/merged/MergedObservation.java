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
import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.impl.AbstractObservationDecorator;
import org.kalypso.ogc.sensor.impl.DefaultAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * encapsulates multiple observations.
 * 
 * @author Dirk Kuch
 */
public class MergedObservation extends AbstractObservationDecorator implements IObservation
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
   * @param metadata
   *          The metadata for this new observation. During merge process, all data-source information will be cleared
   *          and replaced by the new data source information.
   */
  public MergedObservation( final String href, final ObservationSource[] sources, final MetadataList metadata )
  {
    super( merge( href, sources, metadata ) );
  }

  private static IObservation merge( final String href, final ObservationSource[] sources, final MetadataList metadata )
  {
    final IAxis[] axes = getAxisList( sources );

    final MergeObservationWorker worker = new MergeObservationWorker( href, sources, axes, metadata );
    worker.execute( new NullProgressMonitor() );

    return worker.getObservation();
  }

  private static IAxis[] getAxisList( final ObservationSource[] sources )
  {
    for( final ObservationSource source : sources )
    {
      // *grmml* first model defines axes of result model?
      final IObservation observation = source.getObservation();
      final Collection<IAxis> resultAxes = new ArrayList<IAxis>();

      final IAxis[] axes = observation.getAxes();
      for( final IAxis axis : axes )
      {
        if( axis.isPersistable() )
          resultAxes.add( axis );
      }

      if( AxisUtils.findDataSourceAxis( axes ) == null )
      {
        final DefaultAxis dataSourceAxis = new DefaultAxis( ITimeseriesConstants.TYPE_DATA_SRC, ITimeseriesConstants.TYPE_DATA_SRC, "", Integer.class, false );
        resultAxes.add( dataSourceAxis );
      }

      return resultAxes.toArray( new IAxis[resultAxes.size()] );
    }

    return new IAxis[] {};
  }

}
