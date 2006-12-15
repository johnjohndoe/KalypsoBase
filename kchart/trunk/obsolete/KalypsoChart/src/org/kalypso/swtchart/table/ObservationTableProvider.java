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
package org.kalypso.swtchart.table;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.swtchart.logging.Logger;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.ksp.chart.tableconfiguration.ComponentType;
import org.ksp.chart.tableconfiguration.DataSourceType;
import org.ksp.chart.tableconfiguration.DateComponentType;
import org.ksp.chart.tableconfiguration.TableType;

/**
 * @author burtscher1
 * 
 * TableProvider providing an ITable created from an Observation
 *
 */
public class ObservationTableProvider implements ITableProvider
{
  protected TableType m_tableType;

  public ObservationTableProvider()
  {
    
  }
  
  /**
   * @see org.kalypso.swtchart.table.ITableProvider#getTable()
   */
  public ITable getTable( )
  {
    try
    {
      ITable table=createTable();
      return table;
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @see org.kalypso.swtchart.table.ITableProvider#init(org.ksp.chart.tableconfiguration.TableType)
   */
  public void init( TableType tableType )
  {
    m_tableType=tableType;
  }
  
  
  public ITable createTable( ) throws Exception
  {
    DataSourceType dataSource = m_tableType.getDataSource();
    String url = dataSource.getUrl();
    String obsId = dataSource.getId();

    GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL( url ), null );
    Feature feature = workspace.getFeature( obsId );
    if( feature != null )
    {
      
      IObservation<TupleResult> obs = ObservationFeatureFactory.toObservation( feature );
      TupleResult observationResult = obs.getResult();
      
      Logger.trace( "Feature found: " + feature.getId() );
      return tableFromTupleResult( observationResult );
    }
    else
    {
      Logger.trace( "Feature not found: " + obsId );
      return null;
    }
  }

  /**
   * creates an itable from the given features content, using the TableType from a confguration
   * in order to select the displayed properties; 
   * @param feature observation which shall be displayed as html table
   * @param conftable TableType which lists the contents to be contained in the table
   * @return ITable to be displayed
   */
  protected ITable tableFromTupleResult( TupleResult observationResult )
  {
    // Parameter auslesen

    String title = m_tableType.getTitle();
    String titleClass = m_tableType.getTitleClass();
    String tAbstract = m_tableType.getAbstract();
    String tAbstractClass = m_tableType.getAbstractClass();
    String tableClass = m_tableType.getTableClass();

    
    //rausfinden, welche Componenten benötigt werden;
  
    //Das sind die Component-Elemente aus dem Configuration
    List<JAXBElement< ? extends ComponentType>> configComponents = m_tableType.getComponent();
    
    //... und das die IComponents aus der Observation
    IComponent[] resultComponents = observationResult.getComponents();
    
    //Array mit den Namen der Components - damit die Reihenfolge nicht verloren geht
    ArrayList<String> orderedNames = new ArrayList<String>();
    
    // Map mit Name der Conponente und den gewünschten Components des Results
    HashMap<String, IComponent> compRMap = new HashMap<String, IComponent>();
    //Hier werden die IComponents gespeichert, die in der Config angefordert werden 
    HashMap<String, ComponentType> compTMap = new HashMap<String, ComponentType>();
    
    // Map mit den CSS-Klassen der Component-Header
    Map<IComponent, String> componentHeaderClasses=new HashMap<IComponent, String>();
    // Map mit den CSS-Klassen der Component-Zelle
    Map<IComponent, String> componentClasses=new HashMap<IComponent, String>();
    // Map mit den Headern der Component-Zelle
    Map<IComponent, String> componentHeaders=new HashMap<IComponent, String>();
    
    /*
      Durchlaufen der configComponents - das muss in der 
      äusseren Schleife geschehen, da sonst die Reihenfolge aus der Config
      nicht beachtet würde
     */
    for( JAXBElement configComponent : configComponents )
    {
      ComponentType compT = (ComponentType) configComponent.getValue();
      
      for( int i = 0; i < resultComponents.length; i++ )
      {
        IComponent compR = resultComponents[i];
        /*
         * Vergleich zwischen Component-Name und ItemID
         */
        if( compR.getName().compareTo( compT.getName() ) == 0 )
        {
          String name = compR.getName();
          orderedNames.add( name );
          compTMap.put( name, compT );
          compRMap.put( name, compR );
          componentHeaderClasses.put( compR, compT.getHeaderClass() );
          componentHeaders.put( compR, compT.getHeader() );
          componentClasses.put( compR, compT.getComponentClass() );
          break;
        }
      }
    }
    
    
    int newComponentCount = orderedNames.size();
    int resultSize = observationResult.size();
    IComponent[] newComponents = new IComponent[newComponentCount];
    int count=0;
    //nur diese Componenten in das neue TupleResult bauen
    for(String name: orderedNames)
    {
       newComponents[count]=compRMap.get( name );
       count++;
    }
    TupleResult newResult=new TupleResult(newComponents);
    
    //Aufbau des neuen TupleResults
    for (int i=0;i<resultSize;i++)
    {
        IRecord newRecord = newResult.createRecord();
        IRecord oldRecord=observationResult.get( i );
        for (IComponent newComp: newComponents)
        {
          String value="";
          ComponentType configComponent=compTMap.get(newComp.getName());
          //Formattierung der Componenten-Werte bei Bedarf
          if ( configComponent instanceof DateComponentType)
          {
            DateComponentType dateConfigComponent=(DateComponentType) configComponent;
            XMLGregorianCalendar xmlcal= (XMLGregorianCalendar) oldRecord.getValue( newComp );
            GregorianCalendar cal=xmlcal.toGregorianCalendar();
            Date d = new Date(cal.getTimeInMillis());
            SimpleDateFormat sdf=new SimpleDateFormat(dateConfigComponent.getDateFormat());
            value= sdf.format(d );
          }
          else
          {
            value=oldRecord.getValue( newComp ).toString();
          }
          newRecord.setValue( newComp, value );
        }
        newResult.add( newRecord );
    }
    
    ObservationTable ot=new ObservationTable(tableClass, title, titleClass, tAbstract, tAbstractClass, newResult, componentHeaders, componentHeaderClasses, componentClasses);
    return ot;
  }


  
}
