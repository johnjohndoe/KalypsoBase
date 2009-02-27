package de.openali.diagram.factory.configuration.exception;

import de.openali.diagram.factory.provider.IAxisProvider;

/**
 * Exception class for exception within the {@link IAxisProvider} framework.
 * 
 * @author alibu
 */
public class AxisProviderException extends ConfigurationException
{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4884774345855845612L;

	public AxisProviderException(  )
	{
		super(  );
	}

	public AxisProviderException( String message )
  {
    super( message );
  }

  public AxisProviderException( String message, Throwable cause )
  {
    super( message, cause );
  }

  public AxisProviderException( Throwable cause )
  {
    super( cause );
  }

}
