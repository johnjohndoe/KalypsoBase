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
package org.kalypso.swtchart.view;

import java.awt.Insets;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.kalypso.swtchart.Chart;
import org.kalypso.swtchart.axis.DateAxis;
import org.kalypso.swtchart.axis.IAxis;
import org.kalypso.swtchart.axis.NumberAxis;
import org.kalypso.swtchart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.axis.renderer.DateAxisRenderer;
import org.kalypso.swtchart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.axis.renderer.NumberAxisRenderer;
import org.kalypso.swtchart.layer.impl.GridLayer;
import org.kalypso.swtchart.layer.impl.TestDateLayer;
import org.kalypso.swtchart.layer.impl.TestLayer;
import org.kalypso.swtchart.layer.impl.TestStackLayer;
import org.kalypso.swtchart.util.DateComparator;

/**
 * @author schlienger
 */
public class TestApp
{
  public static void main( final String[] args )
  {
    Display display = new Display();
    Shell shell = new Shell( display );

    shell.setText( "Hauptfenster" );
    shell.setSize( 800, 600 );

    shell.setLayout( new FillLayout() );
    final Chart chart = new Chart( shell, SWT.NONE );

    shell.open();

    final IAxis<Number> al = new NumberAxis( "al", "Left Axis", PROPERTY.CONTINUOUS, POSITION.LEFT, DIRECTION.NEGATIVE, new DoubleComparator( 0.001 ) );
    al.setFrom( 0 );
    al.setTo( 100 );
    final IAxis<Date> al2 = new DateAxis( "al2", "Left Axis 2 (Date)", PROPERTY.CONTINUOUS, POSITION.LEFT, new DateComparator(),  DIRECTION.POSITIVE);
    //Start: 100 Tage vor jetzt
    al2.setFrom( new Date (System.currentTimeMillis()- 100*1000*60*60*24) );
    //Ende: Jetzt
    al2.setTo( new Date(System.currentTimeMillis()) );

    final IAxis<Number> ab = new NumberAxis( "ab", "Bottom Axis", PROPERTY.CONTINUOUS, POSITION.BOTTOM, DIRECTION.POSITIVE, new DoubleComparator( 0.001 ) );
    ab.setFrom( 0 );
    ab.setTo( 100 );
    final IAxis<Number> ab2 = new NumberAxis( "ab2", "Bottom Axis 2", PROPERTY.CONTINUOUS, POSITION.BOTTOM, DIRECTION.NEGATIVE, new DoubleComparator( 0.001 ) );
    ab2.setFrom( 0 );
    ab2.setTo( 100 );

    final IAxis<Number> ar = new NumberAxis( "ar", "Right Axis", PROPERTY.CONTINUOUS, POSITION.RIGHT, DIRECTION.POSITIVE, new DoubleComparator( 0.001 ) );
    ar.setFrom( 0 );
    ar.setTo( 100 );
    final IAxis<Number> ar2 = new NumberAxis( "ar2", "Right Axis 2", PROPERTY.CONTINUOUS, POSITION.RIGHT, DIRECTION.NEGATIVE, new DoubleComparator( 0.001 ) );
    ar2.setFrom( 0 );
    ar2.setTo( 100 );

    final IAxis<Number> at = new NumberAxis( "at", "Top Axis", PROPERTY.CONTINUOUS, POSITION.TOP, DIRECTION.POSITIVE, new DoubleComparator( 0.001 ) );
    at.setFrom( 0 );
    at.setTo( 100 );
    final IAxis<Number> at2 = new NumberAxis( "at2", "Top Axis 2", PROPERTY.CONTINUOUS, POSITION.TOP, DIRECTION.NEGATIVE, new DoubleComparator( 0.001 ) );
    at2.setFrom( 0 );
    at2.setTo( 100 );

    final Color colorFg = display.getSystemColor( SWT.COLOR_DARK_GRAY );
    final Color colorBg = display.getSystemColor( SWT.COLOR_WHITE );

    //NumberAxisRenderer in der Registry bekannt machen 
    final IAxisRenderer r1 = new NumberAxisRenderer( colorFg, colorBg, 1, 5, new Insets( 1, 1, 1, 1 ), 1, new Insets( 1, 1, 1, 1 ), 5 );
    chart.getAxisRegistry().setRenderer( Number.class, r1 );
  
    //DateAxisRenderer in der Registry bekannt machen 
    final IAxisRenderer r2 = new DateAxisRenderer( colorFg, colorBg, 1, 5, new Insets( 1, 1, 1, 1 ), 1, new Insets( 1, 1, 1, 1 ), 5 );
    chart.getAxisRegistry().setRenderer( Date.class, r2 );

    chart.getAxisRegistry().addAxis( al );
    chart.getAxisRegistry().addAxis( al2 );
    chart.getAxisRegistry().addAxis( ab );
    chart.getAxisRegistry().addAxis( ab2 );
    chart.getAxisRegistry().addAxis( at );
    chart.getAxisRegistry().addAxis( at2 );
    chart.getAxisRegistry().addAxis( ar );
    chart.getAxisRegistry().addAxis( ar2 );

    chart.addLayer( new TestStackLayer( al, ab ) );
    chart.addLayer( new TestLayer( at2, al ) );
    chart.addLayer( new TestStackLayer( ab2, ar ) );
    chart.addLayer( new GridLayer( al2, at ) );
    chart.addLayer( new GridLayer( al, at2 ) );
    chart.addLayer( new TestDateLayer( al2, at ) );

    chart.setAutoscale( true );
    chart.setHideUnusedAxes( true );
  
    while( !shell.isDisposed() )
    {
      if( !display.readAndDispatch() )
        display.sleep();
    }

    display.dispose();
  }
}
