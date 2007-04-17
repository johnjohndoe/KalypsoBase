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
package org.kalypso.services.ods.helper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.swtchart.configuration.ConfigLoader;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.TableType;
import org.ksp.chart.configuration.ChartType.Layers.LayerRef;
import org.ksp.ods.capabilities.Capability;
import org.ksp.ods.capabilities.DCPType;
import org.ksp.ods.capabilities.DataURL;
import org.ksp.ods.capabilities.Diagram;
import org.ksp.ods.capabilities.Get;
import org.ksp.ods.capabilities.HTTP;
import org.ksp.ods.capabilities.LayerType;
import org.ksp.ods.capabilities.Layers;
import org.ksp.ods.capabilities.Legend;
import org.ksp.ods.capabilities.ODSCapabilities;
import org.ksp.ods.capabilities.OnlineResource;
import org.ksp.ods.capabilities.OperationType;
import org.ksp.ods.capabilities.Request;
import org.ksp.ods.capabilities.Service;
import org.ksp.ods.capabilities.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author burtscher
 *
 * Loads (marshalls) the capabilities configuration file
 * and supplements the information with infos from configuration file
 * in order to create a getCapabilities-XML-Document
 *
 */
public class CapabilitiesLoader
{
  public final static org.ksp.ods.capabilities.ObjectFactory m_ofCap = new org.ksp.ods.capabilities.ObjectFactory();
  private static final JAXBContext JCCaps = JaxbUtilities.createQuiet( org.ksp.ods.capabilities.ObjectFactory.class );

  private String NS_ODS_CAPABILITIES="http://www.ksp.org/ods/capabilities";
  private String NS_CHART_CONFIGURATION="http://www.ksp.org/chart/configuration";
  
  
  private String m_configFilePath;
  private ODSCapabilities m_caps=null;
  private ConfigLoader m_cl;
  private RequestBean m_requestBean;
  private Document m_document;



/**
 * TODO: remove RequestBean-member as only 2 values are needed
 */
  public CapabilitiesLoader(RequestBean requestBean)
  {
      m_requestBean=requestBean;
      
      //Nur wenn der Parameter "configFile" nicht gesetzt wurde, wird auf StandardPfade zurückgegriffen
      String pathConfig = System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT ) +  IODSConstants.ODS_CONFIG_NAME;
      String pathConfigFromUrl=requestBean.getParameterValue( "configFile" );
      if (! ( "" ).equals( pathConfigFromUrl))
      {
        pathConfig=pathConfigFromUrl;
      }
      System.out.println("Path config: "+pathConfig);
      m_configFilePath=pathConfig;
      loadFile();
      mergeConfigIntoODS();
  }

  /**
   * marshalling of Capabilites.xml
   */
  public void loadFile()
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware( true );
    
   
    Node configurationNode=null;
    Node serviceNode= null;
    Node capabilityNode= null;
   
    
    /**
     * Zunächst ODS-Node finden; der hat dann zwei Kindknoten Configuration und Service, die dann gemarshallt werden müssen
     */
    
    try
    {
      DocumentBuilder builder = factory.newDocumentBuilder();
      m_document = builder.parse( m_configFilePath);
      
      serviceNode=getServiceNode();
      capabilityNode=getFirstNodeByName( NS_ODS_CAPABILITIES, "Capability" );
      configurationNode=getFirstNodeByName( NS_CHART_CONFIGURATION, "Configuration" );
    }
    catch( SAXException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch( ParserConfigurationException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
    Logger.trace("loadFile Start");
    m_caps=m_ofCap.createODSCapabilities();
    
      try
      {
        //Capabilities laden 
        Object sn = JCCaps.createUnmarshaller().unmarshal( serviceNode);
        Object cn = JCCaps.createUnmarshaller().unmarshal( capabilityNode);
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Unmarshalling Configuration-Node from file '"+m_configFilePath+"'" );
        if (sn instanceof JAXBElement)
        {
          Object child=((JAXBElement) sn).getValue();
          m_caps=(ODSCapabilities) child;
        }
        else if (sn instanceof ODSCapabilities)
        {
          m_caps=(ODSCapabilities) sn;
        }
        else if (sn instanceof Service)
        {
          Service s=(Service) sn;
          m_caps.setService( s );
        }
        else
          Logger.trace("Klasse von Objekt: "+sn.getClass().getName());
        
        
        if (cn instanceof JAXBElement)
        {
          Object child=((JAXBElement) cn).getValue();
          m_caps=(ODSCapabilities) child;
        }
        else if (cn instanceof ODSCapabilities)
        {
          m_caps=(ODSCapabilities) cn;
        }
        else if (cn instanceof Capability)
        {
          Capability capability=(Capability) cn;
          m_caps.setCapability( capability);
        }
        else
          Logger.trace("Klasse von Objekt: "+sn.getClass().getName());
        
        
        Logger.trace("CapabilityDokument geladen");

        //ViewerConf
        m_cl=new ConfigLoader(configurationNode);

      }
      catch( JAXBException e )
      {
        Logger.trace("Exception");
        e.printStackTrace();
      }
      catch (ClassCastException e)
      {
        e.printStackTrace();
      }
      Logger.trace("loadFile End");
      
  }

  /**
   * @return Capabilities document as formatted xml string
   */
  public String getXMLString()
  {
    String capsString="";
    try
    {
      StringWriter sw=new StringWriter();
      Marshaller m= JCCaps.createMarshaller();
       m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

       //using namespace prefix mapper to display a namespace for any element
       m.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NamespacePrefixMapperImpl());


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
   * supplements capabilities document width diagrams, tables, legends defined in configuration.xml
   * - more precisely, legends are not defined inside the configuration file; instead, chart entries in the
   * configuration file are used to create diagrams AND legends
   */
  public void mergeConfigIntoODS()
  {

    //get actual service root from URL
    String serviceRoot="";
    if (m_requestBean!=null)
    {
      serviceRoot=m_requestBean.getUrl();
    }
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
    HashMap<String, ChartType> charts = m_cl.getCharts();
    Set<String> chartIds = charts.keySet();
    List<Diagram> diagrams = new LinkedList<Diagram>();
    if (charts!=null)
    {
      for( String chartId : chartIds )
      {
        ChartType chart=charts.get(chartId);
        String name= chart.getName();
        String title = chart.getTitle();
        String cabstract = chart.getDescription();

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

        //einzelne Layers auslesen - dazu werden zunächst die ChartElements verwendet
        //Layers layersElt = diagram.getLayers();

        Layers layers = m_ofCap.createLayers();
        List<LayerType> layerTypes = layers.getLayer();

        List<LayerRef> layerRefs = chart.getLayers().getLayerRef();
        for( LayerRef layerRef : layerRefs )
        {
          org.ksp.chart.configuration.LayerType lt = (org.ksp.chart.configuration.LayerType) layerRef.getRef();
          String ltName = lt.getName();
          String ltTitle = lt.getTitle();
          String ltDescription = lt.getDescription();
          LayerType layer = m_ofCap.createLayerType();
          layer.setName( ltName );
          layer.setTitle( ltTitle );
          layer.setDescription( ltDescription );
          layerTypes.add( layer );
        }

        diagram.setLayers( layers );
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
      for( String chartId : chartIds )
      {
        ChartType chart=charts.get(chartId);
        String name= chart.getName();
        String title = chart.getTitle();
        String cabstract = chart.getDescription();

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
    HashMap<String, TableType> vctables = m_cl.getTables();
    Set<String> tableIds = vctables.keySet();

    List<Table> captables = new LinkedList<Table>();
    if (vctables!=null)
    {
      for (String tableId:tableIds)
      {
        TableType vctable=vctables.get(tableId);
        String name= vctable.getName();
        String title = vctable.getHeaderText();
        String tabstract = vctable.getDescriptionText();

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
   * @return OnlineResource containing an url to a specified service
   * @param serviceRoot url of the service root
   * @param service service name, e.g. "ODS"
   * @param operation requested operation (REQUEST parameter)
   * @param name name of a selectable element (tables, diagrams, ...) - if not applicable, null shall be passed instead
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

  /**
   * creates DCPType (DCP = Distributed Computing Platform) using createOnlineResource method
   */
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
  
  /**
   * Returns the first node going by the name localName in the namespace ns 
   */
  private Node getFirstNodeByName(String namespace, String localName)
  {
    Node myNode=null;
    if (m_document!=null)
    {
      NodeList foundNodes=m_document.getElementsByTagNameNS( namespace, localName );
      if (foundNodes!=null && foundNodes.getLength()>0)
      {
        myNode=foundNodes.item( 0 );
        System.out.println("Found node by name: "+myNode.getNodeName());
      }
    }
    return myNode;
  }
  
  
  /**
   * returns Service-Node from ODS-Configuration-Document
   */
  public Node getServiceNode()
  {
    return getFirstNodeByName( NS_ODS_CAPABILITIES, "Service" );
  }
  
  /**
   * returns Capability-Node from ODS-Configuration-Document
   */
  public Node getCapabilityNode()
  {
    return getFirstNodeByName( NS_ODS_CAPABILITIES, "Capabilty" );
  }

  /**
   * returns Configuration-Node from ODS-Configuration-Document
   */
  public Node getConfigurationNode()
  {
    return getFirstNodeByName( NS_CHART_CONFIGURATION, "Configuration" );
  }

}


