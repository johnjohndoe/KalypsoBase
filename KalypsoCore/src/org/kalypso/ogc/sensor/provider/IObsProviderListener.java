package org.kalypso.ogc.sensor.provider;

/**
 * @author belger
 */
public interface IObsProviderListener
{
  /**
   * If called, the previous {@link org.kalypso.ogc.sensor.IObservation}is no more valid, get a new one by calling
   * {@link IObsProvider#getObservation()}
   */
  void observationReplaced( );

  /**
   * The contents of the current available observation has changed.
   */
  void observationChanged( Object sourcee );
}