package org.kalypso.ogc.sensor;

import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;

/**
 * @author Dirk Kuch
 */
public enum TIMESERIES_DATA_TYPE
{
  eDateValue,
  eBooleanValue;

  public static TIMESERIES_DATA_TYPE getType( final String axisType )
  {
    switch( axisType )
    {
      case ITimeseriesConstants.TYPE_DATE:
        return eDateValue;
      case ITimeseriesConstants.TYPE_POLDER_CONTROL:
        return eBooleanValue;

      default:
        return eBooleanValue;
    }
  }
}