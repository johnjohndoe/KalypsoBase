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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

/**
 * @author alibu
 * 
 */
public class GenericDateTickCalculator implements ITickCalculator
{

  /**
   * @see org.kalypso.chart.ext.test.axisrenderer.ITickCalculator#calcTicks(org.eclipse.swt.graphics.GC,
   *      org.kalypso.chart.framework.model.mapper.IAxis)
   */
  /**
   * Calculates the ticks shown for the given Axis
   */
  @Override
  public Number[] calcTicks( final GC gc, final IAxis axis, final Number minDisplayInterval, final Point ticklabelSize )
  {

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      minScreenInterval = 2 + ticklabelSize.x;
    }
    else
    {
      minScreenInterval = 2 + ticklabelSize.y;
    }

    // Mini- und maximalen ANZEIGBAREN Wert ermitteln anhand der Größe der Labels
    int screenMin, screenMax;
    final IDataRange<Number> range = axis.getNumericRange();

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.numericToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.x ));
      }
      else
      {

        screenMin = (int) (axis.numericToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.x ));
      }
    }
    else
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.numericToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      }
      else
      {
        screenMin = (int) (axis.numericToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.y ));
      }

    }

    // Ab jetzt wird nur noch mit long gerechnet

    // logischen mini- und maximalen Wert ermitteln
    final long logicalMin = axis.screenToNumeric( screenMin ).longValue();
    final long logicalMax = axis.screenToNumeric( screenMax ).longValue();

    // der minimale logische Abstand
    final long minLogInterval = Math.abs( axis.screenToNumeric( minScreenInterval ).longValue() - axis.screenToNumeric( 0 ).longValue() );
    // ein paar Größen
    final long secondInMillis = 1000;
    final long minuteInMillis = secondInMillis * 60;
    final long hourInMillis = minuteInMillis * 60;
    final long dayInMillis = hourInMillis * 24;

    // letzten Tagesbeginn VOR dem Startdatum
    final long normmin = ((logicalMin / dayInMillis) - 1) * dayInMillis;
    // erster Tagesbeginn NACH dem Startdatum
    final long normmax = ((logicalMax / dayInMillis) + 1) * dayInMillis;

    // Collection für Ticks
    final HashSet<Long> ticks = new HashSet<Long>();

    int count = 0;
    long oldi = 0;

    long goodInterval = dayInMillis;
    while( goodInterval < minLogInterval )
    {
      goodInterval += dayInMillis;
    }

    for( long i = normmin; i <= normmax; i += goodInterval )
    {
      if( count > 0 )
      {
        findBetweens( oldi, i, minLogInterval, ticks );
      }
      ticks.add( i );

      count++;
      oldi = i;
    }

    final LinkedList<Number> realticks = new LinkedList<Number>();
    for( final Long tick : ticks )
    {
      final long ticklv = tick.longValue();
      if( ticklv >= logicalMin && ticklv <= logicalMax )
      {
        realticks.add( tick );
      }
    }

    final Number[] numTicks = realticks.toArray( new Number[] {} );

    return numTicks;
  }

  /**
   * recursive function which divides an interval into a number of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval
   */
  private void findBetweens( final long from, final long to, final long minInterval, final HashSet<Long> ticks )
  {

    if( from == to || minInterval == 0 )
    {
      return;
    }

    /*
     * TODO: hier muss noch irgendwie überprüft werden, in welchem Bereich sich die DateRange befindet - anhand der
     * DateRange muss dann bestimmt werden, welche divisoren sinnvoll sind; Beispiel: Wenn Stunden geteilt werden, dann
     * sind die Divisoren 6,4,3,2 gut; bei Tagen eher 120, 90, 60, 30, etc.
     */
    final LinkedList<Integer> divisors = new LinkedList<Integer>();

    divisors.add( 60 );
    divisors.add( 24 );
    divisors.add( 18 );
    divisors.add( 12 );
    divisors.add( 6 );
    divisors.add( 2 );

    // Abbruchbedingung: Abstand muss größer 1 sein, sonst kommt immer der 2. Wert raus
    if( to - from > 1 )
    {
      for( final Integer divisor : divisors )
      {
        final long betweenrange = Math.abs( from - to ) / divisor.intValue();
        if( minInterval < betweenrange )
        {
          // vorher prüfen
          int count = 0;
          long oldi = 0;
          for( long i = from; i <= to; i += betweenrange )
          {
            ticks.add( new Long( i ) );
            if( count >= 1 )
            {
              findBetweens( oldi, i, minInterval, ticks );
            }
            // Tick setzen
            count++;
            oldi = i;
          }
          break;
        }
      }
    }

  }

}
