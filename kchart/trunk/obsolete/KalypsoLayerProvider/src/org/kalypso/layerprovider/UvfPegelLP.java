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
package org.kalypso.layerprovider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.kalypso.observation.result.Component;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.chart.layer.ILayerProvider;
import org.kalypso.swtchart.configuration.parameters.ParameterHelper;
import org.ksp.chart.configuration.AxisType;
import org.ksp.chart.configuration.LayerType;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author burtscher
 * 
 * Layer Provider for gauge data from O&M data;
 * it's looking for a feature named "wasserstandsmessung", then tries to
 * transform it into an IObservation, creates a WasserstandLayer
 *  and uses the result components which go by the name "Datum" (domain data) and 
 *  "Wasserstand" (value data) as data input for the layer; the WasserstandLayer draws 
 *  its data as line chart
 * 
 *  The following configuration parameters are needed for the LayerProvider:
 *  dataSource:     URL or relative path leading to observation data
 *  @TODO: dataSource is an independent configuration tag right now - it should be
 *      moved into the parameter section as not every layer provider needs an url
 * 
 */
public class UvfPegelLP implements ILayerProvider
{
  private LayerType m_lt;

  private Chart m_chart;

  public void init( final Chart chart, final LayerType lpt )
  {
    m_lt = lpt;
    m_chart = chart;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( URL context )
  {
    IChartLayer icl = null;
    String configLayerId=m_lt.getName();
    
    ParameterHelper ph=new ParameterHelper();
    ph.addParameters( m_lt.getParameters(), configLayerId );
    
      try
      {
        
          String domainAxisId= ( (AxisType) m_lt.getAxes().getDomainAxisRef().getRef()).getName();
          String valueAxisId= ( (AxisType) m_lt.getAxes().getValueAxisRef().getRef()).getName();
          
          IAxis domAxis = m_chart.getAxisRegistry().getAxis( domainAxisId );
          IAxis valAxis = m_chart.getAxisRegistry().getAxis( valueAxisId);

          
          Component comp_date=new Component("date", "Datum", "das Datum", "", "frame_datum", new QName( "Datum" ), null);
          Component comp_pegel=new Component("pegel", "Pegel", "der Pegelstand", "cmNN", "frame_pegel",  new QName( "Wasserstand" ), null);
          TupleResult result = new TupleResult(new Component[] {comp_date, comp_pegel});
          
//        TODO: umschreiben, damit auch urls verwendet werden können
          String url=ph.getParameterValue( "url", configLayerId);
          FileReader fr=new FileReader(url);
          
          BufferedReader br=new BufferedReader(fr);
          String s="";
          int linecount=0;
          while ((s=br.readLine())!=null)
          {
              if (linecount>4)
              {
                IRecord record = result.createRecord();
                String[] cols=s.split( "  *");
                //YearString
                if (cols.length>=2)
                {
                  String ys=cols[0];
                  //Datum zerpflücken (Bsp: 0510190530)
                  //TODO: Auslagern in Toolbox-ähnliche Klasse
                  int year=2000+Integer.parseInt( ys.substring( 0, 2 )) ;
                  int month=Integer.parseInt( ys.substring( 2, 4 ))-1;
                  int date=Integer.parseInt( ys.substring( 4, 6 )) ;
                  int hour=Integer.parseInt( ys.substring( 6, 8 )) ;
                  int minute=Integer.parseInt( ys.substring( 8, 10 )) ;
                  Calendar cal=GregorianCalendar.getInstance();
                  cal.set( year, month, date, hour, minute );
                  //XMLGregorianCalendar xmlcal=new XMLGregorianCalendarImpl((GregorianCalendar) cal);
                  
                  record.setValue( comp_date, cal);
                  record.setValue( comp_pegel, Integer.parseInt( cols[1]));
                  result.add( record );
                }
              }
              linecount++;
          }
          
          
          
          
          
          
          

          final TreeMap<String, IComponent> map = new TreeMap<String, IComponent>();
          map.put( "domain", comp_date);
          map.put( "value", comp_pegel);
          



          icl = new WasserstandLayer( result, map.get( "domain" ), map.get( "value" ), domAxis, valAxis, m_lt.getParameters() );

          icl.setName( m_lt.getTitle() );

      }
      catch( FileNotFoundException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch( IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    return icl;
  }

}
