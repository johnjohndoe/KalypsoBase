package org.kalypso.services.observation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.kalypso.contribs.eclipse.core.runtime.ThreadContextClassLoaderRunnable;
import org.kalypso.services.observation.client.OcsURLStreamHandler;
import org.kalypso.services.observation.client.repository.ObservationServiceRepository;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.services.observation.server.ObservationServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoServiceObsActivator extends Plugin
{
  public final static String SYSPROP_CONFIGURATION_LOCATION = "kalypso.hwv.observation.service.configuration.location"; //$NON-NLS-1$

  public final static String SYSPROP_REINIT_SERVICE = "kalypso.hwv.observation.service.reinit.interval"; //$NON-NLS-1$

  public static final String DEFAULT_OBSERVATION_SERVICE_ID = "default";

  // The shared instance.
  private static KalypsoServiceObsActivator plugin;

  private final Map<String, IObservationService> m_services = new HashMap<String, IObservationService>();

  /**
   * The constructor.
   */
  public KalypsoServiceObsActivator( )
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
  }

  /**
   * Registers the OCS-StreamURLHAndler iun order to support the 'kalypso-ocs' protocoll.<br>
   * Should be called from the application this stuff is running in, else it is not enforced that the protocol is
   * registered before its first use.<br>
   * Maybe we should introduce some kind of 'protocol' extension point that is triggered in KalypsoCore or similar?
   */
  public static void registerOCSUrlHandler( final BundleContext context )
  {
    // register the observation webservice url stream handler
    final OcsURLStreamHandler handler = new OcsURLStreamHandler();

    final Dictionary<Object, Object> properties = new Hashtable<Object, Object>( 1 );
    properties.put( URLConstants.URL_HANDLER_PROTOCOL, new String[] { ObservationServiceRepository.ID } );
    context.registerService( URLStreamHandlerService.class.getName(), handler, properties );
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    super.stop( context );
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoServiceObsActivator getDefault( )
  {
    return plugin;
  }

  public synchronized IObservationService getDefaultObservationService( )
  {

    final IObservationService service = m_services.get( DEFAULT_OBSERVATION_SERVICE_ID );
    if( service == null )
    {
      // REMARK: We enforce the plugin-classloader as context classloader here. Else, if the plug-in is loaded too
      // early, or i.e. from an ant-task, the classes referenced from the service endpoint interface will not be found.
      final ThreadContextClassLoaderRunnable runnable = new ThreadContextClassLoaderRunnable( KalypsoServiceObsActivator.class.getClassLoader() )
      {
        @Override
        protected void runWithContextClassLoader( ) throws Exception
        {
          initObservationService();
        }
      };
      SafeRunner.run( runnable );

      final Throwable exception = runnable.getException();
      if( exception != null )
        exception.printStackTrace();
    }

    return m_services.get( DEFAULT_OBSERVATION_SERVICE_ID );
  }

  /**
   * Convenience method that returns the observation service proxy.
   * 
   * @return WebService proxy for the IObservationService.
   */
  public synchronized IObservationService getObservationService( final String repository )
  {
    final IObservationService service = m_services.get( repository );

    return service;
  }

  public void setObservationService( final String repository, final IObservationService observationService )
  {
    m_services.put( repository, observationService );
  }

  public boolean isObservationServiceInitialized( final String repository )
  {
    final IObservationService service = m_services.get( repository );
    if( service == null )
      return false;

    return true;
  }

  protected void initObservationService( ) throws MalformedURLException
  {
    final String namespaceURI = "http://server.observation.services.kalypso.org/"; //$NON-NLS-1$
    final String serviceImplName = ObservationServiceImpl.class.getSimpleName();

    final String wsdlLocationProperty = System.getProperty( "kalypso.hwv.observation.service.client.wsdl.location" ); //$NON-NLS-1$
    final URL wsdlLocation = new URL( wsdlLocationProperty );
    final QName serviceName = new QName( namespaceURI, serviceImplName + "Service" ); //$NON-NLS-1$
    final Service service = Service.create( wsdlLocation, serviceName );
    final IObservationService observationService = service.getPort( new QName( namespaceURI, serviceImplName + "Port" ), IObservationService.class ); //$NON-NLS-1$

    m_services.put( DEFAULT_OBSERVATION_SERVICE_ID, observationService );
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }
}