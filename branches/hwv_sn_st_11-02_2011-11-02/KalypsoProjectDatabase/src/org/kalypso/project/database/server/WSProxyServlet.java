package org.kalypso.project.database.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.SafeRunner;
import org.kalypso.contribs.eclipse.core.runtime.ThreadContextClassLoaderRunnable;
import org.kalypso.project.database.KalypsoProjectDatabase;

import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletContextListener;

/**
 * A wrapper around the {@link WSServlet} class<br>
 * This is needed, as the OSGI stuff has no concept of{@link javax.servlet.ServletContextListener}s, which are needed in
 * order to correctly initialise the servlet.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class WSProxyServlet extends WSServlet
{
  transient private final WSServletContextListener m_servletContextListener = new WSServletContextListener();

// private WSServletDelegate m_delegate;

  /**
   * @see com.sun.xml.ws.transport.http.servlet.WSServlet#init(javax.servlet.ServletConfig)
   */
  @Override
  public void init( final ServletConfig servletConfig ) throws ServletException
  {
    /* Get the servlet context. */
    final ServletContext context = servletConfig.getServletContext();

    // REMARK: We enforce the plugin-classloader as context classloader here. Else, if the plug-in is loaded too
    // early, or i.e. from an ant-task, the classes referenced from the service endpoint interface will not be found.
    final ThreadContextClassLoaderRunnable runnable = new ThreadContextClassLoaderRunnable( KalypsoProjectDatabase.class.getClassLoader() )
    {
      @Override
      protected void runWithContextClassLoader( ) throws Exception
      {
        initContextListener( context );
      }
    };
    SafeRunner.run( runnable );

    final Throwable exception = runnable.getException();
    if( exception != null )
      throw new ServletException( "Failed to initialize Observation-Service", exception );

    /* We just simulate the initialization of the servlet. */
    m_servletContextListener.contextInitialized( new ServletContextEvent( context ) );

    // REMARK: in order to get rid of this context listener; and hence in order to move this code to a common place
    // we must register the servlet adapter like shown in the following lines (copied from
    // DeploymentDescriptorParser)
// ServletAdapterList factory = new ServletAdapterList();
//
// Class implementorClass;
// QName serviceName = new QName("http://server.database.project.kalypso.org/","ProjectDatabaseService");
// QName portName = new QName("http://server.database.project.kalypso.org/","ProjectDatabasePort");
// Container container;
// WSBinding binding;
// WSEndpoint<?> endpoint = WSEndpoint.create(
// implementorClass, true,
// null,
// serviceName, portName, container, binding,
// null, new ArrayList<Object>(), createEntityResolver(),false
// );
//
// ServletAdapter servletAdapter = factory.createAdapter( "projectDatabase", "/projectdb", endpoint );
//
//
// List<ServletAdapter> adapters = new ArrayList<ServletAdapter>(1);
// m_delegate = new WSServletDelegate(adapters,context);
// context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO,m_delegate);

    super.init( servletConfig );
  }

  protected void initContextListener( final ServletContext context )
  {
    /* We just simulate the initialisation of the servlet. */
    m_servletContextListener.contextInitialized( new ServletContextEvent( context ) );
  }

  /**
   * @see javax.servlet.GenericServlet#destroy()
   */
  @Override
  public void destroy( )
  {
    final ServletContext servletContext = getServletContext();

    super.destroy();

    m_servletContextListener.contextDestroyed( new ServletContextEvent( servletContext ) );
  }
}