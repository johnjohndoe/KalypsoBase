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
package org.kalypso.ogc.sensor.timeseries.interpolation.worker;

import java.util.Calendar;

import org.kalypso.commons.math.LinearEquation;
import org.kalypso.commons.math.LinearEquation.SameXValuesException;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * @author Dirk Kuch
 */
public final class LocalCalculationStackValues
{
  private LocalCalculationStackValues( )
  {
  }

  public static TupleModelDataSet getInterpolatedValue( final Calendar calendar, final LocalCalculationStack stack, final LocalCalculationStackValue value )
  {
    final IAxis axis = value.getAxis();
    final String axisType = axis.getType();
    if( ITimeseriesConstants.TYPE_WECHMANN_E.equals( axisType ) )
    {
      return getContinuedValue( value );
    }
    else if( ITimeseriesConstants.TYPE_WECHMANN_SCHALTER_V.equals( axisType ) )
    {
      return getContinuedValue( value );
    }

    return getLinearInterpolatedValue( calendar, stack, value );
  }

  private static TupleModelDataSet getContinuedValue( final LocalCalculationStackValue value )
  {
    return value.getValue1();
  }

  private static TupleModelDataSet getLinearInterpolatedValue( final Calendar calendar, final LocalCalculationStack stack, final LocalCalculationStackValue value )
  {
    final long ms = calendar.getTimeInMillis();

    final IAxis valueAxis = value.getAxis();

    final TupleModelDataSet dataSet1 = value.getValue1();
    final TupleModelDataSet dataSet2 = value.getValue2();

    final long linearStart = stack.getDate1().getTime();
    final long linearStop = stack.getDate2().getTime();

    // normal case: perform the interpolation
    try
    {
      // BUGFIX: do not interpolate, if we have the exact date
      if( linearStart == ms )
        return dataSet1;
      else if( linearStop == ms )
        return dataSet2;
      else
      {
        final Object value1 = dataSet1.getValue();
        final Object value2 = dataSet2.getValue();

        if( value1 instanceof Number && value2 instanceof Number )
        {
          final Number number1 = (Number) value1;
          final Number number2 = (Number) value2;

          final LinearEquation equation = new LinearEquation();
          equation.setPoints( linearStart, number1.doubleValue(), linearStop, number2.doubleValue() );

          return new TupleModelDataSet( valueAxis, equation.computeY( ms ), KalypsoStati.BIT_OK, IInterpolationFilter.DATA_SOURCE );
        }
        else
          return dataSet1;

      }
    }
    catch( final SameXValuesException e )
    {
      return dataSet1;
    }
  }
}
