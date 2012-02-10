package org.kalypso.ogc.sensor.timeseries.wq;

/**
 * WQTableException
 * 
 * @author schlienger
 */
public class WQException extends Exception
{
  public WQException( )
  {
    super();
  }

  public WQException( final String message )
  {
    super( message );
  }

  public WQException( final Throwable cause )
  {
    super( cause );
  }

  public WQException( final String message, final Throwable cause )
  {
    super( message, cause );
  }
}
