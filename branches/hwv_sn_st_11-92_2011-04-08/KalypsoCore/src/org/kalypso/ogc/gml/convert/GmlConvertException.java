package org.kalypso.ogc.gml.convert;

/**
 * @author belger
 */
public class GmlConvertException extends Exception
{
  public GmlConvertException( )
  {
    super();
  }

  public GmlConvertException( final String message )
  {
    super( message );
  }

  public GmlConvertException( final Throwable cause )
  {
    super( cause );
  }

  public GmlConvertException( final String message, final Throwable cause )
  {
    super( message, cause );
  }

}
