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
package org.kalypso.service.wps.server.operations;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.wps.internal.KalypsoServiceWPSDebug;
import org.kalypso.service.wps.utils.MarshallUtilities;
import org.kalypso.service.wps.utils.ogc.ExecuteMediator;
import org.kalypso.service.wps.utils.simulation.WPSSimulationInfo;
import org.kalypso.service.wps.utils.simulation.WPSSimulationManager;

/**
 * This operation will execute a specific simulation.
 * 
 * @author Holger Albert
 */
public class ExecuteOperation implements IOperation
{
  /**
   * The constructor.
   */
  public ExecuteOperation( )
  {
  }

  /**
   * @see org.kalypso.service.wps.operations.IOperation#executeOperation(org.kalypso.service.ogc.RequestBean)
   */
  public StringBuffer executeOperation( final RequestBean request ) throws OWSException
  {
    final StringBuffer response = new StringBuffer();

    /* Start the operation. */
    KalypsoServiceWPSDebug.DEBUG.printf( "Operation \"Execute\" started.\n" ); //$NON-NLS-1$

    /* Gets the identifier, but also unmarshalls the request, so it has to be done! */
    final String requestXml = request.getBody();
    Object executeRequest = null;
    try
    {
      executeRequest = MarshallUtilities.unmarshall( requestXml );
    }
    catch( final JAXBException e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" ); //$NON-NLS-1$
    }

    /* Execute the simulation via a manager, so that more than one simulation can be run at the same time. */
    final WPSSimulationManager manager = WPSSimulationManager.getInstance();

    // TODO version 1.0
    final ExecuteMediator executeMediator = new ExecuteMediator( executeRequest );
    final WPSSimulationInfo info = manager.startSimulation( executeMediator );

    /* Prepare the execute response. */
    FileObject resultFile = null;
    try
    {
      final FileObject resultDir = manager.getResultDir( info.getId() );
      resultFile = resultDir.resolveFile( "executeResponse.xml" ); //$NON-NLS-1$
      int time = 0;
      int timeout = 10000;
      final int delay = 500;
      while( !resultFile.exists() && time < timeout )
      {
        Thread.sleep( delay );
        time += delay;
      }
      final FileContent content = resultFile.getContent();
      final InputStream inputStream = content.getInputStream();
      final String responseXml = MarshallUtilities.fromInputStream( inputStream );
      response.append( responseXml );
    }
    catch( final Exception e )
    {
      throw new OWSException( OWSException.ExceptionCode.NO_APPLICABLE_CODE, e, "" ); //$NON-NLS-1$
    }
    finally
    {
      if( resultFile != null )
        try
        {
          resultFile.close();
        }
        catch( final FileSystemException e )
        {
          // gobble
        }
    }

    return response;
  }
}