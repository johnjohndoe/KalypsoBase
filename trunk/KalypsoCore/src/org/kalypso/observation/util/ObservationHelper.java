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
package org.kalypso.observation.util;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTuppleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.xml.sax.InputSource;

/**
 * @author Dirk Kuch
 */
public final class ObservationHelper
{
  public static final int HISTORY = -40;

  private ObservationHelper( )
  {
  }

  public static byte[] flushMetaDataToByteArray( final IObservation observation ) throws SensorException
  {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      /* serialize observation to xml (atm we need only the metadata) */
      final IAxis[] axes = observation.getAxisList();
      final String href = observation.getHref();
      final String name = observation.getName();
      final MetadataList metadataList = observation.getMetadataList();

      final SimpleObservation simple = new SimpleObservation( href, name, metadataList, axes );
      ZmlFactory.writeToStream( simple, outputStream, null );

      return outputStream.toByteArray();

    }
    finally
    {
      IOUtils.closeQuietly( outputStream );
    }
  }

  public static byte[] flushToByteArray( final IObservation observation ) throws SensorException
  {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      ZmlFactory.writeToStream( observation, outputStream, null );

      return outputStream.toByteArray();
    }
    finally
    {
      IOUtils.closeQuietly( outputStream );
    }
  }

  public static ObservationRequest getDefaultRequest( )
  {
    final Calendar from = Calendar.getInstance();
    final Calendar to = Calendar.getInstance();

    from.add( Calendar.DAY_OF_MONTH, HISTORY );
    to.add( Calendar.DAY_OF_MONTH, 1 );

    return new ObservationRequest( from.getTime(), to.getTime() );
  }

  public static SimpleObservation getSimpleObservation( final SimpleTuppleModel model, final IObservation baseObservation )
  {
    final String href = baseObservation.getHref();
    final String name = baseObservation.getName();
    final MetadataList metadataList = baseObservation.getMetadataList();

    return new SimpleObservation( href, name, metadataList, model );
  }

  public static IObservation parseFromByteArray( final byte[] buffer ) throws SensorException
  {
    final ByteArrayInputStream stream = new ByteArrayInputStream( buffer );
    try
    {
      final InputSource source = new InputSource( stream );
      return ZmlFactory.parseXML( source, null );
    }
    finally
    {
      IOUtils.closeQuietly( stream );
    }
  }

  public static IObservation clone( final IObservation observation ) throws SensorException
  {
    final byte[] byteArray = ObservationHelper.flushToByteArray( observation );
    return ObservationHelper.parseFromByteArray( byteArray );
  }
}
