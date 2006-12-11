package org.kalypso.services.processing;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.kalypso.services.KalypsoServiceExtensions;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author skurzbach
 */
public class KalypsoWPSPlugin extends Plugin
{
  // The shared instance.
  private static KalypsoWPSPlugin plugin;

  private LocalProcessingService[] m_services;

  /**
   * The constructor.
   */
  public KalypsoWPSPlugin( )
  {
    plugin = this;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start( BundleContext context ) throws Exception
  {
    super.start( context );
    KalypsoServiceExtensions.initializeServiceExtensions(); //TODO: move to kalypso service core plugin
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop( BundleContext context ) throws Exception
  {
    super.stop( context );
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static KalypsoWPSPlugin getDefault( )
  {
    return plugin;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  public LocalProcessingService getLocalProcessingService( final String simulationId )
  {
    try
    {
      return KalypsoWPSExtensions.getRegisteredService( simulationId );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();
    }
    return null;
  }

  public LocalProcessingService[] getLocalProcessingServices( )
  {
    if( m_services == null )
    {
      try
      {
        m_services = KalypsoWPSExtensions.getRegisteredServices();
      }
      catch( final CoreException e )
      {
        e.printStackTrace();
      }
      catch( final JAXBException e )
      {
        e.printStackTrace();
      }
      finally
      {
        if( m_services == null )
        {
          m_services = new LocalProcessingService[0];
        }
      }
    }
    return m_services;
  }

  public Preferences getInstancePreferences( )
  {
    return new InstanceScope().getNode( getBundle().getSymbolicName() );
  }

  public static void log( final int severity, final String message, final Throwable e )
  {
    getDefault().getLog().log( new Status( severity, KalypsoWPSPlugin.getID(), 0, message, e ) );
  }

}
