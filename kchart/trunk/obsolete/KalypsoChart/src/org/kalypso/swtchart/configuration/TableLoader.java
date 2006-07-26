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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;

import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.ksp.chart.tableconfiguration.ComponentType;
import org.ksp.chart.tableconfiguration.ObjectFactory;
import org.ksp.chart.tableconfiguration.TableType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author alibu
 *
 */
public class TableLoader
{
  
  public final static ObjectFactory OF = new ObjectFactory();
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );
  
  
  public static String createTable(ConfigurationType config, String tableName)
  {
    List<TableType> tables = config.getTable();
    if (tables!=null)
    {
      for( TableType table : tables )
      {
        if (table.getName().compareTo(tableName)==0)
        {
          try
          {
            return createTable(table);
          }
          catch( Exception e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }
  
  public static String createTable(TableType conftable) throws Exception 
  {
    String html="";
    String dataSource = conftable.getDataSource();
    
    GMLWorkspace workspace = GmlSerializer.createGMLWorkspace(new URL(dataSource));
    String obsId = conftable.getObservation().getId();
    Feature feature = workspace.getFeature(obsId);
    if (feature!=null)
      System.out.println("Feature found: "+feature.getId());
    else 
      System.out.println("Feature not found: "+feature.getId());
    
    
    IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation(feature);
    TupleResult result = obs.getResult();
   
    //Vorsicht: Unterscheidung zwischen TableComponent aus Config (compT) und und ResultComponent (compR) 
    List<ComponentType> compTs = conftable.getComponent();
    IComponent[] compRs = result.getComponents();
    
    //Liste mit den gewünschten Components des Results erzeugen
    LinkedList<IComponent> list = new LinkedList<IComponent>(  );
    for( int i = 0; i < compRs.length; i++ )
    {
      IComponent compR=compRs[i];
      System.out.println("Component: "+compR.getName());
      System.out.println("Component: "+compR.getDescription());
      System.out.println("Component: "+compR.getPosition());
      System.out.println("Component: "+compR.getValueTypeName());
      for (ComponentType compT: compTs)
      {
        /*
        Vergleich zwischen Component-Name und ItemID 
        TODO: das läuft derzeit noch unter dem NAmen der Component - besser ist ItemDefinition-id, das
        gibt aber die Schnittstelle IComponent nicht her => Ändern
        */
        if (compR.getName().compareTo(compT.getItemID())==0)
          list.add(compR );
      }
    }
    
    //Ausgewählte Spalten durchlaufen und Tabelle erzeugen
    html+="<table border='1'>";
    html+="<tr><td colspan='"+list.size()+"'>";
    html+=obsId;
    html+="</td></tr>";
    for (int i=0; i<result.size();i++)
    {
      html+="<tr>";
      final IRecord record = result.get( i );
      for (int j=0;j<list.size();j++)
      {
        html+="<td>";
        html+=result.getValue(record, list.get(j));
        html+="</td>";
      }
      html+="</tr>";
    }
    html+="</table>";
    
    return html;
  }


}
