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
package org.kalypso.ogc.sensor.filter.filters.interval;

import java.util.Calendar;

import org.joda.time.Period;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.datasource.DataSourceHandler;

/**
 * @author Gernot Belger
 */
public class IntervalFilterOperation
{
  private final IObservation m_input;

  private final IntervalDefinition m_definition;

  private final MetadataList m_targetMetadata;

  private final Period m_sourceTimestep;

  public IntervalFilterOperation( final IObservation input, final IntervalDefinition definition )
  {
    m_input = input;
    m_definition = definition;

    final MetadataList sourceMetadata = m_input.getMetadataList();

    m_sourceTimestep = MetadataHelper.getTimestep( sourceMetadata );

    m_targetMetadata = MetadataHelper.clone( sourceMetadata );
    definition.setTimestep( m_targetMetadata );
  }

  public IObservation execute( final DateRange range ) throws SensorException
  {
    final String href = m_input.getHref();
    final String name = m_input.getName();

    // BUGFIX: fixes the problem with the first value:
    // the first value was always ignored, because the interval
    // filter cannot handle the first value of the source observation
    // FIX: we just make the request a big bigger in order to get a new first value
    // HACK: we always use DAY, so that work fine only up to time series of DAY-quality.
    // Maybe there should be one day a mean to determine, which is the right amount.
    final ITupleModel sourceModel = ObservationUtilities.requestBuffered( m_input, range, Calendar.DAY_OF_MONTH, 2 );

    final DataSourceHandler targetSourcesHandler = new DataSourceHandler( m_targetMetadata );

    final IntervalValuesOperation valuesOp = new IntervalValuesOperation( sourceModel, m_sourceTimestep, targetSourcesHandler, m_definition );
    valuesOp.execute( range );
    final ITupleModel model = valuesOp.getModel();

    return new SimpleObservation( href, name, m_targetMetadata, model );
  }

}