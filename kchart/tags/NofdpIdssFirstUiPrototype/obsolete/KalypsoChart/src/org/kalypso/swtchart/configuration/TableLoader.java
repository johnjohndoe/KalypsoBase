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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.swtchart.exception.ConfigTableNotFoundException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.ksp.chart.tableconfiguration.ComponentType;
import org.ksp.chart.tableconfiguration.DataSourceType;
import org.ksp.chart.tableconfiguration.DateComponentType;
import org.ksp.chart.tableconfiguration.ObjectFactory;
import org.ksp.chart.tableconfiguration.TableType;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author burtscher
 * 
 * reads an observation and transforms its values into an html table
 */
public class TableLoader
{
  public final static ObjectFactory OF = new ObjectFactory();

  /**
   * checks if a tableType-object named tableName is found in the ConfigurationType. If so, it
   * tries to load the data, creates an html table and returns it as string. Otherwise, it throws a
   * ConfigTableNotFound Exception 
   */
  public static String createTable( ConfigurationType config, String tableName ) throws ConfigTableNotFoundException
  {
    List<TableType> tables = config.getTable();
    TableType foundTable = null;
    if( tables != null )
    {
      for( TableType table : tables )
      {
        if( table.getName().compareTo( tableName ) == 0 )
        {
          foundTable = table;
        }
      }
    }

    if( foundTable != null )
    {
      try
      {
        String htmltable = createTable( foundTable );
        return htmltable;
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
    else
    {
      throw new ConfigTableNotFoundException( tableName );
    }
    return null;
  }

  public static String createTable( TableType conftable ) throws Exception
  {
    DataSourceType dataSource = conftable.getDataSource();
    String url = dataSource.getUrl();
    String obsId = dataSource.getId();

    GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL( url ), null );
    Feature feature = workspace.getFeature( obsId );
    if( feature != null )
    {
      System.out.println( "Feature found: " + feature.getId() );
      return tableFromFeature( feature, conftable );
    }
    else
    {
      System.out.println( "Feature not found: " + obsId );
      return "";
    }
  }

  /**
   * creates an html table from the given features content, using the TableType from a confguration
   * in order to select the displayed properties; 
   * @param feature observation which shall be displayed as html table
   * @param conftable TableType which lists the contents to be contained in the table
   * @return string containing the html table  
   */
  private static String tableFromFeature( Feature feature, TableType conftable )
  {
    // Parameter auslesen

    String title = conftable.getTitle();
    String titleClass = conftable.getTitleClass();
    String abst = conftable.getAbstract();
    String abstClass = conftable.getAbstracClass();
    String tableClass = conftable.getTableClass();

    IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );
    TupleResult result = obs.getResult();

    String html = "";

    // Vorsicht: Unterscheidung zwischen TableComponent aus Config (compT) und und ResultComponent (compR)
    // List<ComponentType> compTs = conftable.getComponent();
    List<JAXBElement< ? extends ComponentType>> compTs = conftable.getComponent();

    IComponent[] compRs = result.getComponents();

    // Map mit Name der Conponente und den gewünschten Components des Results
    HashMap<String, IComponent> compRMap = new HashMap<String, IComponent>();
    // Map mit Name der Conponente und der Benutzer-Component -
    HashMap<String, ComponentType> compTMap = new HashMap<String, ComponentType>();
    // Array mit den Namen der Components - damit die Reihenfolge nicht verloren geht
    ArrayList<String> orderedNames = new ArrayList<String>();

    // Die Config als äußere Schleife, damit die Components in der richtigen
    // Reihenfolge angehängt werden
    for( JAXBElement compTmp : compTs )
    {
      ComponentType compT = null;
      if( compTmp.getValue() instanceof ComponentType )
        compT = (ComponentType) compTmp.getValue();
      else if( compTmp.getValue() instanceof DateComponentType )
        compT = (DateComponentType) compTmp.getValue();

      for( int i = 0; i < compRs.length; i++ )
      {
        IComponent compR = compRs[i];
        System.out.println( compT.getName() + " : " + compR.getName() );
        /*
         * Vergleich zwischen Component-Name und ItemID
         */
        if( compR.getName().compareTo( compT.getName() ) == 0 )
        {
          String name = compR.getName();
          orderedNames.add( name );
          compTMap.put( name, compT );
          compRMap.put( name, compR );
          break;
        }
      }
    }

    /* -------------- Tabelle zusammenbasteln ---------------- */

    // <table class=''>
    html += "<table ";
    if( tableClass != null )
    {
      html += " class='" + tableClass + "' ";
    }
    html += ">";

    // Titelzeile
    if( title != null )
    {
      html += "<tr><td colspan='" + compTMap.size() + "'";
      if( titleClass != null )
        html += " class='" + titleClass + "' ";
      html += ">";

      html += title;

      html += "</td>";
      html += "</tr>";
    }
    // Abstract
    if( abst != null )
    {
      html += "<tr><td colspan='" + compTMap.size() + "'";
      if( abstClass != null )
        html += " class='" + abstClass + "' ";
      html += ">";

      html += abst;

      html += "</td>";
      html += "</tr>";
    }

    // Jetzt die einzelnen Header aus der Konfiguration ausgeben - wenn es mindestens einen gibt
    int count = 0;
    String tmpHtml = "";
    tmpHtml += "<tr>";

    // dazu müssen die Schlüssel der Map durchlaufen werden
    for( String compName : orderedNames )
    {
      ComponentType compT = compTMap.get( compName );
      String header = compT.getHeader();
      String headerClass = compT.getHeaderClass();
      String compClass = compT.getComponentClass();
      String clazz = null;
      tmpHtml += "<td ";
      // Entweder Header- oder ComponentClass als class-Paramter
      if( headerClass != null )
        clazz = headerClass;
      else if( compClass != null )
        clazz = compClass;
      if( clazz != null )
        tmpHtml += " class='" + clazz + "' ";
      tmpHtml += ">";

      if( header != null )
      {
        count++;
        tmpHtml += header;
      }
      else
        tmpHtml += "&nbsp;";

      tmpHtml += "</td>";
    }
    tmpHtml += "</tr>";

    if( count > 0 )
      html += tmpHtml;

    // Ausgewählte Components durchlaufen und Tabelle erzeugen
    for( int i = 0; i < result.size(); i++ )
    {
      html += "<tr>";
      final IRecord record = result.get( i );
      for( String compName : orderedNames )
      {
        html += "<td ";

        // Wenn es eine gültige Klasse gibt: einsetzen
        IComponent compR = compRMap.get( compName );
        ComponentType compT = compTMap.get( compName );
        String compClass = compT.getComponentClass();
        if( compClass != null )
          html += " class='" + compClass + "' ";

        html += " >";
        // Falls es eine Datumskomponente ist, muss das Datum geparst werden
        if( compT instanceof DateComponentType )
        {
          DateComponentType compTDate = (DateComponentType) compT;
          String userFormat = compTDate.getDateFormat();
          SimpleDateFormat df = new SimpleDateFormat( userFormat );
          Object value = result.getValue( record, compR );
          if( value instanceof XMLGregorianCalendar )
          {
            XMLGregorianCalendar cal = (XMLGregorianCalendar) value;
            html += df.format( new Date( cal.toGregorianCalendar().getTimeInMillis() ) );
          }
          else
            html += value.toString();
        }
        else
          html += result.getValue( record, compR );
        html += "</td>";

      }
      html += "</tr>";
    }
    html += "</table>";

    return html;
  }

}
