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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.kalypso.jwsdp.JaxbUtilities;
import org.ksp.chart.tableconfiguration.TableType;
import org.ksp.chart.viewerconfiguration.ChartType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;
import org.ksp.chart.viewerconfiguration.ObjectFactory;
import org.ksp.ods.capabilities.Diagram;
import org.ksp.ods.capabilities.ODSCapabilities;
import org.ksp.ods.capabilities.Table;


/**
 * @author alibu
 *
 */
public class ParseFile
{
  public final static org.ksp.ods.capabilities.ObjectFactory m_ofCap = new org.ksp.ods.capabilities.ObjectFactory();
  public final static org.ksp.chart.viewerconfiguration.ObjectFactory m_ofVC = new org.ksp.chart.viewerconfiguration.ObjectFactory();
  private static final JAXBContext JCCaps = JaxbUtilities.createQuiet( org.ksp.ods.capabilities.ObjectFactory.class );
  private static final JAXBContext JCVC = JaxbUtilities.createQuiet( org.ksp.chart.viewerconfiguration.ObjectFactory.class );
  
  //private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );
  private String m_configPathCaps="/home/alibu/dev/kalypso_workspace312/KalypsoWDS/etc/binding/examples/Capabilities.xml";
  private String m_configPathVC="/home/alibu/dev/kalypso_workspace312/KalypsoChart/etc/binding/examples/Configuration.xml";
  private ODSCapabilities m_caps=null;
  private ConfigurationType m_vc=null;
  
  public ParseFile()
  {
   // init();
  }
  
  public ParseFile(String pathCaps, String pathVC)
  {
      m_configPathCaps=pathCaps;
      m_configPathVC=pathVC;
      loadFile();
     // init();
  }
  
  /*
  public void init()
  {
    try
    {
      m_ctxODS=JAXBContext.newInstance("org.ksp.ods.capabilities");
      m_ctxChart=JAXBContext.newInstance("org.ksp.chart.viewerconfiguration");
      loadFile();
    }
    catch( JAXBException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  */
  
  public void loadFile()
  {
    System.out.println("loadFile Start");
      try
      {
        //CApabilities laden
        
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
  
  public ODSCapabilities getCaps()
  {
    return m_caps;
  }
  
  public String getCapsString()
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
  public ODSCapabilities mergeConfigIntoODS()
  {
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
        
        Diagram diagram = m_ofCap.createDiagram();
        diagram.setName(name);
        diagram.setTitle(title);
        diagram.setAbstract(cabstract);
        diagram.setQueryable( true );
        diagrams.add(diagram);
      }
    }
    if (diagrams.size()>0)
    {
      //an die "hartcodierten" Tabellen anhängen
      List<Diagram> diagram = m_caps.getCapability().getDiagram();
      diagram.addAll(diagrams);
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
        String cabstract = table.getAbstract();
        
        Table captable = m_ofCap.createTable();
        
        captable.setName(name);
        captable.setTitle(title);
        captable.setAbstract(cabstract);
        captable.setQueryable( true );
        captables.add(captable);
      }
    }
    if (captables.size()>0)
    {
      //an die "hartcodierten" Tabellen anhängen
      List<Table> tables = m_caps.getCapability().getTable();
      tables.addAll(captables);
    }
    
    
    return null;
  }

}


