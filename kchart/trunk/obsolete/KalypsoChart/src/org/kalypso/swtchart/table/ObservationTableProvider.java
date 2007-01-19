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

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.logging.Logger;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.ksp.chart.configuration.RefType;
import org.ksp.chart.configuration.TableColumnType;
import org.ksp.chart.configuration.TableType;
import org.ksp.chart.configuration.TableType.TableColumns;

/**
 * @author burtscher1
 * 
 * TableProvider providing an ITable created from an Observation
 *
 */
public class ObservationTableProvider implements ITableProvider
{
  protected TableType m_tableType;
  private TableColumns m_tableColumns;
  private URL m_context;

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
  public void init( TableType tableType, URL context )
  {
    m_tableType=tableType;
    m_context=context;
  }
  
  
  public ITable createTable( ) throws Exception
  {
    String id=m_tableType.getName();

    ParameterHelper ph=new ParameterHelper();
    ph.addParameters( m_tableType.getParameters(), m_tableType.getName());
    String url = ph.getParameterValue( "dataUrl", id, "");
    String obsId = ph.getParameterValue( "observationId", id, "");

    if (url.compareTo( "" )!=0 && obsId.compareTo( "" )!=0)
    {
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
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "Observation '" + obsId + "' not found in datasource");
        return null;
      }
    }
    else
    {
      if (url.compareTo( "" )!=0)
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "No dataUrl given.");
      if (obsId.compareTo( "" )!=0)
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "No observationId given.");
        
    }
    return null;
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
    // Tabellen-Parameter auslesen

    String headerText = m_tableType.getHeaderText();
    String headerClass = m_tableType.getHeaderClass();
    String descriptionText = m_tableType.getDescriptionText();
    String descriptionClass = m_tableType.getDescriptionClass();
    String tableContentClass = m_tableType.getContentClass();

    ParameterHelper columnPH= new ParameterHelper();
    
    
    //.die IComponents aus der Observation
    IComponent[] resultComponents = observationResult.getComponents();
    // Map mit den CSS-Klassen der Component-Header
    Map<IComponent, String> componentHeaderClasses=new HashMap<IComponent, String>();
    // Map mit den CSS-Klassen der Component-Zelle
    Map<IComponent, String> componentClasses=new HashMap<IComponent, String>();
    // Map mit den Headern der Component-Zelle
    Map<IComponent, String> componentHeaders=new HashMap<IComponent, String>();
    // Map mit der Zuordnung columnID -> IComponent
    Map<IComponent, String> componentColumnMap=new HashMap<IComponent, String>();
    
    
    
    //die einzelnen Tabellenspalten auslesen
  
    TableColumns tableColumns = m_tableType.getTableColumns();
    List<RefType> tableColumnRefs = tableColumns.getTableColumnRef();
    ArrayList<String> columnNamesOrdered=new ArrayList<String>();
    
    
    //Array für die neuen Components
    IComponent[] newComponents = new IComponent[tableColumnRefs.size()];
    
    
    //TableColumns durchlaufen, dabei Headers, HeaderClasses, componentClasses und newComponents aufbauen
    int count=0;
    for( RefType tableColumnRef : tableColumnRefs )
    {
      TableColumnType tct=(TableColumnType) tableColumnRef.getRef();
      String columnId=tct.getName();
      String componentId=tct.getComponentId();
      for (IComponent resultComponent: resultComponents)
      {
        if (resultComponent.getId().compareTo( componentId )==0 )
        {
          componentHeaderClasses.put( resultComponent, tct.getHeaderClass() );
          componentHeaders.put( resultComponent, tct.getHeaderText() );
          componentClasses.put( resultComponent, tct.getContentClass() );
          componentColumnMap.put(resultComponent, columnId );
          newComponents[count]=resultComponent;
          count++;
          break;
        }
      }
    }
 
    // Map mit Name der Conponente und den gewünschten Components des Results
    HashMap<String, IComponent> compRMap = new HashMap<String, IComponent>();
    //Hier werden die IComponents gespeichert, die in der Config angefordert werden 
    HashMap<String, String> compTMap = new HashMap<String, String>();
    

    
   
    TupleResult newResult=new TupleResult(newComponents);
    
    //Aufbau des neuen TupleResults - alle Zeilen durchlaufen und bei Bedarf neu Aufbauen
    for (int i=0;i<observationResult.size();i++)
    {
        IRecord newRecord = newResult.createRecord();
        IRecord oldRecord=observationResult.get( i );
        for (IComponent newComp: newComponents)
        {
          String value="";
          
          String columnId=componentColumnMap.get( newComp );
          String columnType=columnPH.getParameterValue( "type", columnId, "");
          
          if (columnType.compareTo( "DATE" )==0)
          {
            String dateFormat=columnPH.getParameterValue( "", columnId, "YYYY.MM.DDThh:ss" );
            XMLGregorianCalendar xmlcal= (XMLGregorianCalendar) oldRecord.getValue( newComp );
            GregorianCalendar cal=xmlcal.toGregorianCalendar();
            Date d = new Date(cal.getTimeInMillis());
            SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
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
    ObservationTable ot=new ObservationTable(tableContentClass, headerText, headerClass, descriptionText, descriptionClass, newResult, componentHeaders, componentHeaderClasses, componentClasses);
    return ot;
  }


  
}
