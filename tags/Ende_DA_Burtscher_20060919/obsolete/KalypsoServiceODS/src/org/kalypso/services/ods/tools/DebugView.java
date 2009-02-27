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
package org.kalypso.services.ods.tools;


import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.services.ods.operation.CapabilitiesLoader;
import org.kalypso.services.ods.operation.ODSException;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.services.ods.preferences.IODSPreferences;
import org.kalypso.services.ods.preferences.ODSPreferences;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.ChartImageContainer;
import org.kalypso.swtchart.chart.Plot;
import org.kalypso.swtchart.chart.legend.LegendImageContainer;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.swtchart.configuration.TableLoader;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.kalypso.swtchart.exception.ConfigTableNotFoundException;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author alibu
 *
 */
public class DebugView extends ViewPart implements PaintListener
{
  
  Composite m_composite;
  private Canvas m_canvas;
//Pfad zu CapabilitiesDatei
  String m_pathCaps;
  //Pfad zur Config-Datei
  String m_pathVC;
  Image m_chartImage;
  Image m_legendImage;

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( Composite parent )
  {
    
    m_canvas = new Canvas( parent, SWT.FILL );
    m_canvas.setLayout( new FillLayout() );
    m_canvas.setBackground( parent.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
    m_canvas.addPaintListener( this );
    
    IEclipsePreferences preferences = new ODSPreferences().getPreferences();
    m_pathVC=preferences.get(IODSPreferences.CONFIG_PATH, "")+preferences.get(IODSPreferences.CONFIG_NAME, "");
    m_pathCaps=preferences.get(IODSPreferences.CAPABILITIES_PATH, "")+preferences.get(IODSPreferences.CAPABILITIES_NAME, "");
    
    int width=300;
    int height=200;
    m_chartImage=new Image(Display.getDefault(), width, height);  
    m_legendImage=new Image(Display.getDefault(), 200, 150);  
   // createChart("Test");
    getCapabilities();
    
  }
  
  /**
   * Capabilities auslesen und ausgeben
   */
  public void getCapabilities()
  {
    CapabilitiesLoader pf = new CapabilitiesLoader(m_pathCaps, m_pathVC);
    pf.mergeConfigIntoODS();
    String capsxml = pf.getXMLString();
    System.out.println(capsxml);
  }
  
  
  
  /**
   * Testmethode zur Ausgabe einer Tabelle aus einem 
   */
  public void getHTMLTable() throws ConfigTableNotFoundException
  {
    //HTMLTabelle erzeugen
    ConfigurationLoader cl=null;
    try
    {
      cl = new ConfigurationLoader(m_pathVC);
    }
    catch( JAXBException e )
    {
      e.printStackTrace();
    }
    
    if (cl!=null)
    {
      String htmltable = TableLoader.createTable(cl.getConfiguration(), "MyFirstTable");
      System.out.println(htmltable);
      //  und noch eine HTMLTabelle erzeugen
      String htmltable2 = TableLoader.createTable(cl.getConfiguration(), "MySecondTable");
      System.out.println(htmltable2);
    }
  }

  
  /**
   * Chart erzeugen
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void createChart (String chartname) {
    
    Display d= Display.getDefault();
    //Display für Shell kann null sein :-)
    String confpath="/home/alibu/dev/kalypso_workspace312/KalypsoChart/etc/binding/examples/Configuration.xml";
    ConfigurationLoader cl=null;
    try
    {
      cl = new ConfigurationLoader(confpath);
    }
    catch( JAXBException e )
    {
      e.printStackTrace();
    }
    if (cl!=null)
    {
      ConfigurationType config = cl.getConfiguration();
      
      ChartImageContainer cic=null;
      LegendImageContainer lic=null;
      try
      {
        cic = new ChartImageContainer(config, "Test", d, 500,100);
        lic=new LegendImageContainer(config, "Test", d);
      }
      catch( ConfigChartNotFoundException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      if (cic!=null && lic!=null)
      {
        GC gcchart=new GC(m_chartImage);
        GC gcleg=new GC(m_legendImage);
        Image cicImage=cic.getImage();
        Image licImage=lic.getImage();
        if (cicImage!=null)
          gcchart.drawImage( cicImage, 0, 0 );
        if (licImage!=null)
          gcleg.drawImage( licImage, 0, 0 );
        
        gcleg.dispose();
        gcchart.dispose();
        lic.dispose();
        cic.dispose();
      }
      
    }
    
  }
  
  
  
  
  
  
  
  @Override
  public void dispose()
  {
    if (m_composite!=null)
    {
      m_composite.dispose();
      m_composite=null;
    }
    if (m_canvas!=null)
    {
      m_canvas.dispose();
      m_canvas=null;
    }
    if (m_chartImage!=null)
    {
      m_chartImage.dispose();
      m_chartImage=null;
    }
    super.dispose();
  }
  
  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl( PaintEvent e )
  {
    GC gc=e.gc;
    gc.setBackground( e.display.getSystemColor( SWT.COLOR_WHITE ) );
    gc.setForeground( e.display.getSystemColor( SWT.COLOR_BLACK ) );
    gc.drawString("Ich bin nur zum Testen da.", 100, 100);
    
    if (m_chartImage!=null)
    {
      gc.drawImage(m_chartImage, 0, 0);
    }
    if (m_legendImage!=null)
    {
      gc.drawImage(m_legendImage, 0, 0);
    }
    
  }

  

}