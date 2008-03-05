package de.openali.odysseus.chart.factory.config.exception;

import java.net.MalformedURLException;

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

  public ConfigurationException( String string, MalformedURLException e )
  {
    super( string, e );
  }

}
