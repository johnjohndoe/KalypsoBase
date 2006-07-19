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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.layer.impl.GridLayer;
import org.kalypso.swtchart.layer.impl.TestDateLayer;
import org.kalypso.swtchart.layer.impl.TestLayer;
import org.kalypso.swtchart.layer.impl.TestStackLayer;
import org.kalypso.swtchart.styles.LayerStyle;
import org.kalypso.swtchart.styles.StyledLine;
import org.kalypso.swtchart.styles.StyledPoint;
import org.kalypso.swtchart.styles.StyledPolygon;
import org.kalypso.swtchart.styles.IStyleConstants.SE_LINESTYLE;
import org.kalypso.swtchart.util.AliComparator;


/**
 * @author schlienger
 */
public class TestApp implements KeyListener
{
  Display m_display;

  Chart m_chart;

  public static void main( final String[] args )
  {
    TestApp ta = new TestApp();
    ta.showConfigChart();
   
  }
  
  
  public void showConfigChart()
  {
    m_display = new Display();
    Shell shell = new Shell( m_display );

    shell.setText( "Hauptfenster" );
    shell.setSize( 800, 600 );

    shell.setLayout( new FillLayout() );
    shell.addKeyListener( this );

    m_chart = new Chart( shell, SWT.NONE );

    shell.open();
    
    
    
    ChartLoader cl=new ChartLoader(m_chart, "/home/alibu/dev/kalypso_workspace312/KalypsoChart/etc/binding/examples/Configuration.xml");
    cl.createChart("Test");
    
    //Es sollen Start- und Entwerte aus der Konfiguration verwendet werden
    m_chart.setAutoscale( false );
    //Da ist Benutzerintelligenz gefragt - wenn eine Achse nicht angezeigt werden soll, sollte man
    //sie nicht definieren
    m_chart.setHideUnusedAxes( false );

    while( !shell.isDisposed() )
    {
      if( !m_display.readAndDispatch() )
        m_display.sleep();
    }

    m_display.dispose();
  }
  
  
  public void showChart( )
  {
    m_display = new Display();
    Shell shell = new Shell( m_display );

    shell.setText( "Hauptfenster" );
    shell.setSize( 800, 600 );

    shell.setLayout( new FillLayout() );
    shell.addKeyListener( this );

    m_chart = new Chart( shell, SWT.NONE );

    shell.open();
    
   
    
    
    
    

    final IAxis<Number> al = new NumberAxis( "al", "Left Axis", PROPERTY.CONTINUOUS, POSITION.LEFT, DIRECTION.NEGATIVE, new DoubleComparator( 0.001 ) );
    al.setFrom( 0 );
    al.setTo( 100 );
    final IAxis<Date> al2 = new DateAxis( "al2", "Left Axis 2 (Date)", PROPERTY.CONTINUOUS, POSITION.LEFT, new AliComparator(), DIRECTION.POSITIVE );
    // Start: 100 Tage vor jetzt
    al2.setFrom( new Date( System.currentTimeMillis() - 100 * 1000 * 60 * 60 * 24 ) );
    // Ende: Jetzt
    al2.setTo( new Date( System.currentTimeMillis() ) );

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

    final Color colorFg = m_display.getSystemColor( SWT.COLOR_DARK_GRAY );
    final Color colorBg = m_display.getSystemColor( SWT.COLOR_WHITE );

    // NumberAxisRenderer in der Registry bekannt machen
    final IAxisRenderer r1 = new NumberAxisRenderer( colorFg, colorBg, 1, 5, new Insets( 1, 1, 1, 1 ), 1, new Insets( 1, 1, 1, 1 ), 5 );
    m_chart.getAxisRegistry().setRenderer( Number.class, r1 );

    // DateAxisRenderer in der Registry bekannt machen
    final IAxisRenderer r2 = new DateAxisRenderer( colorFg, colorBg, 1, 5, new Insets( 1, 1, 1, 1 ), 1, new Insets( 1, 1, 1, 1 ), 5, new SimpleDateFormat("yyyy-MM-dd") );
    m_chart.getAxisRegistry().setRenderer( Date.class, r2 );

    m_chart.getAxisRegistry().addAxis( al );
    m_chart.getAxisRegistry().addAxis( al2 );
    m_chart.getAxisRegistry().addAxis( ab );
    m_chart.getAxisRegistry().addAxis( ab2 );
    m_chart.getAxisRegistry().addAxis( at );
    m_chart.getAxisRegistry().addAxis( at2 );
    m_chart.getAxisRegistry().addAxis( ar );
    m_chart.getAxisRegistry().addAxis( ar2 );

    TestStackLayer tsl1 = new TestStackLayer( al, ab );
    LayerStyle tsl1_style = new LayerStyle();
    tsl1_style.add( new StyledPolygon( new RGB( 255, 125, 255 ), 1, new RGB( 100, 100, 100 ), 255 ) );
    tsl1.setStyle( tsl1_style );

    TestStackLayer tsl2 = new TestStackLayer( ab2, ar );
    LayerStyle tsl2_style = new LayerStyle();
    tsl2_style.add( new StyledPolygon( new RGB( 0, 255, 0 ), 1, new RGB( 0, 0, 0 ), 255 ) );
    tsl2.setStyle( tsl2_style );

    TestLayer tl1 = new TestLayer( at2, al );
    LayerStyle tl1_style = new LayerStyle();
    tl1_style.add( new StyledPoint( 3, 3, new RGB(255, 0, 0 ), 1, new RGB(0, 0, 0 ) , 255 ) );
    tl1_style.add( new StyledPoint( 5, 5, new RGB(255, 255, 0 ), 1, new RGB( 0, 0, 0 ), 255 ) );
    tl1_style.add( new StyledLine( 2, new RGB( 0, 0, 255 ), SE_LINESTYLE.DOT, 255 ) );
    tl1.setStyle( tl1_style );

    GridLayer gl1 = new GridLayer( al2, at );
    LayerStyle gl1_style = new LayerStyle();
    gl1.setStyle( gl1_style );

    TestDateLayer tdl1 = new TestDateLayer( al2, at );
    LayerStyle tdl1_style = new LayerStyle();
    tdl1.setStyle( tdl1_style );

  //  m_chart.addLayer( gl1 );
    m_chart.addLayer( tsl1 );
    m_chart.addLayer( tl1 );
    m_chart.addLayer( tsl2 );
    m_chart.addLayer( tdl1 );
    // m_chart.addLayer( tom1 );

    m_chart.setAutoscale( true );
    m_chart.setHideUnusedAxes( false );

    while( !shell.isDisposed() )
    {
      if( !m_display.readAndDispatch() )
        m_display.sleep();
    }

    m_display.dispose();
  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
   */
  public void keyPressed( KeyEvent e )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
   */
  public void keyReleased( KeyEvent e )
  {
    // TODO Auto-generated method stub
    char c = e.character;
    if( c == 'r' )
    {
      m_display.dispose();
      showChart();
      System.out.println( "Reset" );
    }
    else if( c == 'l' )
    {
      m_chart.redraw();
      m_chart.layout();
      System.out.println( "Layout" );
    }

  }
}
