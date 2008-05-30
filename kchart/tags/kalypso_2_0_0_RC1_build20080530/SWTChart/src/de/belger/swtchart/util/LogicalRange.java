package de.belger.swtchart.util;


/**
 * @author gernot
 */
public class LogicalRange
{
  private double m_from;
  private double m_to;

  public LogicalRange( final double from, final  double to  )
  {
    m_from = Math.min( from, to );
    m_to = Math.max( from, to );
  }

  public void add( final LogicalRange logRange )
  {
    m_from = MathUtil.nanMin( m_from, logRange.m_from );
    m_to = MathUtil.nanMax( m_to, logRange.m_to );
  }

  public double getFrom( )
  {
    return m_from;
  }
  
  public double getTo( )
  {
    return m_to;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return "" + m_from + " - " + m_to;
  }
  
}
