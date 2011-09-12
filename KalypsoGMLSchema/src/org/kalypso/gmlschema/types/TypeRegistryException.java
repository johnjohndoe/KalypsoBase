package org.kalypso.gmlschema.types;

/**
 * @author belger
 */
public class TypeRegistryException extends Exception
{
  public TypeRegistryException( String message )
  {
    super( message );
  }

  public TypeRegistryException( Throwable cause )
  {
    super( cause );
  }

  public TypeRegistryException( String message, Throwable cause )
  {
    super( message, cause );
  }
}