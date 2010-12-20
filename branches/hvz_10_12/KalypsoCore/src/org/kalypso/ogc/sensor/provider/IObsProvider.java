package org.kalypso.ogc.sensor.provider;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * @author Gernot Belger
 */
public interface IObsProvider
{
  void addListener( final IObsProviderListener listener );

  void removeListener( final IObsProviderListener listener );

  /** Clones this object, that is returns a provider of the same observation */
  IObsProvider copy( );

  void dispose( );

  IRequest getArguments( );

  IObservation getObservation( );

  /**
   * @return observation is loaded?
   */
  boolean isLoaded( );
}
