/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.commons.net;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.commons.KalypsoCommonsPlugin;

/**
 * This job sends a http get request and stores the results.
 * 
 * @author Holger Albert
 */
public class SendHttpGetRequestJob extends Job
{
  /**
   * The url.
   */
  private final URL m_url;

  /**
   * The http response. It may be null, if the request could not be send to the server, or if the job was never started.
   */
  private HttpResponse m_response;

  /**
   * The constructor.
   * 
   * @param url
   *          The url.
   */
  public SendHttpGetRequestJob( final URL url )
  {
    super( "HttpGetRequestJob" );

    m_url = url;
    m_response = null;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    try
    {
      m_response = HttpClientUtilities.sendGet( m_url.toExternalForm() );

      return new Status( IStatus.OK, KalypsoCommonsPlugin.getID(), "Request was successfull." );
    }
    catch( final Exception ex )
    {
      m_response = null;

      return new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), ex.getLocalizedMessage(), ex );
    }
  }

  /**
   * This function returns the http response. It may be null, if the request could not be send to the server, or if the
   * job was never started.
   * 
   * @return The http response.
   */
  public HttpResponse getResponse( )
  {
    return m_response;
  }
}