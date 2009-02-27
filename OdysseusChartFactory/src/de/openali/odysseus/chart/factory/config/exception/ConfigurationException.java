package de.openali.odysseus.chart.factory.config.exception;


public class ConfigurationException extends Exception
{

  public ConfigurationException( String message )
  {
    super( message );
  }

  public ConfigurationException( )
  {
    super();
  }

  public ConfigurationException( String string, Exception e )
  {
    super( string, e );
  }

}
