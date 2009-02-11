package org.kalypso.chart.factory.configuration.exception;

/**
 * Exception class for exception within the {@link ILayerProvider} framework.
 */
public class LayerProviderException extends ConfigurationException
{

  /**
   * 
   */
  private static final long serialVersionUID = -822031612038055883L;

  public LayerProviderException( String message )
  {
    super( message );
  }

  public LayerProviderException( String message, Throwable cause )
  {
    super( message, cause );
  }

  public LayerProviderException( Throwable cause )
  {
    super( cause );
  }

}
