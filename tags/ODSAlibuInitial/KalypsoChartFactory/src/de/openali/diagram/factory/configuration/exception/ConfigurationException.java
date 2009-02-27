package de.openali.diagram.factory.configuration.exception;

public class ConfigurationException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 9082199820216686027L;

	public ConfigurationException(String msg)
	{
		super(msg);
	}

	public ConfigurationException()
	{
		super();
	}
	
	  public ConfigurationException( String message, Throwable cause )
	  {
	    super( message, cause );
	  }

	  public ConfigurationException( Throwable cause )
	  {
	    super( cause );
	  }
	
}
