package de.openali.odysseus.chart.framework.exception;

public class ZeroSizeDataRangeException extends Exception
{
  /**
   * 
   */
  private static final long serialVersionUID = -3518628678245470254L;

  public ZeroSizeDataRangeException( )
  {
    super( "DataRange must not be zero sized" ); //$NON-NLS-1$
  }

}
