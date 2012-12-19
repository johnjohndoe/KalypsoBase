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

import java.util.Date;

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.IObservationVisitor;

/**
 * @author Dirk Kuch
 */
public final class Observations
{
  private Observations( )
  {
  }

  public static void accept( final IObservation observation, final IObservationVisitor visitor, final IRequest request ) throws SensorException
  {
    accept( observation, visitor, request, 1 );
  }

  public static void accept( final IObservation observation, final IObservationVisitor visitor, final IRequest request, final int direction ) throws SensorException
  {
    final ITupleModel model = observation.getValues( request );

    if( direction >= 0 )
    {
      for( int index = 0; index < model.size(); index++ )
      {
        try
        {
          doVisit( visitor, observation, model, index );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }
    else
    {
      for( int index = model.size() - 1; index >= 0; index-- )
      {
        try
        {
          doVisit( visitor, observation, model, index );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }

  }

  private static void doVisit( final IObservationVisitor visitor, final IObservation observation, final ITupleModel model, final int index ) throws CancelVisitorException, SensorException
  {
    visitor.visit( new ObservationValueContainer( model, index, observation ) );
  }

  public static DateRange findDateRange( final IObservation observation )
  {
    try
    {
      final ITupleModel model = observation.getValues( null );

      return findDateRange( model );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return new DateRange();
  }

  public static DateRange findDateRange( final ITupleModel model )
  {
    try
    {
      if( model.isEmpty() )
        return new DateRange();

      final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );

      final Date from = (Date)model.get( 0, dateAxis );
      final Date to = (Date)model.get( model.size() - 1, dateAxis );

      return new DateRange( from, to );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

    return new DateRange();
  }
}