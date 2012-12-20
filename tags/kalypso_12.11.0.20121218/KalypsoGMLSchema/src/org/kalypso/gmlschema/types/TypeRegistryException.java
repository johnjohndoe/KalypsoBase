package org.kalypso.gmlschema.types;

/**
 * @author belger
 */
public class TypeRegistryException extends Exception
{
  public TypeRegistryException( final String message )
  {
    super( message );
  }

  public TypeRegistryException( final Throwable cause )
  {
    super( cause );
  }

  public TypeRegistryException( final String message, final Throwable cause )
  {
    super( message, cause );
  }
}