package de.openali.odysseus.chart.framework.exception;

public class MalformedValueException extends Exception
{

  /**
   * 
   */
  private static final long serialVersionUID = 256836919640695196L;

  public MalformedValueException( Exception e )
  {
    super( e.getMessage(), e );
  }

  public MalformedValueException( )
  {
    super();
  }

  public MalformedValueException( String msg )
  {
    super( msg );
  }

}
