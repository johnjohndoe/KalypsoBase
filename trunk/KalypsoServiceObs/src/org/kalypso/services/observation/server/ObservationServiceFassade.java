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
package org.kalypso.services.observation.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.ui.services.IDisposable;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.ogc.sensor.zml.ZmlURLConstants;
import org.kalypso.repository.IModifyableRepository;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IWriteableRepositoryItem;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.conf.RepositoryConfigUtils;
import org.kalypso.repository.conf.RepositoryFactoryConfig;
import org.kalypso.repository.factory.IRepositoryFactory;
import org.kalypso.repository.utils.RepositoryItemUtlis;
import org.kalypso.repository.utils.RepositoryUtils;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.i18n.Messages;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;
import org.kalypso.services.observation.sei.RepositoryBean;
import org.kalypso.zml.request.Request;
import org.xml.sax.InputSource;

/**
 * Kalypso Observation Service Fassade.
 * <p>
 * Server Fassade for an "local" IRepository
 * 
 * @author Dirk Kuch
 */
@SuppressWarnings("restriction")
public class ObservationServiceFassade implements IObservationService, IDisposable
{
  public static final String DESTINATION_REPOSITORY = "org.kalypso.services.observation.server.fassade.destination.repository.name";

  private IRepository m_repository = null;

  private ItemBean m_repositoryBean = null;

  /** Data-ID(String) --> File */
  private final Map<String, File> m_mapDataId2File;

  private final File m_tmpDir;

  private final Logger m_logger;

  private final String m_configurationLocation;

  /**
   * Constructs the service by reading the configuration.
   */
  public ObservationServiceFassade( ) throws RepositoryException
  {
    m_mapDataId2File = new Hashtable<String, File>( 128 );

    m_logger = Logger.getLogger( ObservationServiceFassade.class.getName() );

    m_tmpDir = FileUtilities.createNewTempDir( "Observations" ); //$NON-NLS-1$
    m_tmpDir.deleteOnExit();

    m_configurationLocation = FrameworkProperties.getProperty( KalypsoServiceObsActivator.SYSPROP_CONFIGURATION_LOCATION );

    /* HINT: The init method tries to access another servlet in the same container. */
    init();
  }

  /**
   * This function disposes everything.
   */
  public final void dispose( )
  {
    m_repository.dispose();

    // force delete, even if we called deleteOnExit()
    if( m_tmpDir.exists() )
      m_tmpDir.delete();
  }

  /**
   * Initialise the Service according to configuration.
   * 
   * @throws RemoteException
   */
  private synchronized void init( ) throws RepositoryException
  {
    try
    {
      final URL confLocation = new URL( m_configurationLocation );
      final URL confUrl = UrlResolverSingleton.resolveUrl( confLocation, "repositories_server.xml" ); //$NON-NLS-1$

      // this call also closes the stream
      final RepositoryFactoryConfig[] facConfs = RepositoryConfigUtils.loadConfig( confUrl );

      final RepositoryFactoryConfig config = RepositoryConfigUtils.resolveConfiguration( facConfs, System.getProperty( DESTINATION_REPOSITORY, null ) );
      final IRepositoryFactory factory = config.getFactory();
      m_repository = factory.createRepository();
    }
    catch( final Exception e )
    {
      if( e instanceof RepositoryException )
        throw (RepositoryException) e;

      throw new RepositoryException( "Initializing repository server fassade failed.", e );
    }
  }

  public final DataBean readData( final String href ) throws SensorException
  {
    final String hereHref = ZmlURL.removeServerSideId( href );
    final String obsId = ZmlURL.getIdentifierPart( hereHref );
    final ObservationBean obean = new ObservationBean( obsId );

    // request part specified?
    IRequest request = null;
    Request requestType = null;
    try
    {
      requestType = RequestFactory.parseRequest( hereHref );
      if( requestType != null )
      {
        request = ObservationRequest.createWith( requestType );

        m_logger.info( "Reading data for observation: " + obean.getId() + " Request: " + request ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      else
        m_logger.info( "Reading data for observation: " + obean.getId() ); //$NON-NLS-1$
    }
    catch( final SensorException e )
    {
      m_logger.warning( "Invalid Href: " + href ); //$NON-NLS-1$
      m_logger.throwing( getClass().getName(), "readData", e ); //$NON-NLS-1$

      // this is a fatal error (software programming error on the client-side)
      // so break processing now!
      throw e;
    }

    // fetch observation from repository
    IObservation obs = null;
    try
    {
      final IRepositoryItem item = itemFromBean( obean );

      obs = (IObservation) item.getAdapter( IObservation.class );
    }
    catch( final Exception e )
    {
      m_logger.info( "Could not find an observation for " + obean.getId() + ". Reason is:\n" + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$

      // this is not a fatal error, repository might be temporarely unavailable
    }

    if( obs == null )
    {
      // obs could not be created, use the request now
      m_logger.info( "Creating request-based observation for " + obean.getId() ); //$NON-NLS-1$
      obs = RequestFactory.createDefaultObservation( requestType );
    }

    // and eventually manipulate the observation
    updateObservation( obs, obean.getId() );

    try
    {
      // tricky: maybe make a filtered observation out of this one
      obs = FilterFactory.createFilterFrom( hereHref, obs, null );


      // name of the temp file must be valid against OS-rules for naming files
      // so remove any special characters
      final String tempFileName = FileUtilities.validateName( "___" + obs.getName(), "-" ); //$NON-NLS-1$ //$NON-NLS-2$

      // create temp file
      m_tmpDir.mkdirs(); // additionally create the parent dir if not already exists
      final File f = File.createTempFile( tempFileName, ".zml", m_tmpDir ); //$NON-NLS-1$

      // we say delete on exit even if we allow the client to delete the file
      // explicitly in the clearTempData() service call. This allows us to
      // clear temp files on shutdown in the case the client forgets it.
      f.deleteOnExit();

      ZmlFactory.writeToFile( obs, f, request );

      final DataBean data = new DataBean( f.toString(), new DataHandler( new FileDataSource( f ) ) );
      m_mapDataId2File.put( data.getId(), f );

      return data;
    }
    catch( final IOException e ) // generic exception used for simplicity
    {
      m_logger.throwing( getClass().getName(), "readData", e ); //$NON-NLS-1$
      throw new SensorException( e.getLocalizedMessage(), e );
    }
  }

  public final void clearTempData( final String dataId )
  {
    final File file = m_mapDataId2File.get( dataId );
    if( file != null )
    {
      final boolean b = file.delete();

      if( !b )
        m_logger.warning( Messages.getString( "org.kalypso.services.observation.server.ObservationServiceDelegate.0", file.toString(), dataId ) ); //$NON-NLS-1$
    }
    else
      m_logger.warning( Messages.getString( "org.kalypso.services.observation.server.ObservationServiceDelegate.1", dataId ) ); //$NON-NLS-1$
  }

  public final void writeData( final ObservationBean obean, final DataHandler odb ) throws SensorException
  {
    try
    {
      final IRepositoryItem item = itemFromBean( obean );

      final IObservation obs = (IObservation) item.getAdapter( IObservation.class );

      if( obs == null )
      {
        final RemoteException e = new RemoteException( "No observation for " + obean.getId() ); //$NON-NLS-1$
        m_logger.throwing( getClass().getName(), "writeData", e ); //$NON-NLS-1$
        throw e;
      }

      final IObservation zml = ZmlFactory.parseXML( new InputSource( odb.getInputStream() ), obs.getIdentifier(), null );

      synchronized( obs )
      {
        obs.setValues( zml.getValues( null ) );
      }
    }
    catch( final Throwable e ) // generic exception caught for simplicity
    {
      m_logger.throwing( getClass().getName(), "writeData", e ); //$NON-NLS-1$
      throw new SensorException( e.getLocalizedMessage(), e );
    }
  }

  /**
   * @throws NoSuchElementException
   *           if item and/or repository not found
   */
  private IRepositoryItem itemFromBean( final ItemBean obean ) throws RepositoryException
  {
    final String id = ZmlURL.removeServerSideId( obean.getId() );

    final IRepositoryItem item = m_repository.findItem( id );
    if( item == null )
      throw new NoSuchElementException( "Item does not exist or could not be found: " + id ); //$NON-NLS-1$

    return item;
  }

  /**
   * @see org.kalypso.repository.service.IRepositoryService#hasChildren(org.kalypso.repository.service.ItemBean)
   */
  public final boolean hasChildren( final ItemBean parent ) throws RepositoryException
  {
    final String id = parent.getId();
    final IRepositoryItem item = m_repository.findItem( id );

    return item.hasChildren();
  }

  /**
   * @see org.kalypso.repository.service.IRepositoryService#getChildren(org.kalypso.repository.service.ItemBean)
   */
  public final ItemBean[] getChildren( final ItemBean pbean ) throws RepositoryException
  {
    // dealing with ROOT?
    if( pbean == null )
    {
      createRepositoryBeans();

      return new ItemBean[] { m_repositoryBean };
    }

    try
    {
      final IRepositoryItem item = itemFromBean( pbean );
      final IRepositoryItem[] children = item.getChildren();

      final List<ItemBean> beans = new ArrayList<ItemBean>();
      for( final IRepositoryItem child : children )
      {
        final Boolean modifyable = child instanceof IWriteableRepositoryItem;

        beans.add( new ItemBean( child.getIdentifier(), child.getName(), modifyable ) );
      }

      return beans.toArray( new ItemBean[] {} );
    }
    catch( final RepositoryException e )
    {
      m_logger.throwing( getClass().getName(), "getChildren", e ); //$NON-NLS-1$
      throw e;
    }
  }

  /**
   * This function creates the repository beans, if necessary.
   */
  private void createRepositoryBeans( )
  {
    if( m_repositoryBean == null )
    {
      final Boolean modifyable = m_repository instanceof IModifyableRepository;
      m_repositoryBean = new RepositoryBean( m_repository.getIdentifier(), m_repository.getName(), modifyable );
    }
  }

  /**
   * @see org.kalypso.services.sensor.IObservationService#adaptItem(org.kalypso.repository.service.ItemBean)
   */
  public final ObservationBean adaptItem( final ItemBean ib ) throws SensorException
  {
    try
    {
      final IRepositoryItem item = itemFromBean( ib );
      if( item == null )
        return null;

      final IObservation obs = (IObservation) item.getAdapter( IObservation.class );
      if( obs == null )
        return null;

      final MetadataList md = updateObservation( obs, ib.getId() );
      final Boolean modifyable = item instanceof IWriteableRepositoryItem;

      return new ObservationBean( ib.getId(), obs.getName(), modifyable, md );
    }
    catch( final RepositoryException e )
    {
      m_logger.throwing( getClass().getName(), "adaptItem", e ); //$NON-NLS-1$
      throw new SensorException( e.getLocalizedMessage(), e );
    }
  }

  private MetadataList updateObservation( final IObservation obs, final String id )
  {
    // always update the observation metadata with the ocs-id
    final MetadataList md = obs.getMetadataList();
    md.setProperty( ZmlURLConstants.MD_OCS_ID, ZmlURL.addServerSideId( id ) );
    return md;
  }

  /**
   * @see org.kalypso.services.sensor.IObservationService#getServiceVersion()
   */
  public final int getServiceVersion( )
  {
    return 0;
  }

  /**
   * @see org.kalypso.repository.service.IRepositoryService#reload()
   */
  public void reload( )
  {
  }

  /**
   * @see org.kalypso.repository.service.IRepositoryService#findItem(java.lang.String)
   */
  public final ItemBean findItem( final String id ) throws RepositoryException
  {
    final IRepositoryItem item = m_repository.findItem( id );
    if( item != null )
    {
      final Boolean modifyable = item instanceof IWriteableRepositoryItem;

      return new ItemBean( item.getIdentifier(), item.getName(), modifyable );
    }

    return null;
  }

  /**
   * @see org.kalypso.services.observation.sei.IRepositoryService#makeItem(java.lang.String)
   */
  @Override
  public final void makeItem( final String itemIdentifier ) throws RepositoryException
  {
    if( m_repository instanceof IModifyableRepository )
    {
      final IModifyableRepository modifyable = (IModifyableRepository) m_repository;
      modifyable.makeItem( RepositoryItemUtlis.replaceIdentifier( itemIdentifier, modifyable.getIdentifier() ) );
    }
  }

  /**
   * @see org.kalypso.services.observation.sei.IRepositoryService#deleteItem(java.lang.String)
   */
  @Override
  public final void deleteItem( final String identifier ) throws RepositoryException
  {
    if( m_repository instanceof IModifyableRepository )
    {
      final IModifyableRepository modifyable = (IModifyableRepository) m_repository;
      modifyable.deleteItem( RepositoryItemUtlis.replaceIdentifier( identifier, modifyable.getIdentifier() ) );
    }
  }

  /**
   * @see org.kalypso.services.observation.sei.IRepositoryService#setItemData(java.lang.String, java.lang.Object)
   */
  @Override
  public final void setItemData( final String identifier, final Object serializable ) throws RepositoryException
  {
    if( m_repository instanceof IModifyableRepository )
    {
      final IRepositoryItem item = RepositoryUtils.findEquivalentItem( m_repository, identifier );
      if( item instanceof IWriteableRepositoryItem )
      {
        if( serializable instanceof Serializable )
        {
          final IWriteableRepositoryItem modifyable = (IWriteableRepositoryItem) item;
          modifyable.setData( (Serializable) serializable );
        }
        else
        {
          throw new NotImplementedException();
        }
      }
    }
  }

  /**
   * @see org.kalypso.services.observation.sei.IRepositoryService#setItemName(java.lang.String, java.lang.String)
   */
  @Override
  public final void setItemName( final String identifier, final String name ) throws RepositoryException
  {
    if( m_repository instanceof IModifyableRepository )
    {
      final IRepositoryItem item = m_repository.findItem( identifier );
      if( item instanceof IWriteableRepositoryItem )
      {
        final IWriteableRepositoryItem modifyable = (IWriteableRepositoryItem) item;
        modifyable.setName( name );
      }
    }
  }
}