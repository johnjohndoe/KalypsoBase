package org.kalypso.simulation.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class KalypsoSimulationCorePlugin extends Plugin
{
  // The shared instance.
  private static KalypsoSimulationCorePlugin plugin;

  private ISimulationService[] m_services;

  /**
   * The constructor.
   */
  public KalypsoSimulationCorePlugin( )
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
  public static KalypsoSimulationCorePlugin getDefault( )
  {
    return plugin;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  public ISimulationService[] getSimulationServices( )
  {
    if( m_services == null )
      m_services = KalypsoSimulationCoreExtensions.createServices();

    return m_services;
  }

  /**
   * Finds the first Calculation-Service, which can calculate the given type.
   */
  public static ISimulationService findCalculationServiceForType( final String typeID ) throws CoreException
  {
    final ISimulationService[] services = KalypsoSimulationCorePlugin.getDefault().getSimulationServices();

    final MultiStatus status = new MultiStatus( KalypsoSimulationCorePlugin.getID(), 0, "Keiner der konfigurierten Berechnungsdienste kann den gewünschten Modelltyp rechnen.", null );
    for( final ISimulationService service : services )
    {
      try
      {
        final String[] jobTypes = service.getJobTypes();
        for( final String jobType : jobTypes )
        {
          if( typeID.equals( jobType ) )
            return service;
        }
      }
      catch( final SimulationException e )
      {
        status.add( StatusUtilities.statusFromThrowable( e, "Fehler beim Aufruf eines Berechnungsdienstes" ) );
      }
    }

    if( status.isOK() )
      throw new CoreException( StatusUtilities.createInfoStatus( "Keiner der konfigurierten Berechnungsdienste kann den gewünschten Modelltyp rechnen: " + typeID ) );
    else
      throw new CoreException( status );
  }

}
