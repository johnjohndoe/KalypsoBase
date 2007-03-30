/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.kalypso.service.ogc.RequestBean;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.axis.CalendarAxis;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.NumberAxis;
import org.kalypso.swtchart.chart.axis.XMLGregorianCalendarAxis;
import org.kalypso.swtchart.chart.axis.registry.IAxisRegistry;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.configuration.parameters.impl.CalendarParser;
import org.kalypso.swtchart.configuration.parameters.impl.MalformedValueException;
import org.kalypso.swtchart.logging.Logger;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author alibu
 *
 * Helper class for chart stuff concerning request parameters
 */
public class ODSUtils
{
  
  /**
   * sets the charts axes values as given by the request parameters AXIS_MIN and AXIS_MAX
   */
  public static void setAxisValues(Chart c, RequestBean requestBean )
  {
    String minstring=null;
    String maxstring=null;
    if (requestBean!=null)
    {
      minstring=requestBean.getParameterValue( "AXIS_MIN" );
      maxstring=requestBean.getParameterValue( "AXIS_MAX" );
    } 
    if (minstring!=null)
    {
      Logger.trace("REQUEST: "+minstring);
      String[] axes = minstring.split( ";" );
      IAxisRegistry ar=c.getAxisRegistry();
      for( String axis : axes )
      {
        String[] axinfo = axis.split( ":" );
        Logger.trace("REQUEST Axis: "+axis);
        IAxis iaxis = ar.getAxis( axinfo[0] );
        if (iaxis !=null)
        {
          if (iaxis instanceof NumberAxis)
          {
            iaxis.setFrom( Double.parseDouble( axinfo[1] ) );
          }
          else if (iaxis instanceof XMLGregorianCalendarAxis)
          {
            String[] date=axinfo[1].split("-");
            GregorianCalendar cal=new GregorianCalendar();
            cal.set( Calendar.YEAR, Integer.parseInt( date[0]) );
            cal.set( Calendar.MONTH, Integer.parseInt( date[1])-1 );
            cal.set( Calendar.DAY_OF_MONTH, Integer.parseInt( date[2]) );
            iaxis.setFrom( new XMLGregorianCalendarImpl(cal) );
          }
          else if (iaxis instanceof CalendarAxis)
          {
            CalendarParser cp=new CalendarParser();
            Calendar cal;
            try
            {
              cal = cp.createValueFromString( axinfo[1] );
              iaxis.setFrom( cal );
            }
            catch( MalformedValueException e )
            {
              e.printStackTrace();
            }
          }
        }
      }
    }
    if (maxstring!=null)
    {
      Logger.trace("REQUEST: "+maxstring);
      String[] axes = maxstring.split( ";" );
      IAxisRegistry ar=c.getAxisRegistry();
      for( String axis : axes )
      {
        String[] axinfo = axis.split( ":" );
        Logger.trace("REQUEST Axis: "+axis);
        IAxis iaxis = ar.getAxis( axinfo[0] );
        if (iaxis !=null)
        {
          if (iaxis instanceof NumberAxis)
          {
            iaxis.setTo( Double.parseDouble( axinfo[1] ) );
          }
          else if (iaxis instanceof XMLGregorianCalendarAxis)
          {
            String[] date=axinfo[1].split("-");
            GregorianCalendar cal=new GregorianCalendar();
            cal.set( Calendar.YEAR, Integer.parseInt( date[0]) );
            cal.set( Calendar.MONTH, Integer.parseInt( date[1])-1 );
            cal.set( Calendar.DAY_OF_MONTH, Integer.parseInt( date[2]) );
            iaxis.setTo( new XMLGregorianCalendarImpl(cal) );
          }
          else if (iaxis instanceof CalendarAxis)
          {
            /*
            String[] date=axinfo[1].split("-");
            GregorianCalendar cal=new GregorianCalendar();
            cal.set( Calendar.YEAR, Integer.parseInt( date[0]) );
            cal.set( Calendar.MONTH, Integer.parseInt( date[1])-1 );
            cal.set( Calendar.DAY_OF_MONTH, Integer.parseInt( date[2]) );
            iaxis.setTo( cal );
            */
            CalendarParser cp=new CalendarParser();
            Calendar cal;
            try
            {
              cal = cp.createValueFromString( axinfo[1] );
              iaxis.setTo( cal );
            }
            catch( MalformedValueException e )
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
          else
            System.out.println(iaxis.getClass().toString());
       //   Logger.trace(iaxis.getIdentifier()+": url to: "+axinfo[1]);
       //   Logger.trace(iaxis.getIdentifier()+": new to: "+iaxis.getTo());
        }
      }
    }
    
  }
  
  
  /**
   * sets the layers visibility from the request parameter LAYERS - if the parameter
   * is not empty, all those layers which are not mentioned in the comma separated list
   * are set invisible
   */
  public static void setLayerVisibility(Chart chart, RequestBean requestBean)
  {
    String reqLayerString=requestBean.getParameterValue( "LAYERS" );
    if (reqLayerString!=null)
    {
      String[] reqLayers=reqLayerString.split( "," );
      List<IChartLayer> layers = chart.getLayers();
      if (reqLayers.length>=1)
      {
        for( IChartLayer layer : layers )
        {
          boolean foundit=false;
          for (String reqLayer:reqLayers)
          {
            if (layer.getName().compareTo( reqLayer )==0)
            {
              foundit=true;
              Logger.trace("LAYER GEFUNDEN: "+layer.getName());
              break;
            }
          }
          if (!foundit)
          {
            //Es wurde nicht gefunden -> verunsichtbaren
            layer.setVisibility( false );
            System.out.println("Unsichtbar: "+layer.getName());
          }
          else
          {
            layer.setVisibility( true );
            System.out.println("Sichtbar: "+layer.getName());
          }
        }
      }
    }
  }
  
}
