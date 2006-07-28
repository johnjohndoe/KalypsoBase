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
package org.kalypso.swtchart.configuration;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.KalypsoChartExtensions;
import org.kalypso.swtchart.chart.Plot;
import org.kalypso.swtchart.chart.axis.AxisFactory;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.component.AxisComponent;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.chart.layer.impl.DoubleDataLayerProvider;
import org.ksp.chart.viewerconfiguration.AbstractAxisType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.LayerProviderType;


/**
 * @author alibu
 *
 */
public class ChartLoader
{
  
  /**
   * Erstellt ein Chart aus der Konfiguration  
   * @param chart Das Chart-Objekt, das mit Leben gefüllt wird
   * @param configChartName Der Name der ChartConfiguration, die verwendet werden soll
   */
  public static Chart createChart(Chart chart, ConfigurationType config, String configChartName)
  {
    ChartType configChart=null;
    
    //ChartConfig auslesen
    if (config!=null)
    {
      List<ChartType> ccharts = config.getChart();
      for( ChartType c : ccharts )
      {
          if (c.getName().compareTo(configChartName)==0)
          {
            configChart=c;
            break;
          }
      }
    }
    

    
    
    if (configChart!=null)
    {
        //Achsen hinzufügen
      
        List<JAXBElement< ? extends AbstractAxisType>> jaxbAxes = configChart.getAxis();
        for( JAXBElement< ? extends AbstractAxisType> element : jaxbAxes )
        {
            AbstractAxisType abstractAxis=element.getValue();
            AxisFactory.addConfigAxis(chart, abstractAxis);
        }
        
        
        //Layer hinzufügen
        List<LayerProviderType> lps = configChart.getLayerProvider();
        for( LayerProviderType lp : lps )
        {
            String lpname=lp.getName();
            /**
             * Das ist nur zum Testen
             */
            if (
                  lpname.compareTo("org.ksp.observation.layerProvider.Test.Line")==0
                  || lpname.compareTo("org.ksp.observation.layerProvider.Test.Bar")==0  
              )
            {
              DoubleDataLayerProvider ddlp=new DoubleDataLayerProvider();
              ddlp.init(chart, lp);
              IChartLayer[] icl=ddlp.getLayers();
              for( IChartLayer layer : icl )
              {
                System.out.println("Adding Layer "+layer.getName());
                chart.addLayer(layer);
              }
            }
            else
            {
              try
              {
                final ILayerProvider provider = KalypsoChartExtensions.createProvider( lpname );
                if (provider!=null)
                {
                  provider.init(chart, lp);
                  IChartLayer[] icl=provider.getLayers();
                  if (icl!=null)
                  {
                    for( IChartLayer layer : icl )
                    {
                      System.out.println("Adding Layer "+layer.getName());
                      chart.addLayer(layer);
                    }
                  }
                }
                else
                {
                  System.out.println("No LayerProvider for "+lpname);
                }
              }
              catch( CoreException e )
              {
                e.printStackTrace();
              }
            }
        }
    }
    return chart;
  }
  
  /**
   * Erzeugt das gewünschte Chart und schreibt es in ein Image
   * @param config Chart-Konfiguration (ge-un-marshallte Konfigurationsdatei)
   * @param chartname der Name des Charts in der Konfiguration
   * @param img Das Image, in das das Chart gezeichnet werden soll.
   */
    public static void createChartImage (ConfigurationType config, String chartname, Image img) {
    
    Display d= Display.getDefault();
    //Display für Shell kann null sein :-)
    Shell shell = new Shell( );
    shell.setLayout( new FillLayout() );
    //Display für Shell darf nicht null sein :-(
    Chart chart = new Chart( shell, SWT.NONE );
 
    ChartLoader.createChart(chart, config, chartname);
    chart.setSize(new Point(img.getBounds().width,img.getBounds().height));
    chart.layout();
    chart.update();
    
    GCWrapper gcw=new GCWrapper(new GC(img));
    Image tmpImg=new Image(d, img.getBounds().width, img.getBounds().height);
    GC tmpGc=new GC(tmpImg);
    GCWrapper tmpGcw=new GCWrapper(tmpGc);
    tmpGcw.setBackground(d.getSystemColor(SWT.COLOR_YELLOW));
    tmpGcw.fillRectangle(0,0,tmpImg.getBounds().width, tmpImg.getBounds().height);
    
    //img mit weißem Hintergrund versehen
    gcw.setBackground(d.getSystemColor(SWT.COLOR_WHITE));
    gcw.fillRectangle(0,0, img.getBounds().width, img.getBounds().height);
    
    
    //Plot zeichnen
    Plot plot=(Plot) chart.getPlot();
    tmpImg=plot.paintChart(d, tmpGcw, img.getBounds(), null);
    ImageLoader il=new ImageLoader();
    gcw.drawImage( tmpImg, 0, 0, plot.getBounds().width, plot.getBounds().height, plot.getBounds().x, plot.getBounds().y, plot.getBounds().width, plot.getBounds().height);
    tmpGcw.fillRectangle(0,0,tmpImg.getBounds().width, tmpImg.getBounds().height);
    
    
    IAxisRegistry ar = chart.getAxisRegistry();
    Map<IAxis, IAxisComponent> components = ar.getComponents();
    Set<Entry<IAxis, IAxisComponent>> acs = components.entrySet();
    for( Entry<IAxis, IAxisComponent> ac : acs )
    {
      IAxis axis = ac.getKey();
      //Casten, damit man auf die Component-Eigenschaften zugreifen kann
      AxisComponent comp = (AxisComponent) ac.getValue();
      IAxisRenderer rend=axis.getRenderer();
      
      //Wenn man den GC nicht neu erzeugt, werden die AChsen nicht gezeichnet, sondern nochmal das Chart
      tmpGc.dispose();
      tmpGcw.dispose();
      tmpGc=new GC(tmpImg);
      tmpGcw=new GCWrapper(tmpGc);
      //den Renderer in den TmpGC-Zeichnen lassen
      rend.paint(tmpGcw, d, axis, comp.getBounds());
      /*  ...und ins Endbild kopieren; dabei muss berücksichtigt werden, 
             dass die Components in 2 Ebenen Tiefe liegen - zur Position muss 
             noch die der Eltern addiert werden
      */
      gcw.drawImage( tmpImg, comp.getBounds().x, comp.getBounds().y , comp.getBounds().width, comp.getBounds().height, comp.getParent().getBounds().x+comp.getBounds().x, comp.getParent().getBounds().y+comp.getBounds().y, comp.getBounds().width, comp.getBounds().height);
    }
    
    chart.dispose();
    gcw.dispose();
    tmpGcw.dispose();
    shell.dispose();
    tmpImg.dispose();
    
  }


  
}

  