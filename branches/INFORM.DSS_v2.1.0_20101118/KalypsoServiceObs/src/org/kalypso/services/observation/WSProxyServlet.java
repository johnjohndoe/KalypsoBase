package org.kalypso.services.observation;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.SafeRunner;
import org.kalypso.contribs.eclipse.core.runtime.ThreadContextClassLoaderRunnable;

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
  private final WSServletContextListener m_servletContextListener = new WSServletContextListener();

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
    final ThreadContextClassLoaderRunnable runnable = new ThreadContextClassLoaderRunnable( KalypsoServiceObs.class.getClassLoader() )
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