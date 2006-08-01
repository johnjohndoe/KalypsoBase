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
package org.kalypso.services.ods.operation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.ows.ExceptionReport;
import net.opengis.ows.ExceptionType;
import net.opengis.ows.ObjectFactory;

import org.kalypso.jwsdp.JaxbUtilities;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.ui.KalypsoGisPlugin;



/**
 * @author alibu
 *
 */
public class ODSException
{

  private static ObjectFactory OF=new ObjectFactory();
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );
  
  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public static void showException( HttpServletRequest request, HttpServletResponse response, ERROR_CODE ec )
  {
      PrintWriter writer =null;
      try
      {
        //KalypsoGisPlugin.getDefault();
        writer = response.getWriter();
        response.setContentType("text/xml");
        writer.write( getXMLString(ec, request) );
      }
      catch( IOException e)
      {
        e.printStackTrace();
      }
      finally
      {
        if (writer!=null)
          writer.close();
      }
  }
  
  public static String getXMLString(ERROR_CODE e, HttpServletRequest request) throws IOException
  {
    ExceptionReport report = OF.createExceptionReport();
    ExceptionType extype = OF.createExceptionType();
    extype.setExceptionCode( e.toString() );
    extype.setLocator( request.getQueryString() );
    report.getException().add( extype );
    Marshaller m;
    String excString="";
    StringWriter sw=null;
    try
    {
      sw=new StringWriter();
      m = JC.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      m.marshal(report, sw);
      excString=sw.toString();
    }
    catch( JAXBException e1 )
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    finally
    {
      if (sw!=null)
        sw.close();
    }
    return excString;
  }

}
