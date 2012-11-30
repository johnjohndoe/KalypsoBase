package org.kalypso.model.wspm.core.result;

/**
 * Ein Ergebnisdatensatz einer 1D Berechnungen Eine Zuordnung Station(String) -> Ergebniswert(double)
 */
public interface IResultSet extends Iterable<String>
{
  String getName( );

  /** Returns the previously assigned value or null, if no value was assigned. */
  Double putValue( final String station, final String type, final double value );

  Double getValue( final String station, final String type );

  IStationResult getValues( final String station );
}
