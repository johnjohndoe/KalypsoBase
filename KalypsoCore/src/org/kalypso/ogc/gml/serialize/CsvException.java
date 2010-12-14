package org.kalypso.ogc.gml.serialize;

/**
 * @author belger
 */
public class CsvException extends Exception
{
  public CsvException( )
  {
    super();
  }

  public CsvException( final String message )
  {
    super( message );
  }

  public CsvException( final Throwable cause )
  {
    super( cause );
  }

  public CsvException( final String message, final Throwable cause )
  {
    super( message, cause );
  }
}
