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
package org.kalypso.ogc.sensor.zml;

import java.net.URL;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.utils.Repositories;
import org.kalypso.zml.request.Request;

/**
 * @author Gernot Belger
 */
final class ObservationRepositoryFetcher
{
  private ObservationRepositoryFetcher( )
  {
  }

  public static IObservation loadObservation( final URL url ) throws SensorException
  {
    try
    {
      final String urlBase = url.toExternalForm();
      if( ZmlURL.isEmpty( urlBase ) )
        return RequestFactory.createDefaultObservation( urlBase );

      final IRepository registeredRepository = Repositories.findRegisteredRepository( url.toExternalForm() );
      if( registeredRepository == null )
        return null;

      final String[] splittedUrlBase = urlBase.split( "\\?" ); //$NON-NLS-1$
      if( splittedUrlBase.length > 2 )
        throw new IllegalStateException( String.format( "Unknown URL format. Format = zml-proxy://itemId?parameter. Given %s", urlBase ) ); //$NON-NLS-1$

      final String itemId = splittedUrlBase[0];

      final IObservation observation = fetchZmlFromRepository( registeredRepository, itemId );

      /* If we have an request here but we did not find an observation -> create an request anyways */
      if( observation != null )
        return observation;

      final Request xmlReq = RequestFactory.parseRequest( urlBase );
      if( xmlReq != null )
        return RequestFactory.createDefaultObservation( xmlReq );

      return observation;
    }
    catch( final SensorException e )
    {
      throw e;
    }
    catch( final Exception ex )
    {
      throw new SensorException( "Parsing zml-proxy observation failed.", ex ); //$NON-NLS-1$
    }
  }

  private static IObservation fetchZmlFromRepository( final IRepository repository, final String itemId ) throws RepositoryException
  {
    final IRepositoryItem item = repository.findItem( itemId );
    if( item == null )
      throw new RepositoryException( String.format( "Unknown ID: %s", itemId ) );

    return (IObservation) item.getAdapter( IObservation.class );
  }
}
