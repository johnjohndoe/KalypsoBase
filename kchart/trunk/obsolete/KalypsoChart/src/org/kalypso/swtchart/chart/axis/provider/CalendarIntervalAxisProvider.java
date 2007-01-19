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
package org.kalypso.swtchart.chart.axis.provider;

import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.swtchart.chart.axis.CalendarAxis;
import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisProvider;
import org.kalypso.swtchart.chart.axis.IAxisConstants.DIRECTION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.IAxisConstants.PROPERTY;
import org.kalypso.swtchart.chart.axis.renderer.CalendarIntervalAxisRenderer;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;
import org.kalypso.swtchart.configuration.parameters.IParameterContainer;
import org.kalypso.swtchart.configuration.parameters.impl.AxisDirectionParser;
import org.kalypso.swtchart.configuration.parameters.impl.AxisPositionParser;
import org.kalypso.swtchart.configuration.parameters.impl.CalendarParser;
import org.kalypso.swtchart.configuration.parameters.impl.FontDataParser;
import org.kalypso.swtchart.configuration.parameters.impl.FontStyleParser;
import org.kalypso.swtchart.configuration.parameters.impl.MalformedValueException;
import org.kalypso.swtchart.configuration.parameters.impl.ParameterHelper;
import org.kalypso.swtchart.configuration.parameters.impl.RGBParser;
import org.kalypso.swtchart.exception.AxisProviderException;
import org.kalypso.swtchart.logging.Logger;
import org.ksp.chart.configuration.AxisType;


/**
 * @author alibu
 *
 */
public class CalendarIntervalAxisProvider implements IAxisProvider
{

  AxisType m_at;
  IParameterContainer m_pc;

  /**
   * @see org.kalypso.swtchart.chart.axis.IAxisProvider#getAxis()
   */
  public IAxis getAxis( ) throws AxisProviderException
  {
    AxisPositionParser app=new AxisPositionParser();
    POSITION pos=app.createValueFromString( m_at.getPosition() );
    AxisDirectionParser adp=new AxisDirectionParser();
    DIRECTION dir=adp.createValueFromString( m_at.getDirection() );
    IAxis<Calendar> axis = new CalendarAxis( m_at.getName(), m_at.getLabel(), PROPERTY.CONTINUOUS, pos, dir );
    if (axis!=null)
    {
        CalendarParser gcp=new CalendarParser();
        try
        {
          axis.setFrom( gcp.createValueFromString( m_at.getMinVal()) );
        }
        catch( MalformedValueException e )
        {
          //config-date not valid - using Calendar start date
          Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default date ");
          Calendar c=Calendar.getInstance();
          c.setTimeInMillis( 0 );
          axis.setFrom( c );
        }
        try
        {
          axis.setTo( gcp.createValueFromString( m_at.getMaxVal()));
        }
        catch( MalformedValueException e )
        {
          //config-date not valid - using "now"
          Logger.logError(Logger.TOPIC_LOG_CONFIG, "Unparsable value for AxisMin; using default date ");
          axis.setTo(  Calendar.getInstance());
        }
    }
    else
       throw new AxisProviderException();

    return axis;
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.IAxisProvider#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return Calendar.class;
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.IAxisProvider#getRenderer()
   */
  public IAxisRenderer getRenderer( )
  {
    RGBParser rgbp=new RGBParser();
    RGB fgRGB=m_pc.getParsedParameterValue( "color", "#000000", rgbp );
    RGB bgRGB=m_pc.getParsedParameterValue( "background-color", "#ffffff", rgbp );

    FontDataParser fdp=new FontDataParser();
    FontStyleParser fsp=new FontStyleParser();
    FontData fdLabel=m_pc.getParsedParameterValue( "font-family_label", "Arial", fdp );
    fdLabel.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_label", "10" ) ) );
    fdLabel.setStyle( m_pc.getParsedParameterValue( "font-style_label", "NORMAL", fsp ) );
    FontData fdTick=m_pc.getParsedParameterValue( "font-family_tick", "Arial", fdp );
    fdTick.setHeight( Integer.parseInt( m_pc.getParameterValue( "font-height_tick", "8" ) ) );
    fdTick.setStyle( m_pc.getParsedParameterValue( "font-style_tick", "NORMAL", fsp ) );

    int insetTick=Integer.parseInt( m_pc.getParameterValue( "inset_tick", "3" ));
    Insets insetsTick = new Insets( insetTick, insetTick, insetTick, insetTick );
    int insetLabel=Integer.parseInt( m_pc.getParameterValue( "inset_label", "3" ));
    Insets insetsLabel = new Insets( insetLabel, insetLabel, insetLabel, insetLabel );

    String dateFormatString=m_pc.getParameterValue( "dateFormat", "yyyy-MM-dd\nhh:mm:ss" );
    //Steuerzeichen aus Config ersetzen
    dateFormatString=dateFormatString.replace( "\\n", "\n" );
    IAxisRenderer<Calendar> calendarIntervalAxisRenderer = new CalendarIntervalAxisRenderer( fgRGB, bgRGB, 1, 5, insetsTick, insetsLabel, 0, fdLabel, fdTick, new SimpleDateFormat( dateFormatString ) );
    return calendarIntervalAxisRenderer;
  }

  /**
   * @see org.kalypso.swtchart.chart.axis.IAxisProvider#init(org.ksp.chart.configuration.AxisType)
   */
  public void init( AxisType at )
  {
    m_at=at;
    m_pc=new ParameterHelper();
    m_pc.addParameters( m_at.getParameters(), m_at.getName() );

  }


}
