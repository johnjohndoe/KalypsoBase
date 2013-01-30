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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.ui.services.IDisposable;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.filter.filters.ZmlFilter;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.repository.IModifyableRepository;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.IWriteableRepository;
import org.kalypso.repository.IWriteableRepositoryItem;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.conf.RepositoryConfigUtils;
import org.kalypso.repository.conf.RepositoryFactoryConfig;
import org.kalypso.repository.factory.IRepositoryFactory;
import org.kalypso.repository.utils.Repositories;
import org.kalypso.repository.utils.RepositoryItems;
import org.kalypso.services.observation.KalypsoServiceObs;
import org.kalypso.services.observation.ObservationServiceUtils;
import org.kalypso.services.observation.i18n.Messages;
import org.kalypso.services.observation.sei.DataBean;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.sei.ItemBean;
import org.kalypso.services.observation.sei.ObservationBean;
import org.kalypso.services.observation.sei.RepositoryBean;
import org.kalypso.services.observation.sei.StatusBean;
import org.kalypso.zml.request.Request;

/**
 * Kalypso Observation Service.
 * <p>
 * When a observation is delivered to the client, the IObservationManipulator mechanism is always used to possibly
 * manipulate the observation before it is delivered. ObservationManipulators are configured within the
 * IObservationService configuration file. All entries that begin with "MANIPULATOR_" are defining such manipulators.
 * The syntax of the configuration is as follows: MANIPULATOR_&lt;repository_id&gt;=&lt;manipulator_class_name&gt;.
 * 
 * @author Marc Schlienger
 * @author Gernot Belger
 * @author Holger Albert
 */
@SuppressWarnings("restriction")
public class ObservationServiceDelegate implements IObservationService, IDisposable
{
  private final List<IRepository> m_repositories = new Vector<IRepository>();

  private ItemBean[] m_repositoryBeans = null;

  /** Bean-ID(String) --> IRepositoryItem */
  private final Map<String, IRepositoryItem> m_mapBeanId2Item = new Hashtable<String, IRepositoryItem>( 512 );

  /** IRepositoryItem --> ItemBean */
  private final Map<IRepositoryItem, ItemBean[]> m_mapItem2Bean = new Hashtable<IRepositoryItem, ItemBean[]>( 512 );

  /** Repository-ID(String) --> IRepository */
  private final Map<String, IRepository> m_mapRepId2Rep = new Hashtable<String, IRepository>();

  /** Data-ID(String) --> File */
  private final Map<String, File> m_mapDataId2File = new Hashtable<String, File>( 128 );

  private final File m_tmpDir = FileUtilities.createNewTempDir( "Observations" ); //$NON-NLS-1$

  private final Logger m_logger = Logger.getLogger( ObservationServiceDelegate.class.getName() );

  private boolean m_initialized = false;

  private final String m_configurationLocation = FrameworkProperties.getProperty( KalypsoServiceObs.SYSPROP_CONFIGURATION_LOCATION );

  /**
   * Constructs the service by reading the configuration.
   */
  public ObservationServiceDelegate( )
  {
    m_tmpDir.deleteOnExit();

    /* HINT: The init method tries to access another servlet in the same container. */
    init();
  }

  @Override
  protected final void finalize( ) throws Throwable
  {
    // System.out.println( "Finalize observation service delegate (" + this.toString() + ") ... " +
    // DateFormat.getDateTimeInstance().format( Calendar.getInstance().getTime() ) );

    /* Dispose everything. */
    dispose();
  }

  /**
   * This function disposes everything.
   */
  @Override
  public final void dispose( )
  {
    clearCache();

    // force delete, even if we called deleteOnExit()
    if( m_tmpDir.exists() )
      m_tmpDir.delete();
  }

  private synchronized void clearCache( )
  {
    m_mapBeanId2Item.clear();
    m_mapItem2Bean.clear();
    m_mapRepId2Rep.clear();
    m_repositoryBeans = null;

    // dispose repositories
    final IRepository[] repositories = m_repositories.toArray( new IRepository[] {} );
    for( final IRepository repository : repositories )
      repository.dispose();

    m_repositories.clear();

    // clear temp files
    final File[] files = m_mapDataId2File.values().toArray( new File[] {} );
    for( final File file : files )
    {
      // File may already have been deleted
      if( file.exists() )
        FileUtils.deleteQuietly( file );
    }

    m_mapDataId2File.clear();

    ZmlFilter.configureFor( null );
  }

  /**
   * Initialise the Service according to configuration.
   * 
   * @throws RemoteException
   */
  protected final synchronized void init( )
  {
    // TODO: at the moment, we silently ignore this implementation if no
    // service location was given: this is necessary, because if the help
    // system is running (jetty!), this implementation is also available at the client side...
    // TODO Solution: remove this server code from the client side... (but in order to do that, we must split-up the
    // observation service plug-ins)
    if( m_initialized || m_configurationLocation == null )
      return;

    m_initialized = true;

    clearCache();

    try
    {
      // this call also closes the stream
      final URL confLocation = new URL( m_configurationLocation );
      final URL confUrl = UrlResolverSingleton.resolveUrl( confLocation, "repositories_server.xml" ); //$NON-NLS-1$
      final RepositoryFactoryConfig[] facConfs = RepositoryConfigUtils.loadConfig( confUrl );

      // load the service properties
      final URL urlProps = UrlResolverSingleton.resolveUrl( confLocation, "service.properties" ); //$NON-NLS-1$
      final Properties props = loadProperties( urlProps );

      /* Configure logging according to configuration */
      configureLogger( props );

      /* Load Repositories */
      for( final RepositoryFactoryConfig item : facConfs )
      {
        final IRepositoryFactory fact = item.getFactory();

        try
        {
          final IRepository rep = fact.createRepository();
          m_repositories.add( rep );

          m_mapRepId2Rep.put( rep.getIdentifier(), rep );
        }
        catch( final Exception e )
        {
          m_logger.warning( "Could not create Repository " + fact.getRepositoryName() + " with configuration " + fact.getConfiguration() + ". Reason is:\n" + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          e.printStackTrace();
        }
      }

      // tricky: set the list of repositories to the ZmlFilter so that
      // it can directly fetch the observations without using the default
      // URL resolving stuff
      ZmlFilter.configureFor( m_repositories );
    }
    catch( final Exception e ) // generic exception caught for simplicity
    {
      m_logger.throwing( getClass().getName(), "init", e ); //$NON-NLS-1$

      // FIXME: should'nt we set m_initialized to false here? depending on the kind of error
    }
  }

  private void configureLogger( final Properties props )
  {
    try
    {
      final String logLevelString = props.getProperty( "LOG_LEVEL", Level.INFO.getName() ); //$NON-NLS-1$
      final Level logLevel = Level.parse( logLevelString );
      Logger.getLogger( "" ).setLevel( logLevel ); //$NON-NLS-1$
    }
    catch( final Throwable t )
    {
      // Catch everything, changing the log level should not prohibit this service to run
      t.printStackTrace();
    }
  }

  private Properties loadProperties( final URL urlProps )
  {
    InputStream ins = null;

    try
    {
      final Properties props = new Properties();
      ins = urlProps.openStream();
      props.load( ins );
      ins.close();

      return props;
    }
    catch( final IOException e )
    {
      m_logger.warning( "Cannot read properties-file: " + e.getLocalizedMessage() ); //$NON-NLS-1$
      return new Properties();
    }
    finally
    {
      IOUtils.closeQuietly( ins );
    }
  }

  @Override
  public final DataBean readData( final String href ) throws SensorException
  {
    init();

    final String hereHref = ObservationServiceUtils.removeServerSideId( href );
    final String obsId = org.kalypso.ogc.sensor.zml.ZmlURL.getIdentifierPart( hereHref );
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

        // m_logger.info( "Reading data for observation: " + obean.getId() + " Request: " + request ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      // else
      //  m_logger.info( "Reading data for observation: " + obean.getId() ); //$NON-NLS-1$
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

      if( obs == null )
        m_logger.info( "Could not find an observation for " + obean.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch( final Exception e )
    {
      m_logger.info( "Could not find an observation for " + obean.getId() + ". Reason is:\n" + e.getLocalizedMessage() ); //$NON-NLS-1$ //$NON-NLS-2$

      // this is not a fatal error, repository might be temporarily unavailable
    }

    if( obs == null )
    {
      // FIXME: check: why do we do this on the server side, dubious! We should rather throw an exception,
      // the clients should handle this

      // obs could not be created, use the request now
      m_logger.info( "Creating request-based observation for " + obean.getId() ); //$NON-NLS-1$
      m_logger.info( "Request was: " + hereHref ); //$NON-NLS-1$
      obs = RequestFactory.createDefaultObservation( requestType );
    }

    if( obs == null )
      throw new SensorException( String.format( "Unknwon repository item: %s", href ) ); //$NON-NLS-1$

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
      // explicitely in the clearTempData() service call. This allows us to
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

  @Override
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

  /**
   * @throws NoSuchElementException
   *           if item and/or repository not found
   */
  private IRepositoryItem itemFromBean( final ItemBean obean ) throws RepositoryException
  {
    if( obean == null )
      throw new NullPointerException( "ItemBean must not be null" ); //$NON-NLS-1$

    /* Create the repository beans, if neccessary. */
    createRepositoryBeans();

    final String id = ObservationServiceUtils.removeServerSideId( obean.getId() );

    // maybe bean already in map?
    if( m_mapBeanId2Item.containsKey( id ) )
      return m_mapBeanId2Item.get( id );

    // try with repository id
    final String repId = Repositories.getRepositoryId( id );
    if( m_mapRepId2Rep.containsKey( repId ) )
    {
      final IRepository rep = m_mapRepId2Rep.get( repId );

      final IRepositoryItem item = rep.findItem( id );

      if( item == null )
        throw new NoSuchElementException( "Item does not exist or could not be found: " + id ); //$NON-NLS-1$

      return item;
    }

    // last chance: go through repositories and use findItem()
    for( final Object element : m_repositories )
    {
      final IRepository rep = (IRepository) element;

      final IRepositoryItem item = rep.findItem( id );
      if( item != null )
        return item;
    }

    throw new NoSuchElementException( "Unknown Repository or item. Repository: " + repId + ", Item: " + id ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public final boolean hasChildren( final ItemBean parent ) throws RepositoryException
  {
    init();

    // dealing with ROOT?
    if( parent == null )
      return m_repositories.size() > 0;

    try
    {
      final IRepositoryItem item = itemFromBean( parent );

      return item.hasChildren();
    }
    catch( final RepositoryException e )
    {
      m_logger.throwing( getClass().getName(), "hasChildren", e ); //$NON-NLS-1$
      throw e;
    }
  }

  @Override
  public final ItemBean[] getChildren( final ItemBean pbean ) throws RepositoryException
  {
    init();

    // dealing with ROOT?
    if( pbean == null )
    {
      createRepositoryBeans();
      return m_repositoryBeans;
    }

    IRepositoryItem item = null;

    try
    {
      item = itemFromBean( pbean );

      // already in cache?
      if( m_mapItem2Bean.containsKey( item ) )
        return m_mapItem2Bean.get( item );

      final IRepositoryItem[] children = item.getChildren();

      final ItemBean[] beans = new ItemBean[children.length];
      for( int i = 0; i < beans.length; i++ )
      {
        final IRepositoryItem child = children[i];
        final Boolean modifyable = child instanceof IWriteableRepositoryItem;

        beans[i] = new ItemBean( child.getIdentifier(), child.getName(), modifyable );

        // store it for future referencing
        m_mapBeanId2Item.put( beans[i].getId(), children[i] );
      }

      // cache it for next request
      m_mapItem2Bean.put( item, beans );

      return beans;
    }
    catch( final RepositoryException e )
    {
      m_logger.throwing( getClass().getName(), "getChildren", e ); //$NON-NLS-1$
      throw e;
    }
  }

  /**
   * This function creates the repository beans, if neccessary.
   */
  private void createRepositoryBeans( )
  {
    if( m_repositoryBeans == null )
    {
      m_repositoryBeans = new ItemBean[m_repositories.size()];

      for( int i = 0; i < m_repositoryBeans.length; i++ )
      {
        final IRepository rep = m_repositories.get( i );
        final Boolean modifyable = rep instanceof IModifyableRepository;

        m_repositoryBeans[i] = new RepositoryBean( rep.getIdentifier(), rep.getName(), modifyable );
        m_mapBeanId2Item.put( m_repositoryBeans[i].getId(), rep );
      }
    }
  }

  @Override
  public final ObservationBean adaptItem( final ItemBean ib ) throws SensorException
  {
    try
    {
      init();

      final IRepositoryItem item = itemFromBean( ib );
      if( item == null )
        return null;

      final IObservation obs = (IObservation) item.getAdapter( IObservation.class );
      if( obs == null )
        return null;

      final Boolean modifyable = item instanceof IWriteableRepositoryItem;

      return new ObservationBean( ib.getId(), obs.getName(), modifyable, obs.getMetadataList() );
    }
    catch( final RepositoryException e )
    {
      m_logger.throwing( getClass().getName(), "adaptItem", e ); //$NON-NLS-1$
      throw new SensorException( e.getLocalizedMessage(), e );
    }
  }

  @Override
  public final int getServiceVersion( )
  {
    return 0;
  }

  @Override
  public final void reload( )
  {
    m_initialized = false;

    init();
  }

  @Override
  public final ItemBean findItem( final String id ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      final IRepositoryItem item;

      // first check the repository itself, then look into it
      if( repository.getIdentifier().equals( id ) )
        item = repository;
      else
      {
        try
        {
          item = Repositories.findEquivalentItem( repository, id );
        }
        catch( final RepositoryException e )
        {
          m_logger.throwing( getClass().getName(), "findItem", e ); //$NON-NLS-1$
          throw e;
        }
      }

      if( item != null )
      {
        final ItemBean bean = toBean( item );

        // store it for future referencing
        m_mapBeanId2Item.put( bean.getId(), item );

        return bean;
      }
    }

    m_logger.warning( Messages.getString( "org.kalypso.services.observation.server.ObservationServiceDelegate.3", id ) ); //$NON-NLS-1$

    return null;
  }

  /**
   * FIXME at the moment we assume that an new item should be created in all sub repositories
   */
  @Override
  public final void makeItem( final String itemIdentifier ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      if( repository instanceof IModifyableRepository )
      {
        final IModifyableRepository modifyable = (IModifyableRepository) repository;
        modifyable.makeItem( RepositoryItems.replaceIdentifier( itemIdentifier, repository.getIdentifier() ) );
      }
    }
  }

  /**
   * FIXME at the moment we assume that an item should be deleted in all sub repositories
   */
  @Override
  public final void deleteItem( final String identifier ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      if( repository instanceof IModifyableRepository )
      {
        final IModifyableRepository modifyable = (IModifyableRepository) repository;
        modifyable.deleteItem( RepositoryItems.replaceIdentifier( identifier, repository.getIdentifier() ) );
      }
    }
  }

  @Override
  public final void setItemData( final String identifier, final Object serializable ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      if( repository instanceof IWriteableRepository )
      {
        final IRepositoryItem item = Repositories.findEquivalentItem( repository, identifier );
        if( item instanceof IWriteableRepositoryItem )
        {
          if( serializable instanceof Serializable )
          {
            final IWriteableRepositoryItem modifyable = (IWriteableRepositoryItem) item;
            modifyable.setData( (Serializable) serializable );
          }
          else
            throw new NotImplementedException();

        }
      }
    }
  }

  @Override
  public final void setItemName( final String identifier, final String name ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      if( repository instanceof IModifyableRepository )
      {
        final IRepositoryItem item = repository.findItem( identifier );
        if( item instanceof IWriteableRepositoryItem )
        {
          final IWriteableRepositoryItem modifyable = (IWriteableRepositoryItem) item;
          modifyable.setName( name );
        }
      }
    }
  }

  @Override
  public boolean isMultipleSourceItem( final String identifier ) throws RepositoryException
  {
    init();

    for( final IRepository repository : m_repositories )
    {
      final IRepositoryItem item = Repositories.findEquivalentItem( repository, identifier );
      if( item != null )
        return item.isMultipleSourceItem();
    }

    return false;
  }

  @Override
  public StatusBean getStatus( final String type )
  {
    init();

    final Set<IStatus> stati = new LinkedHashSet<IStatus>();
    for( final IRepository repository : m_repositories )
      stati.add( repository.getStatus( type ) );

    final IStatus status = StatusUtilities.createStatus( stati, "Repository states" );

    return new StatusBean( status.getSeverity(), status.getPlugin(), status.getMessage() );
  }

  @Override
  public ItemBean getParent( final String identifier ) throws RepositoryException
  {
    init();

    for( final Object element : m_repositories )
    {
      final IRepository rep = (IRepository) element;

      final IRepositoryItem item = rep.findItem( identifier );
      if( item != null )
      {
        final IRepositoryItem parent = item.getParent();
        return toBean( parent );
      }
    }

    m_logger.warning( Messages.getString( "org.kalypso.services.observation.server.ObservationServiceDelegate.3", identifier ) ); //$NON-NLS-1$

    return null;
  }

  private ItemBean toBean( final IRepositoryItem item )
  {
    if( item == null )
      return null;

    final Boolean modifyable = item instanceof IWriteableRepositoryItem;
    return new ItemBean( item.getIdentifier(), item.getName(), modifyable );
  }
}