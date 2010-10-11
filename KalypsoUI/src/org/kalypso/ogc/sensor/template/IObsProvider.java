package org.kalypso.ogc.sensor.template;

import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * @author Gernot Belger
 */
public interface IObsProvider
{
  IObservation getObservation( );

  IRequest getArguments( );

  void addListener( final IObsProviderListener listener );

  void removeListener( final IObsProviderListener listener );

  void dispose( );

  /** Clones this object, that is returns a provider of the same observation */
  IObsProvider copy( );
}
