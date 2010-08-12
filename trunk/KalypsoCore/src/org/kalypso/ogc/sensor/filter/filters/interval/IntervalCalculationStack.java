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

import java.util.Arrays;
import java.util.Calendar;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.status.KalypsoStati;

/**
 * @author Dirk Kuch
 */
public class IntervalCalculationStack
{
  protected enum PROCESSING_INSTRUCTION
  {
    eNothing,
    eGoToNextTarget,
    eGoToNextSource,
    eFinished;

    public boolean isGoToNextSource( )
    {
      return PROCESSING_INSTRUCTION.eGoToNextSource.equals( valueOf( name() ) );
    }

    public boolean isGoToNextTarget( )
    {
      return PROCESSING_INSTRUCTION.eGoToNextTarget.equals( valueOf( name() ) );
    }

    public boolean isFinished( )
    {
      return PROCESSING_INSTRUCTION.eFinished.equals( valueOf( name() ) );
    }
  }

  public IntervalCalculationStack( final IAxis[] valueAxes, final IAxis[] statusAxes, final double defaultValue, final int defaultStatus )
  {
    defaultValues = new double[valueAxes.length];
    defaultStatis = new int[statusAxes.length];

    Arrays.fill( defaultValues, defaultValue );
    Arrays.fill( defaultStatis, defaultStatus );
  }

  public Calendar lastTargetCalendar = null;

  public Calendar lastSrcCalendar = null;

  public Interval targetInterval = null;

  public int targetRow = 0;

  public Interval srcInterval = null;

  public int srcRow = 0;

  public final double[] defaultValues;

  public final int[] defaultStatis;

  public int[] getPlainStatis( )
  {
    final int[] statis = new int[defaultStatis.length];
    Arrays.fill( statis, KalypsoStati.BIT_OK );

    return statis;
  }

  public double[] getPlainValues( )
  {
    final double[] values = new double[defaultValues.length];
    Arrays.fill( values, 0d );

    return values;
  }
}
