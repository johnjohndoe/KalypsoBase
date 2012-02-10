package org.kalypso.ogc.sensor.timeseries.wq;

import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;

/**
 * IWQConversion
 * 
 * @author schlienger
 */
// TODO: 'Q' and 'W' is hardcoded here, changes this to 'from' and 'to'
public interface IWQConverter
{
  /** Converts from 'from' to 'to' */
  double computeW( final ITupleModel model, final Integer index, final double q ) throws WQException, SensorException;

  /** Converts from 'to' to 'from' */
  double computeQ( final ITupleModel model, final Integer index, final double w ) throws WQException, SensorException;

  String getFromType( );

  String getToType( );
}
