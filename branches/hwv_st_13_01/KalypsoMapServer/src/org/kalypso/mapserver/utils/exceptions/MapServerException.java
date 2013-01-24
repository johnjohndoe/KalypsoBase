package org.kalypso.mapserver.utils.exceptions;

/**
 * An exception, that indicates, that something went wrong with the map server.
 * 
 * @author Holger Albert
 */
public class MapServerException extends Exception
{
  /**
   * The constructor.
   */
  public MapServerException( )
  {
  }

  /**
   * The constructor.
   * 
   * @param message
   *          The detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public MapServerException( String message )
  {
    super( message );
  }

  /**
   * The constructor.
   * 
   * @param cause
   *          The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public MapServerException( Throwable cause )
  {
    super( cause );
  }

  /**
   * The constructor.
   * 
   * @param message
   *          The detail message. The detail message is saved for later retrieval by the getMessage() method.
   * @param cause
   *          The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
   *          indicates that the cause is nonexistent or unknown.)
   */
  public MapServerException( String message, Throwable cause )
  {
    super( message, cause );
  }
}