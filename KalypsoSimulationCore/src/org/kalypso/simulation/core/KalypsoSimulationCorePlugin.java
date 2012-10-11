package org.kalypso.simulation.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.core.i18n.Messages;
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
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
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

    final MultiStatus status = new MultiStatus( KalypsoSimulationCorePlugin.getID(), 0, Messages.getString( "org.kalypso.simulation.core.KalypsoSimulationCorePlugin.0" ), null ); //$NON-NLS-1$
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
        status.add( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.simulation.core.KalypsoSimulationCorePlugin.1" ) ) ); //$NON-NLS-1$
      }
    }

    if( status.isOK() )
      throw new CoreException( new Status( IStatus.INFO, KalypsoSimulationCorePlugin.getID(), Messages.getString( "org.kalypso.simulation.core.KalypsoSimulationCorePlugin.2" ) + typeID ) ); //$NON-NLS-1$
    else
      throw new CoreException( status );
  }

}
