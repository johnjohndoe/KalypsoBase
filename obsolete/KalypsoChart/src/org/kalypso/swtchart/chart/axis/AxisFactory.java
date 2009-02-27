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

import java.awt.Insets;
import java.text.SimpleDateFormat;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.chart.axis.renderer.NumberAxisRenderer;
import org.kalypso.swtchart.chart.axis.renderer.XMLGregorianCalendarAxisRenderer;
import org.kalypso.swtchart.chart.util.AliComparator;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.DateAxisType;
import org.ksp.chart.viewerconfiguration.DoubleAxisType;

/**
 * @author burtscher creates Axis stuff from configuration TODO: should be moved to the configuration package
 */
public class AxisFactory
{
  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  public static void addConfigAxis( Chart chart, AbstractAxisType a )
  {
    IAxisRegistry ar = chart.getAxisRegistry();

    // Richtung
    DIRECTION dir = DIRECTION.POSITIVE;
    String direction = a.getDirection().value();
    if( direction.compareTo( "NEGATIVE" ) == 0 )
      dir = DIRECTION.NEGATIVE;

    // Position
    POSITION pos = null;
    String position = a.getPosition().value();
    if( position.compareTo( "BOTTOM" ) == 0 )
      pos = POSITION.BOTTOM;
    else if( position.compareTo( "TOP" ) == 0 )
      pos = POSITION.TOP;
    else if( position.compareTo( "RIGHT" ) == 0 )
      pos = POSITION.RIGHT;
    else if( position.compareTo( "LEFT" ) == 0 )
      pos = POSITION.LEFT;

    /**
     * TODO: mit dem DoubleComparator muss ich mir noch was einfallen lassen: Wie soll die Genauigkeit festgelegt
     * werden? * in der Config * automatisch
     */

    /**
     * TODO: Insgesamt ist hier alles noch ein wenig hart reincodiert: Label insets, MaxDigits, etc.
     */

    FontData fdLabel = new FontData( "Arial", 11, SWT.BOLD );
    FontData fdTick = new FontData( "Arial", 10, SWT.NONE );
    Insets insetsTick = new Insets( 1, 1, 1, 1 );
    Insets insetsLabel = new Insets( 1, 1, 1, 1 );

    if( a instanceof DoubleAxisType )
    {
      DoubleAxisType da = (DoubleAxisType) a;
      IAxis<Number> axis = new NumberAxis( a.getId(), a.getName(), PROPERTY.CONTINUOUS, pos, dir, new DoubleComparator( 0.001 ) );
      axis.setFrom( da.getMinVal() );
      axis.setTo( da.getMaxVal() );
      ar.addAxis( axis );
      if( ar.getRenderer( axis ) == null )
      {
        IAxisRenderer<Number> numberAxisRenderer = new NumberAxisRenderer( chart.getFGColor(), chart.getBGColor(), 1, 5, insetsTick, 1, insetsLabel, 0, fdLabel, fdTick );
        ar.setRenderer( Number.class, numberAxisRenderer );
      }
    }
    else if( a instanceof DateAxisType )
    {
      DateAxisType da = (DateAxisType) a;
      AliComparator ac = new AliComparator();
      IAxis<XMLGregorianCalendar> axis = new XMLGregorianCalendarAxis( a.getId(), a.getName(), PROPERTY.CONTINUOUS, pos, ac, dir );
      axis.setFrom( da.getMinVal() );
      axis.setTo( da.getMaxVal() );
      ar.addAxis( axis );
      if( ar.getRenderer( axis ) == null )
      {
        IAxisRenderer<XMLGregorianCalendar> xgcAxisRenderer = new XMLGregorianCalendarAxisRenderer( chart.getFGColor(), chart.getBGColor(), 1, 5, insetsTick, 1, insetsLabel, 0, fdLabel, fdTick, new SimpleDateFormat( "dd.MM.yy" ) );
        ar.setRenderer( XMLGregorianCalendar.class, xgcAxisRenderer );
      }
      else
      {
        System.out.println( "AxisFactory: Unknown axis type: " + a.getClass().getName() );
      }
    }
  }
}
