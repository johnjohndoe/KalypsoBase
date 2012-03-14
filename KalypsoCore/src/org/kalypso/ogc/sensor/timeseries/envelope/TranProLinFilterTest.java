/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.timeseries.envelope;

import java.io.StringWriter;
import java.util.Date;

import junit.framework.TestCase;

import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.timeseries.TimeseriesTestUtils;

/**
 * @author schlienger
 */
public class TranProLinFilterTest extends TestCase
{
  public void testGetValues( ) throws SensorException
  {
    final IObservation obs = TimeseriesTestUtils.createTestTimeserie( new String[] { ITimeseriesConstants.TYPE_WATERLEVEL }, 10, false ); //$NON-NLS-1$

    final ITupleModel values = obs.getValues( null );
    assertEquals( 10, values.size() );

    System.out.println( ObservationUtilities.dump( values, "   " ) ); //$NON-NLS-1$

    final IAxis dateAxis = AxisUtils.findDateAxis( obs.getAxes() );

    final DateRange range = new DateRange( (Date) values.get( 0, dateAxis ), (Date) values.get( values.size() - 1, dateAxis ) );

    final TranProLinFilter filter = new TranProLinFilter( range, "*", 1, 1.15, ITimeseriesConstants.TYPE_WATERLEVEL ); //$NON-NLS-1$
    filter.initFilter( null, obs, null );

    assertEquals( 10, filter.getValues( null ).size() );

    Number valueOrg = (Number) values.get( 0, ObservationUtilities.findAxisByType( obs.getAxes(), ITimeseriesConstants.TYPE_WATERLEVEL ) ); //$NON-NLS-1$
    Number valueNew = (Number) filter.getValues( null ).get( 0, ObservationUtilities.findAxisByType( filter.getAxes(), ITimeseriesConstants.TYPE_WATERLEVEL ) ); //$NON-NLS-1$
    assertEquals( valueOrg.doubleValue(), valueNew.doubleValue(), 0.001 );

    valueOrg = (Number) values.get( 9, ObservationUtilities.findAxisByType( obs.getAxes(), ITimeseriesConstants.TYPE_WATERLEVEL ) ); //$NON-NLS-1$
    valueNew = (Number) filter.getValues( null ).get( 9, ObservationUtilities.findAxisByType( filter.getAxes(), ITimeseriesConstants.TYPE_WATERLEVEL ) ); //$NON-NLS-1$
    assertEquals( valueOrg.doubleValue() * 1.15, valueNew.doubleValue(), 0.001 );

    final StringWriter w1 = new StringWriter();
    ObservationUtilities.dump( values, "\t", w1 ); //$NON-NLS-1$

    final StringWriter w2 = new StringWriter();
    ObservationUtilities.dump( filter.getValues( null ), "\t", w2 ); //$NON-NLS-1$

    assertFalse( w1.toString().equals( w2.toString() ) );
  }
}
