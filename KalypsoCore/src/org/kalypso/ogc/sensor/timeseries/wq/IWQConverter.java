package org.kalypso.ogc.sensor.timeseries.wq;

import java.util.Date;

/**
 * IWQConversion
 * 
 * @author schlienger
 */
// TODO: 'Q' and 'W' is hardcoded here, changes this to 'from' and 'to'
public interface IWQConverter
{
  /** Converts from 'from' to 'to' */
  double computeW( final Date date, final double q ) throws WQException;

  /** Converts from 'to' to 'from' */
  double computeQ( final Date date, final double w ) throws WQException;

  String getFromType( );

  String getToType( );
}
