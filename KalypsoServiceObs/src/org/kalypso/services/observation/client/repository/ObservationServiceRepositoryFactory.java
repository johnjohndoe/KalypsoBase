/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.services.observation.client.repository;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.factory.AbstractRepositoryFactory;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.server.ObservationServiceImpl;

/**
 * @author schlienger
 */
public class ObservationServiceRepositoryFactory extends AbstractRepositoryFactory
{
  public ObservationServiceRepositoryFactory( )
  {
    setRepositoryName( "Zeitreihen Dienst" ); //$NON-NLS-1$
  }

  /**
   * Does nothing
   * 
   * @see org.kalypso.repository.factory.IRepositoryFactory#configureRepository()
   */
  public final boolean configureRepository( )
  {
    final KalypsoServiceObsActivator plugin = KalypsoServiceObsActivator.getDefault();
    if( !plugin.isObservationServiceInitialized( getRepositoryName() ) )
    {
      final String wsdlLocationProperty = getConfiguration();
      if( wsdlLocationProperty != null && !wsdlLocationProperty.isEmpty() )
      {

        try
        {
          final String namespaceURI = "http://server.observation.services.kalypso.org/"; //$NON-NLS-1$
          final String serviceImplName = ObservationServiceImpl.class.getSimpleName();

          final URL wsdlLocation = new URL( wsdlLocationProperty );
          final QName serviceName = new QName( namespaceURI, serviceImplName + "Service" ); //$NON-NLS-1$
          final Service service = Service.create( wsdlLocation, serviceName );
          final IObservationService observationService = service.getPort( new QName( namespaceURI, serviceImplName + "Port" ), IObservationService.class ); //$NON-NLS-1$

          plugin.setObservationService( getRepositoryName(), observationService );
        }
        catch( final MalformedURLException e )
        {
          plugin.getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }
    }
    
    return true;
  }

  /**
   * @see org.kalypso.repository.factory.IRepositoryFactory#createRepository()
   */
  public final IRepository createRepository( ) throws RepositoryException
  {
    final KalypsoServiceObsActivator plugin = KalypsoServiceObsActivator.getDefault();

    if( !plugin.isObservationServiceInitialized( getRepositoryName() ) )
    {
      configureRepository();
    }

    return new ObservationServiceRepository( getRepositoryName(), getRepositoryLabel(), getClass().getName(), isReadOnly(), isCached() );
  }
}