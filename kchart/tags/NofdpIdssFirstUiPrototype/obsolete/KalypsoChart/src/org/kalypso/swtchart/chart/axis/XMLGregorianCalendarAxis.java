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
package org.kalypso.swtchart.chart.axis;

import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.graphics.Point;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author schlienger
 * @author burtscher
 * 
 * Concrete IAxis implementation - to be used for calendar data from XML documents
 * 
 */
public class XMLGregorianCalendarAxis extends AbstractAxis<XMLGregorianCalendar>
{
  public XMLGregorianCalendarAxis( String id, String label, PROPERTY prop, POSITION pos, DIRECTION dir )
  {
    super( id, label, prop, pos, dir, XMLGregorianCalendar.class );
  }

  public XMLGregorianCalendarAxis( String id, String label, PROPERTY prop, POSITION pos, Comparator<XMLGregorianCalendar> comp, DIRECTION dir )
  {
    super( id, label, prop, pos, dir, comp, XMLGregorianCalendar.class );
  }

  public double logicalToNormalized( final XMLGregorianCalendar value )
  {
    XMLGregorianCalendar xgcTo = getTo();
    GregorianCalendar gcTo = xgcTo.toGregorianCalendar();
    long toMillis = gcTo.getTimeInMillis();

    XMLGregorianCalendar xgcFrom = getFrom();
    GregorianCalendar gcFrom = xgcFrom.toGregorianCalendar();
    long fromMillis = gcFrom.getTimeInMillis();

    long valueMillis = value.toGregorianCalendar().getTimeInMillis();

    // r should not be 0 here (see AbstractAxis)
    final long r = toMillis - fromMillis;

    // Die rechte Seite muss unbedingt nach double gecastet werden, da sonst auf die Werte 0 oder 1 gerundet wird.
    final double norm = ((double) (valueMillis - fromMillis)) / r;
    // System.out.println("Normalization: "+DateFormat.getInstance().format(value)+" => "+norm+"; from:
    return norm;
  }

  public XMLGregorianCalendar normalizedToLogical( final double value )
  {
    final double r = getTo().toGregorianCalendar().getTimeInMillis() - getFrom().toGregorianCalendar().getTimeInMillis();

    final long logical = (long) (value * r + getFrom().toGregorianCalendar().getTimeInMillis());

    GregorianCalendar c = new GregorianCalendar();
    c.setTime( new Date( logical ) );

    return new XMLGregorianCalendarImpl( c );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#logicalToScreen(T)
   */
  public int logicalToScreen( final XMLGregorianCalendar value )
  {
    if( m_registry == null )
      return 0;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return 0;

    double norm = logicalToNormalized( value );
    int screen = comp.normalizedToScreen( norm );
    return screen;
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#screenToLogical(int)
   */
  public XMLGregorianCalendar screenToLogical( final int value )
  {
    if( m_registry == null )
      return null;

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null;

    return normalizedToLogical( comp.screenToNormalized( value ) );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#logicalToScreenInterval(T, T, double)
   */
  public Point logicalToScreenInterval( XMLGregorianCalendar value, XMLGregorianCalendar fixedPoint, double intervalSize )
  {
    // intervalSize wird als Millisecond-Angabe interpretiert
    long intervalMillis = (long) intervalSize;
    long fixedMillis = fixedPoint.toGregorianCalendar().getTimeInMillis();
    long valueMillis = value.toGregorianCalendar().getTimeInMillis();
    /*
     * der Increment-Wert f¸r die Schleife richtet sich nach der Richtung, in der der Wert vom fixedPoint
     *  gesehen liegt  
     */ 
    long start = fixedMillis;
    long end = fixedMillis;
    System.out.println( "Axis (" + getIdentifier() + "): starting IntervalCalculation" );
    if( start >= valueMillis )
    {
      while( start > valueMillis )
        start -= intervalMillis;
      end = start + intervalMillis;
    }
    else
    {
      while( end < valueMillis )
        end += intervalMillis;
      start = end - intervalMillis;
    }
    System.out.println( "Axis (" + getIdentifier() + "): IntervalCalculation finished" );

    // Start-Zeitpunkt des Wert-Intervalls
    GregorianCalendar startCal = new GregorianCalendar();
    startCal.setTimeInMillis( start );
    XMLGregorianCalendar startXMLCal = new XMLGregorianCalendarImpl( startCal );

    // End-Zeitpunkt des Wert-Intervalls
    GregorianCalendar endCal = new GregorianCalendar();
    endCal.setTimeInMillis( end );
    XMLGregorianCalendar endXMLCal = new XMLGregorianCalendarImpl( endCal );

    final IAxisComponent comp = m_registry.getComponent( this );
    if( comp == null )
      return null;

    int startPoint = comp.normalizedToScreen( logicalToNormalized( startXMLCal ) );
    int endPoint = comp.normalizedToScreen( logicalToNormalized( endXMLCal ) );
    System.out.println( "Axis (" + getIdentifier() + "): Using Interval: " + startPoint + ":" + endPoint );
    return new Point( startPoint, endPoint );
  }

}
