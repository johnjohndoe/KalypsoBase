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
package org.kalypso.ogc.sensor.util;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationValueContainer;

/**
 * @author Gernot Belger
 */
final class ObservationValueContainer implements IObservationValueContainer
{
  private final ITupleModel m_model;

  private final int m_index;

  private final IObservation m_observation;

  ObservationValueContainer( final ITupleModel model, final int index, final IObservation observation )
  {
    m_model = model;
    m_index = index;
    m_observation = observation;
  }

  @Override
  public boolean hasAxis( final String... types )
  {
    for( final String type : types )
    {
      if( AxisUtils.findAxis( m_model.getAxes(), type ) == null )
        return false;
    }

    return true;
  }

  @Override
  public int getIndex( )
  {
    return m_index;
  }

  @Override
  public IAxis[] getAxes( )
  {
    return m_model.getAxes();
  }

  @Override
  public Object get( final IAxis axis ) throws SensorException
  {
    return m_model.get( m_index, axis );
  }

  @Override
  public Object getPrevious( final IAxis axis ) throws SensorException
  {
    if( m_index > 0 )
      return m_model.get( m_index - 1, axis );

    return null;
  }

  @Override
  public Object getNext( final IAxis axis ) throws SensorException
  {
    if( m_index + 1 < m_model.size() )
      return m_model.get( m_index + 1, axis );

    return null;
  }

  @Override
  public MetadataList getMetaData( )
  {
    return m_observation.getMetadataList();
  }

  @Override
  public void set( final IAxis axis, final Object value ) throws SensorException
  {
    m_model.set( m_index, axis, value );
  }

  @Override
  public TupleModelDataSet getDataSetFor( final String valueAxis ) throws SensorException
  {
    return getDataSetFor( getMetaData(), valueAxis );
  }

  @Override
  public TupleModelDataSet getDataSetFor( final MetadataList metadata, final String valueAxis ) throws SensorException
  {
    return TupleModelDataSet.toDataSet( this, metadata, valueAxis );
  }
}