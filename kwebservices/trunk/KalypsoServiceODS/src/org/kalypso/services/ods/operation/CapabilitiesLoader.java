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
package org.kalypso.services.ods.operation;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.services.ods.preferences.IODSPreferences;
import org.kalypso.services.ods.preferences.ODSPreferences;
import org.ksp.chart.tableconfiguration.TableType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.ods.capabilities.DCPType;
import org.ksp.ods.capabilities.DataURL;
import org.ksp.ods.capabilities.Diagram;
import org.ksp.ods.capabilities.Get;
import org.ksp.ods.capabilities.HTTP;
import org.ksp.ods.capabilities.Legend;
import org.ksp.ods.capabilities.ODSCapabilities;
import org.ksp.ods.capabilities.OnlineResource;
import org.ksp.ods.capabilities.OperationType;
import org.ksp.ods.capabilities.Request;
import org.ksp.ods.capabilities.Table;


/**
 * @author alibu
 *
 */
public class CapabilitiesLoader
{
  public final static org.ksp.ods.capabilities.ObjectFactory m_ofCap = new org.ksp.ods.capabilities.ObjectFactory();
  public final static org.ksp.chart.viewerconfiguration.ObjectFactory m_ofVC = new org.ksp.chart.viewerconfiguration.ObjectFactory();
  private static final JAXBContext JCCaps = JaxbUtilities.createQuiet( org.ksp.ods.capabilities.ObjectFactory.class );
  private static final JAXBContext JCVC = JaxbUtilities.createQuiet( org.ksp.chart.viewerconfiguration.ObjectFactory.class );
  
  private String m_configPathCaps;
  private String m_configPathVC;
  private ODSCapabilities m_caps=null;
  private ConfigurationType m_vc=null;
  

  public CapabilitiesLoader(String pathCaps, String pathVC)
  {
      m_configPathCaps=pathCaps;
      m_configPathVC=pathVC;
      loadFile();
      mergeConfigIntoODS();
  }
  
  public void loadFile()
  {
    System.out.println("loadFile Start");
      try
      {
        //Capabilities laden
        
        Object o = JCCaps.createUnmarshaller().unmarshal( new File(m_configPathCaps) );
        if (o instanceof JAXBElement)
        {
          Object child=((JAXBElement) o).getValue();
          m_caps=(ODSCapabilities) child;
        }
        else if (o instanceof ODSCapabilities)
        {
          m_caps=(ODSCapabilities)  o;
        }
        else
          System.out.println("Klasse von Objekt: "+o.getClass().getName());
        System.out.println("CapabilityDokument geladen");
        
        //      ViewerConf
        Object o2 = JCVC.createUnmarshaller().unmarshal( new File(m_configPathVC) );
        if (o2 instanceof JAXBElement)
        {
          Object child=((JAXBElement) o2).getValue();
          m_vc=(ConfigurationType) child;
        }
        else if (o2 instanceof ConfigurationType)
        {
          m_vc=(ConfigurationType)  o2;
        }
        else
          System.out.println("Klasse von Objekt: "+o2.getClass().getName());
        System.out.println("ConfDokument geladen");
        
      }
      catch( JAXBException e )
      {
        System.out.println("Exception");
        e.printStackTrace();
      }
      catch (ClassCastException e)
      {
        e.printStackTrace();
      }
      System.out.println("loadFile End");
      if (m_vc!=null)
        System.out.println("ViewerConf != null");
      else
        System.out.println("ViewerConf == null");
  }
  
  public String getXMLString()
  {
    String capsString="";
    try
    {
      StringWriter sw=new StringWriter();
      Marshaller m= JCCaps.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

      m.marshal(m_caps, sw);
      capsString=sw.toString();
      sw.close();
    }
    catch( JAXBException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return capsString;
  }
  
  /**
   * Fügt die über die ViewerConfiguration definierten Diagramme und Tabellen in das GetCapabilities-Dokument ein
   */
  public void mergeConfigIntoODS()
  {
    
    
    IEclipsePreferences prefs = new ODSPreferences().getPreferences();
    String serviceRoot=prefs.get( IODSPreferences.SERVICE_ROOT, "" );
    String service="ODS";
    
    //URLs für Request erzeugen
    Request request = m_caps.getCapability().getRequest();
    //getCapabilities
    OperationType getCapabilities = request.getGetCapabilities();
    getCapabilities.getDCPType().clear();
    getCapabilities.getDCPType().add( createDCPType( serviceRoot, service, "GetCapabilities" ) );
    
    //getLegend
    OperationType getLegend = request.getGetLegend();
    getLegend.getDCPType().clear();
    getLegend.getDCPType().add( createDCPType( serviceRoot, service, "GetLegend" ) );
    
    //getDiagram
    OperationType getDiagram = request.getGetDiagram();
    getDiagram.getDCPType().clear();
    getDiagram.getDCPType().add( createDCPType( serviceRoot, service, "GetDiagram" ) );

    //getHTMLTable
    OperationType getHTMLTable = request.getGetHTMLTable();
    getHTMLTable.getDCPType().clear();
    getHTMLTable.getDCPType().add( createDCPType( serviceRoot, service, "GetHTMLTable" ) );
    

    
    
    //Diagramme anhängen
    List<ChartType> charts = m_vc.getChart();
    List<Diagram> diagrams = new LinkedList<Diagram>();
    if (charts!=null)
    {
      for( ChartType chart : charts )
      {
        String name= chart.getName();
        String title = chart.getTitle();
        String cabstract = chart.getAbstract();
        
//      URL und Format erzeugen 
        String operation="GetDiagram";
        DataURL dataURL = m_ofCap.createDataURL();
        dataURL.setFormat( "image/jpeg" );
        dataURL.setOnlineResource( createOnlineResource( serviceRoot, service, operation, name ) );
        
        Diagram diagram = m_ofCap.createDiagram();
        diagram.getDataURL().add( dataURL );
        diagram.setName(name);
        diagram.setTitle(title);
        diagram.setAbstract(cabstract);
        diagram.setQueryable( true );
        diagrams.add(diagram);
      }
    }
    if (diagrams.size()>0)
    {
      List<Diagram> diagram = m_caps.getCapability().getDiagram();
      //die anderen löschen
      diagram.clear();
      diagram.addAll(diagrams);
    }
    
    //Legenden anhängen - die werden auch aus den ConfigCharts generiert, weil zu jedem Chart auch eine Legend gehört 
    List<Legend> legends = new LinkedList<Legend>();
    if (charts!=null)
    {
      for( ChartType chart : charts )
      {
        String name= chart.getName();
        String title = chart.getTitle();
        String cabstract = chart.getAbstract();
        
//      URL und Format erzeugen 
        String operation="GetLegend";
        DataURL dataURL = m_ofCap.createDataURL();
        dataURL.setFormat( "image/jpeg" );
        dataURL.setOnlineResource( createOnlineResource( serviceRoot, service, operation, name ) );
        
        Legend legend = m_ofCap.createLegend();
        legend.getDataURL().add( dataURL );
        legend.setName(name);
        legend.setTitle(title);
        legend.setAbstract(cabstract);
        legend.setQueryable( true );
        legends.add(legend);
      }
    }
    if (legends.size()>0)
    {
      List<Legend> legend = m_caps.getCapability().getLegend();
      //alle anderen verwerfen
      legend.clear();
      legend.addAll(legends);
    }
    

    //Tabellen anhängen
    List<TableType> vctables = m_vc.getTable();
    List<Table> captables = new LinkedList<Table>();
    if (vctables!=null)
    {
      for( TableType table : vctables )
      {
        String name= table.getName();
        String title = table.getTitle();
        String tabstract = table.getAbstract();
        
        Table captable = m_ofCap.createTable();
        captable.setName(name);
        captable.setTitle(title);
        captable.setAbstract(tabstract);
        captable.setQueryable( true );

        //URL und Format erzeugen 
        String operation="GetHTMLTable";
        DataURL dataURL = m_ofCap.createDataURL();
        dataURL.setFormat( "text/html" );
        dataURL.setOnlineResource( createOnlineResource( serviceRoot, service, operation, name ) );
        captable.getDataURL().add( dataURL );
        
        captables.add(captable);
        
      }
    }
    if (captables.size()>0)
    {
      List<Table> tables = m_caps.getCapability().getTable();
      //alle anderen Löschen
      tables.clear();
      tables.addAll(captables);
    }
    
    
  }
  
  /**
   * Erzeugt einen Link auf einen Service als OnlineResource 
   * @param serviceRoot URL zur Webservice-Klasse
   * @param service Name des Services, z.B. "ODS"
   * @param operation Auszuführende Operation (REQUEST)
   * @param name Name eines auswählbaren Elements - wenn nicht zutreffend, kann uach null übergeben werden 
   */
  private OnlineResource createOnlineResource(String serviceRoot, String service, String operation, String name)
  {
    OnlineResource resource = m_ofCap.createOnlineResource();
    String url=serviceRoot+"?SERVICE="+service+"&REQUEST="+operation;
    if (name!=null)
      url+="&NAME="+name;
    resource.setHref( url );
    return resource;
  }
  
  public DCPType createDCPType(String serviceRoot, String service, String operation)
  {
    DCPType dcpCaps=m_ofCap.createDCPType();
    HTTP http=m_ofCap.createHTTP();
    Get get=m_ofCap.createGet(  );
    
    get.setOnlineResource( createOnlineResource( serviceRoot, service, operation, null )  );
    http.setGet( get );
    dcpCaps.setHTTP( http );
    return dcpCaps;
  }

}


