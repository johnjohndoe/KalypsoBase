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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.kalypso.services.ods.Activator;
import org.kalypso.services.ods.operation.IODSConstants.ERROR_CODE;
import org.kalypso.services.ods.preferences.IODSPreferences;
import org.kalypso.services.ods.preferences.ODSPreferences;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.swtchart.configuration.TableLoader;
import org.kalypso.swtchart.exception.ConfigTableNotFoundException;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author alibu
 *
 */
public class GetHTMLTable implements IODSOperation
{

  /**
   * @see org.kalypso.services.ods.operation.IODSOperation#operate(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void operate( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet: GetHTMLTable", null ) );

    String name=request.getParameter( "NAME" );
    if (name!=null)
    {
      //    HTMLTabelle erzeugen
      IEclipsePreferences prefs = new ODSPreferences().getPreferences();
      String pathVC=prefs.get(IODSPreferences.CONFIG_PATH, "")+prefs.get(IODSPreferences.CONFIG_NAME, "");
      ConfigurationLoader cl=null;
        try
        {
          cl = new ConfigurationLoader(pathVC);
          //GMLWorkspace initialisieren
          KalypsoGisPlugin.getDefault();
          String htmltable = TableLoader.createTable(cl.getConfiguration(), name);
          final PrintWriter out=response.getWriter();
          response.setContentType( "text/html" );
          out.write( htmltable );
          out.close();
        }
        catch( JAXBException e )
        {
          e.printStackTrace();
        }
        catch( ConfigTableNotFoundException e )
        {
          ODSException.showException( request, response, ERROR_CODE.TABLE_NOT_DEFINED );
        }
    }
    else
      ODSException.showException( request, response, ERROR_CODE.NO_NAME_SPECIFIED );
    
  }
}
