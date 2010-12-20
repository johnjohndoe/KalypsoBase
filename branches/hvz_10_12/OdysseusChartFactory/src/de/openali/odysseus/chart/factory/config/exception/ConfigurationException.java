package de.openali.odysseus.chart.factory.config.exception;

public class ConfigurationException extends Exception
{

  public ConfigurationException( final String message )
  {
    super( message );
  }

  public ConfigurationException( )
  {
    super();
  }

  public ConfigurationException( final String string, final Throwable t )
  {
    super( string, t );
  }

}
